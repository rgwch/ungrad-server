package ch.rgw.ungrad_backup

import ch.rgw.tools.json.get
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.UserInfo
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.*

/**
 * Created by gerry on 02.12.16.
 */
class Scp(val cfg: JsonObject, val subdir: String = "") {
    val js = JSch()

    val log = LoggerFactory.getLogger("Ungrad SCP")
    lateinit var inp: InputStream
    lateinit var out: OutputStream
    lateinit var session: Session

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
        if (b < 1) return true;

        val sb = StringBuffer()
        var c: Int
        do {
            c = inp.read()
            sb.append(c.toChar())
        } while (c.toChar() != '\n')
        log.error(sb.toString())

        return false
    }

    fun transmit(file: File): Boolean {
        val cmd = if (subdir.isEmpty()){
            "scp "
        }else{
            "mkdir $subdir && cd $subdir && scp "
        }+" -t "+file.name
        val channel = open(cmd)

        if (checkAck()) {

            val filesize = file.length()
            var command = "C0644 ${filesize.toString()} ${file.name}\n"
            val ba = command.toByteArray(Charsets.UTF_8)
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
                    return true
                }
            }
        }
        return false
    }

    fun fetch(filename: String, destDir: File): File? {
        val channel = open("scp -f ${subdir}/$filename")
        out.write(0)
        out.flush()
        var c = 0
        val sb=StringBuilder()
        while(c.toChar()!='\n'){
            c=inp.read()
            sb.append(c.toChar())
            if(c<=0){
                throw IOException("Error scp fetch "+sb.toString())
            }
        }
        val line=sb.toString().split(" +".toRegex())
        if(line.size<3){
            throw(IOException("Bad protocol for scp"))
        }
        val fileSize=line[1].toLong()
        log.debug("fetching ${line[2]}. ${line[1]} bytes.")
        out.write(0)
        out.flush()
        val fout = File(destDir, filename)
        val fouts = FileOutputStream(fout)
        var count=0;
        while (count++<fileSize) {
            c = inp.read()
            if (c < 0) {
                throw IOException("Error reading file "+filename)
            }
            fouts.write(c)
        }
        out.close()
        inp.close()
        channel.disconnect()
        session.disconnect()
        fouts.close()
        return fout;
    }

}

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