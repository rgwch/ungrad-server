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


package ch.rgw.ungrad_backup

import ch.rgw.tools.json.get
import ch.rgw.tools.json.json_error
import ch.rgw.tools.json.json_ok
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.UserInfo
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.*

/**
 * Transfer files with SCP to remote hosts
 * Created by gerry on 02.12.16.
 *
 * The Consutructor takes a JsonObject which must contain at least the following fields:
 * * user - the username for the remote host
 * * password - the password needed for ssh/scp
 * * host - the IP or name of the remote host
 * * port - the Port to use (defaults to 22)
 * * directory - the subdirectory to use on the remote host (relative to the user's home dir). If the directory
 * does not exist, it will be created.
 */
class Scp(val cfg: JsonObject) {
    val js = JSch()

    val log = LoggerFactory.getLogger("Ungrad SCP")
    lateinit var inp: InputStream
    lateinit var out: OutputStream
    lateinit var session: Session


    /**
     * Send a [file] to the remote host
     * @return true on success
     */
    fun transmit(file: File): JsonObject {
        try {
            val subdir = cfg["directory"]
            val cmd = if (subdir == null) {
                "scp "
            } else {
                "mkdir $subdir && cd $subdir && scp "
            } + " -t " + file.name
            val channel = open(cmd)

            if (checkAck()) {

                val filesize = file.length()
                val command = "C0644 ${filesize.toString()} ${file.name}\n"
                //val ba = command.toByteArray(Charsets.UTF_8)
                out.write(command.toByteArray(Charsets.UTF_8))
                out.flush()
                if (checkAck()) {
                    file.forEachBlock(1024) { bytes, i ->
                        out.write(bytes, 0, i)
                    }
                    out.write(0)
                    out.flush()
                    if (checkAck()) {
                        out.close()
                        inp.close()
                        channel.disconnect()
                        session.disconnect()
                        return json_ok()
                    }
                }
            }
            return json_error("contact error with server")
        }catch(ex:Exception){
            log.error(ex.message)
            ex.printStackTrace()
            return json_error("Exception ${ex.message}")
        }
    }

    /**
     * fetch a file with the given [filename] from the remote computer and store it in [destDir]
     * @return the File on success, or null.
     */
    fun fetch(filename: String, destDir: File): JsonObject {
        try {
            val cmd = if (cfg["directory"] == null) {
                "scp -f $filename"
            } else {
                "scp -f ${cfg["directory"]}/$filename"
            }
            val channel = open(cmd)
            out.write(0)
            out.flush()
            var c = 0
            val sb = StringBuilder()
            while (c.toChar() != '\n') {
                c = inp.read()
                sb.append(c.toChar())
                if (c <= 0) {
                    throw IOException("Error scp fetch " + sb.toString())
                }
            }
            val line = sb.toString().split(" +".toRegex())
            if (line.size < 3) {
                throw(IOException("Bad protocol for scp"))
            }
            val fileSize = line[1].toLong()
            log.debug("fetching ${line[2]}. ${line[1]} bytes.")
            out.write(0)
            out.flush()
            val fout = File(destDir, filename)
            val fouts = FileOutputStream(fout)
            var count = 0
            while (count++ < fileSize) {
                c = inp.read()
                if (c < 0) {
                    throw IOException("Error reading file " + filename)
                }
                fouts.write(c)
            }
            out.close()
            inp.close()
            channel.disconnect()
            session.disconnect()
            fouts.close()
            return json_ok().put("file",fout.absolutePath);
        }catch(ex:Exception){
            log.error(ex.message)
            ex.printStackTrace()
            return json_error("Exception: ${ex.message}")
        }
    }


    private fun open(command: String): ChannelExec {
        session = js.getSession(cfg["user"], cfg["host"], cfg.getInteger("port", 22))
        session.userInfo = SCPUser(cfg["password"]!!)
        session.connect()
        val channel = session.openChannel("exec") as ChannelExec
        channel.setCommand(command)
        out = channel.outputStream
        inp = channel.inputStream
        channel.connect()
        return channel
    }

    private fun checkAck(): Boolean {
        val b = inp.read()
        if (b < 1) return true

        val sb = StringBuffer()
        var c: Int
        do {
            c = inp.read()
            sb.append(c.toChar())
        } while (c.toChar() != '\n')
        log.error(sb.toString())

        return false
    }
}

/**
 * Helper class to make jsch happy. Only purpose here is to return the password supplied to the constructor.
 */
class SCPUser(val pwd: String) : UserInfo {
    override fun promptPassword(p0: String?) = true
    override fun promptPassphrase(p0: String?) = true
    override fun promptYesNo(p0: String?) = true
    override fun getPassphrase(): String? = null

    override fun showMessage(p0: String?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getPassword(): String {
        return pwd
    }

}