package ch.elexis.ungrad.server

import ch.rgw.tools.JsonUtil
import java.io.File

/**
 * Created by gerry on 05.09.16.
 */
class JsonFilePersistor:IPersistor {

    val conf = JsonUtil.createFromFile(File(System.getProperty("user.home"), "ungradServer.config"),true)

    override fun write(id: String, value: JsonUtil): Boolean {
        conf.put(id,value)
        return conf.flush()
    }

    override fun read(id: String): JsonUtil {
        return conf.getBranch(id)
    }
}