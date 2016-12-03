package ch.rgw.ungrad_backup

import ch.rgw.tools.json.json_create
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 02.12.16.
 */
class Verticle :AbstractVerticle() {
    val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
    val BASE_ADDRESS="ch.rgw.ungrad.backup"
    val CONTROL_ADDRESS=BASE_ADDRESS+".admin"
    val ID="ch.rgw.ungrad.backup"
    val API="1.0"
    val FUNC_REDUCE=RegSpec(BASE_ADDRESS+".reduce","/reduce/:sourcedir/:destfile","admin","GET")
    val FUNC_CHOP=RegSpec(BASE_ADDRESS+".chop","/chop/:sourcefile/:destdir","admin","GET")
    val FUNC_SCP=RegSpec(BASE_ADDRESS+"scp","/scp/:sourcedir","admin","GET")


    override fun start() {
        register(FUNC_REDUCE)
        register(FUNC_CHOP)
        register(FUNC_SCP)

    }

    fun register(func: RegSpec) {
        vertx.eventBus().send(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server", json_create("id:$ID", "name:Backup", "address:$CONTROL_ADDRESS").put("params", createParams())))

    }

    data class RegSpec(val addr: String, val rest: String, val role: String, val method: String)

    fun createParams(): JsonArray {
        return JsonArray()
    }
}