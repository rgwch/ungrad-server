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
 */
package ch.elexis.ungrad.server

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.net.URLClassLoader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


/**
 * Created by gerry on 06.08.16.
 * Handle requests on the /ui interface. Load static resources from the file system or from the *.jar of the application.
 * Set file times and expire times correctly
 */
class UIHandler(val cfg: JsonObject) : Handler<RoutingContext> {

    val df = SimpleDateFormat("E, dd MM yyyy HH:mm:ss z")
    val dm = SimpleDateFormat("yyyy-MM-dd HH:mm")
    val log = LoggerFactory.getLogger("UIHandler")

    init {
        df.timeZone = TimeZone.getTimeZone("UTC")
        log.info("Origin is "+File(".").absolutePath)
        log.info("Webroot is "+File(cfg.getString("webroot",".")).absolutePath)
        println("UIHandler launched")
    }

    override fun handle(ctx: RoutingContext) {
        ctx.response().putHeader("Date", df.format(Date()))
        ctx.response().putHeader("Server", "Elexis Ungrad Server")
        val rawPath = ctx.request().path()
        val reqpath=if(rawPath.startsWith(prefix)){
            rawPath.substring(prefix.length)
        }else{
            rawPath
        }

        if (reqpath == "/" || reqpath == "/index.html" || reqpath == "/index.htm") {
            val rnd = UUID.randomUUID().toString()
            var scanner: Scanner? = null
            try {
                //File in = new File(cfg.getString("webroot"), "index.html");
                //Date lm = new Date(in.lastModified());
                val index = getResource(cfg.getString("webroot"), "/index.html")
                val lm = index.lastModified
                ctx.response().putHeader("Last-Modified", df.format(lm))
                var cid: String? = cfg.getString("googleID")
                if (cid == null) {
                    cid = "x-undefined"
                }

                scanner = Scanner(index.stream, "UTF-8")
                var modified = scanner.useDelimiter("\\A").next().replace("GUID".toRegex(), rnd).replace("GOOGLE_CLIENT_ID".toRegex(), cid)
                ctx.response().putHeader("Content-Length", java.lang.Long.toString(modified.length.toLong()))
                ctx.response().write(modified)
            } catch (e: Throwable) {
                log.error("Could not return index.html",e )
                ctx.response().setStatusCode(501).end("internal Server error")
            } finally {
                if (scanner != null) {
                    scanner.close()
                    ctx.response().end()
                }
            }

        } else if (reqpath.contains("..")) {
            ctx.response().setStatusCode(403).end()
        } else {
            anyOtherResource(ctx)
        }


    }

