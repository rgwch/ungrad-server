package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import ch.rgw.tools.TimeTool
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 15.08.16.
 */
class Appointments : WebelexisVerticle() {

    override fun start() {
        register(FUNC_LIST)

        vertx.eventBus().consumer<JsonObject>(FUNC_LIST.addr) { msg ->
            getConnection(msg) {
                if (it.succeeded()) {
                    val conn = it.result()
                    val from = msg.body().getString("from")
                    val until = msg.body().getString("until")
                    val rsrc = msg.body().getString("resource")
                    val parms = JsonArray()
                            .add(TimeTool(from).toString(TimeTool.DATE_COMPACT))
                            .add(TimeTool(until).toString(TimeTool.DATE_COMPACT))
                            .add(rsrc)
                    conn.queryWithParams(SQL_APPTS,parms){ result ->
                        if(result.succeeded()){
                            val rs=result.result()
                            val output=rs.rows
                            msg.reply(JsonUtil.create("status:ok").put("result",output))
                        }else{
                            log.error("error executing SQL_APPTS.",result.cause())
                            msg.reply(JsonUtil.create("status:error","message:${result.cause().message}"))
                        }
                    }

                }
            }
        }
    }

    companion object {
        const val SQL_APPTS = "SELECT * FROM AGNTERMINE WHERE Tag>=? AND Tag<=? AND Bereich=? AND deleted='0'"
        const val BASE_ADDR = "ch.webelexis.appointments."
        val FUNC_LIST = RegSpec(BASE_ADDR+"list", "appnts/list/:resource/:from/:until", "user", "get")
    }
}