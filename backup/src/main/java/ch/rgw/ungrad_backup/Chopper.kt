package ch.rgw.ungrad_backup

import ch.qos.logback.core.encoder.ByteArrayUtil
import ch.rgw.io.FileTool
import ch.rgw.tools.crypt.TwofishInputStream
import ch.rgw.tools.crypt.TwofishOutputStream
import ch.rgw.tools.crypt.Twofish_Algorithm
import ch.rgw.tools.json.decrypt
import ch.rgw.tools.json.encrypt
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.*
import java.security.MessageDigest
import java.util.*

/**
 * Created by gerry on 01.12.16.
 */
class Chopper {

    val md = MessageDigest.getInstance("SHA-1")

    fun chop(source: File, destDir: File, size: Long, key: ByteArray): JsonObject {
        md.reset()
        val skey = Twofish_Algorithm.makeKey(key)
        val list = JsonArray()
        val input = BufferedInputStream(FileInputStream(source))
        if(!destDir.exists()) {
            destDir.mkdirs()
        }
        while (true) {
            val name = UUID.randomUUID().toString()
            val output = File(destDir, name)
            val out = TwofishOutputStream(BufferedOutputStream(FileOutputStream(output)), skey)
            var i: Long = 0
            var c: Int = 0
            while (i++ < size) {
                c = input.read()
                if (c == -1) {
                    break;
                }
                out.write(c)
                md.update(c.toByte())
            }
            out.close()
            list.add(name)
            if (c == -1) {
                break
            }
        }
        val ret = JsonObject().put("files", list).put("hash", ByteArrayUtil.toHexString(md.digest()))
                .put("filename", source.name)

        FileTool.writeFile(File(destDir, "directory"), ret.encrypt(ByteArrayUtil.toHexString(key)))
        return ret;
    }

    fun unchop(source: File, destDir: File, key: ByteArray):String {
        md.reset()
        val skey = Twofish_Algorithm.makeKey(key)
        val directory = decrypt(FileTool.readFile(File(source, "directory")), ByteArrayUtil.toHexString(key))
        val destName=directory.getString("filename")
        val output = BufferedOutputStream(FileOutputStream(File(destDir, destName)))

        for (file in directory.getJsonArray("files")) {
            val input = TwofishInputStream(BufferedInputStream(FileInputStream(File(source, file as String))), skey)
            var c: Int = 0
            while (true) {
                c = input.read()
                if (c == -1) {
                    break;
                }
                md.update(c.toByte())
                output.write(c)
            }
            output.flush();
        }
        output.close()
        val compare=ByteArrayUtil.toHexString(md.digest())
        if(compare!=directory.getString("hash")){
            throw(IOException("Checksum Error"))
        }
        return destName
    }
}