    /**
     * Load a resource as requested in the HTTPServerRequest.
     * If the resource path starts with "/custom/" search in the customRoot first. If not found, search in
     * webroot.
     * If the path starts with any other prefix, search directly from webroot
     * @param req The HttpServerRequest
     */
    private fun anyOtherResource(req: RoutingContext) {
        try {
            var rsc: RscObject
            val rawPath = req.request().path()
            val addrPath=if(rawPath.startsWith(prefix)){
                rawPath.substring(prefix.length)
            }else{
                rawPath
            }
            val cOff = addrPath.indexOf("/custom/")
            if (cOff != -1) {
                rsc = getResource(cfg.getString("customRoot", ""), addrPath.substring(cOff + 8))
                if (rsc.stream == null) {
                    rsc = getResource(cfg.getString("webroot"), addrPath)
                }
            } else {
                rsc = getResource(cfg.getString("webroot"), addrPath)
            }
            if (rsc.stream == null) {
                val ans = "${addrPath} not found"
                req.response().putHeader("Content-Length", Integer.toString(ans.length))
                req.response().write(ans)
                req.response().statusCode = 404 // not found
                req.response().statusMessage = "Not found"
                req.response().end()
            } else {
                /*
                   * If the client asks for an existing file: define cache control: (arbitrarily). let
                   * css and js be valid for 24 hours, image files for 10 days. But both can
                   * live longer, if the client knows and uses the "if-modified-since"-
                   * Header on expired files.
                   */

                val lm = rsc.lastModified
                val reqDat = req.request().headers().get("if-modified-since")
                var noSend = false
                if (reqDat != null) {
                    try {
                        val reqDate = df.parse(reqDat)
                        if (!reqDate.before(lm)) {
                            // after or equal
                            noSend = true
                        }
                    } catch (e: ParseException) {
                        log.warn("could not parse reqDat " + reqDat)
                    }

                }
                req.response().putHeader("Last-Modified", df.format(lm))
                if (addrPath.endsWith(".css")) {
                    req.response().putHeader("Cache-Control", "max-age=864000")
                    req.response().putHeader("content-type", "text/css; charset=UTF-8")
                } else if (addrPath.endsWith(".js")) {
                    req.response().putHeader("Content-Type", "application/javascript; charset=utf-8")
                    req.response().putHeader("Cache-Control", "max-age=864000")
                } else if (addrPath.endsWith(".png") || addrPath.endsWith(".jpg")) {
                    req.response().putHeader("Cache-Control", "public, max-age=864000")
                }
                if (noSend) {
                    req.response().statusCode = 304
                    // req.response().setStatusMessage("not modified");
                    req.response().end()
                } else {
                    req.response().isChunked = true
                    val bis = BufferedInputStream(rsc.stream)
                    val buffer = ByteArray(4 * 4096)
                    var i: Int
                    do {
                        i = bis.read(buffer)
                        val vb = Buffer.buffer(buffer)
                        req.response().write(vb.slice(0, i))
                    } while (i == buffer.size)
                    req.response().end()
                }
            }
        } catch (ex: Exception) {
            req.response().statusCode = 500
        }

    }

    /**
     * Find and load a file either from the file system, or from the .jar of the application

     * @param root parent directory for the search
     * @param name pathname relative to root
     * @return an Object containing a Stream and some informations on the file
     * @throws ParseException
     */
    @Throws(ParseException::class)
    internal fun getResource(root: String, name: String): RscObject {
        try {
            val className = javaClass.simpleName + ".class"
            val classPath = javaClass.getResource(className).toString()
            log.debug(classPath)
            if (!classPath.startsWith("jar")) {
                // Class not from JAR
                val file = File(root, name)
                log.debug(file.absolutePath)
                val fis = FileInputStream(file)
                return RscObject(fis, false, file.lastModified())
            }
            val manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF"
            log.debug(manifestPath)
            val manifest = Manifest(URL(manifestPath).openStream())
            val attr = manifest.mainAttributes
            val timestamp = attr.getValue("timestamp")
            val dat = dm.parse(timestamp)
            val jarfile=classPath.substring(4, classPath.lastIndexOf("!"))
            // log.debug("Jarfile is $jarfile")
            val jarURL=URL(jarfile)
            // log.debug("URL of jar file is: ${jarURL.path}")
            val urlcs=URLClassLoader(Array<URL>(1,{jarURL}))
            val rsr=urlcs.findResource(root+name)
            log.debug("Resource loader: trying to load (${root+name}) from $classPath with timestamp ${dat.toString()}")
            return RscObject(rsr.openStream(),true,dat.time)

        } catch (e: IOException) {
            e.printStackTrace()
            return RscObject(null, false, 0L)
        }

        /*
        val resource = classPath.substring(0, classPath.lastIndexOf("!") + 1)+"/" + root + name
        log.debug("Resource loader: trying to load ${resource} with timestamp ${dat.toString()}")
        val rsr = ClassLoader.getSystemClassLoader().getResource(resource)
        log.debug(if (rsr == null) "resource is null" else rsr.toString())
        return RscObject(javaClass.getResourceAsStream(resource), true, dat.time)
        */

    }

    class RscObject(val stream: InputStream?, val inJar: Boolean, millis: Long) {
        var lastModified: Date

        init {
            lastModified = Date(millis)
        }

    }
    companion object{
        val prefix="/ui"
    }
}