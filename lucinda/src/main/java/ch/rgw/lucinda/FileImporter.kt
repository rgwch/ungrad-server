/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 ********************************************************************************/

package ch.rgw.lucinda

import ch.rgw.io.FileTool
import ch.rgw.tools.crypt.makeHash
import ch.rgw.tools.json.get
import ch.rgw.tools.json.set
import io.vertx.core.json.JsonObject
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage
import java.io.*
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/**
 * This is a Handler<Future> which is called from the indexing process to import a new file.
 * Created by gerry on 05.05.16.
 */

class FileImporter {
    val temppath: String by lazy {
        val checkDir = File(lucindaConfig.getString("fs_basedir", "target/store"), "tempfiles")
        if (checkDir.exists()) {
            if (checkDir.isFile) {
                log.error("temporary directory exists but is a file")
            }
        } else {
            if (!checkDir.mkdirs()) {
                log.error("Can't create tempdir")
            }
        }
        checkDir.absolutePath
    }
    val failures: String by lazy {
        val checkDir = File(lucindaConfig.getString("fs_basedir", "target/store"), "failures")
        if (checkDir.exists()) {
            if (checkDir.isFile) {
                log.error("failure directory $checkDir exists but is a file")
            }
        } else {
            if (!checkDir.mkdirs()) {
                log.error("Can't create directory $checkDir")
            }
        }
        checkDir.absolutePath
    }


    fun process(file: Path, fileMetadata: JsonObject): String {
        val filename = fileMetadata["url"] ?: file.toFile().absolutePath
        with(File(filename)) {
            if (!exists()) {
                return "file not found"
            }
            if (!canRead()) {
                return "can't read"
            }
            if (length() < 10) {
                return "too short"
            }
            log.info("FileImporer: importing $absolutePath")
        }

        try {
            val payload = if (fileMetadata.containsKey("payload")) {
                fileMetadata.getBinary("payload")
            } else {
                val plc = FileTool.readFileWithChecksum(File(filename))
                fileMetadata.put("uuid", plc["checksumString"])
                plc["contents"] as ByteArray
            }
            fileMetadata.remove("payload")
            if (fileMetadata["_id"].isNullOrBlank()) {
                fileMetadata["_id"] = makeID(file)
            }
            val doc = Communicator.indexManager.addDocument(ByteArrayInputStream(payload), fileMetadata)
            val text = doc.getField("text").stringValue()
            if ((text.length < 15) and (doc.get("content-type") == ("application/pdf"))) {
                if (text == "unparseable") {
                    log.info("unparseable text")
                    return ""
                } else {
                    // if we don't get much text out of a pdf, it's probably a scan containing only one or more images.
                    return tryOCR(doc, filename)
                }
            } else {
                // no pdf or text too short
                return ""
            }


        } catch(ex: Exception) {
            ex.printStackTrace()
            log.error("fatal error reading $filename")
            return ("read error ${ex.message}")
        }
    }

    /**
     * send a pdf file through OCR: Extract all images from the file and call tesseract-ocr with
     * each of them. If tesseract finds some text, add this text to the lucene index.
     */
    fun tryOCR(doc: Document, filename: String): String {
        var failed = false
        val basename = temppath + "/" + makeHash(filename)
        log.info("Seems to be a PDF with only image(s). Trying OCR as $basename")
        var numImages = 0
        fun writeImage(img: PDXObjectImage): Boolean {
            val imgName = basename + "_" + (++numImages).toString()
            img.write2file(imgName)
            val sourcename = imgName + "." + img.suffix
            val result = runTesseract(sourcename, imgName)
            FileTool.deleteFile(sourcename)
            if (result) {
                val plaintext = File(imgName + ".txt")
                if (plaintext.exists() and plaintext.canRead() and (plaintext.length() > 10L)) {
                    val newtext = FileTool.readTextFile(plaintext)
                    doc.add(TextField("text", newtext, Field.Store.NO))
                    Communicator.indexManager.updateDocument(doc)
                    plaintext.delete()
                } else {
                    log.warn("no text content found in $filename")
                }
                return true
            } else {
                return false
            }
        }
        try {
            // This will throw an exception if the pdf is invalid.
            val document = PDDocument.load(filename)
            val list = document.documentCatalog.allPages
            list.forEach { page ->
                val pdResources = (page as? PDPage)?.resources
                val pageImages = pdResources?.xObjects
                val imageIter = pageImages?.keys?.iterator()
                imageIter?.forEach {
                    val pdxObjectImage = pageImages?.get(it)
                    if (pdxObjectImage is PDXObjectForm) {
                        val xrs = pdxObjectImage.resources?.xObjects
                        xrs?.keys?.forEach { kk ->
                            val subimg = xrs.get(kk)
                            if (subimg is PDXObjectImage) {
                                writeImage(subimg)
                            }
                        }
                    } else if (pdxObjectImage is PDXObjectImage) {
                        writeImage(pdxObjectImage);
                    }
                }
            }
            document.close()
            return ""

        } catch(ex: Exception) {
            ex.printStackTrace()
            log.error("Fatal error in pdf file $filename: ${ex.message}")
            return ("Exception thrown: ${ex.message}")
        }
    }

    /**
     * Try to OCR an image document. If tesseract doesn't return after TIMEOUT seconds,
     * stop it
     */

    val TIMEOUT = 300L

    fun runTesseract(source: String, dest: String): Boolean {
        val process = Runtime.getRuntime().exec(listOf("tesseract", source, dest).toTypedArray())
        if (process == null) {
            log.error("Could not launch tesseract!")
            throw IllegalArgumentException("tesseract setting wrong")
        }
        val errmsg = ByteArrayOutputStream()

        val err = Catcher(process.errorStream, errmsg)
        val output = ByteArrayOutputStream()
        val out = Catcher(process.inputStream, output)
        err.start()
        out.start()
        if (process.waitFor(TIMEOUT, TimeUnit.SECONDS)) {
            val exitValue = process.exitValue()
            log.info("tesseract $source ended, " + output.toString("utf-8"))
            return exitValue == 0
        } else {
            log.error("Tesseract process hangs " + errmsg.toString("utf-8"))
            process.destroy()
            return false
        }
    }

    internal class Catcher(val inp: InputStream, val outp: OutputStream) : Thread() {
        override fun run() {
            inp.copyTo(outp, 4096)
        }
    }

}