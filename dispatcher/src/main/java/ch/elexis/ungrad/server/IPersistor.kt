package ch.elexis.ungrad.server

import ch.rgw.tools.JsonUtil
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 05.09.16.
 */
interface IPersistor {
    fun read(id:String): JsonUtil
    fun write(id:String,value:JsonUtil):Boolean
}