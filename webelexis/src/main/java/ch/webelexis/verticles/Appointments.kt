package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import ch.rgw.tools.TimeTool
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 15.08.16.
 */
class Appointments : WebelexisVerticle(ID, CONTROL_ADDR) {

    override fun start() {
        super.start()
        log.info("Appointments verticle launching")
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
                    conn.queryWithParams(SQL_APPTS, parms) { result ->
                        if (result.succeeded()) {
                            val rs = result.result()
                            val output = rs.rows
                            msg.reply(JsonUtil.create("status:ok").put("result", output))
                        } else {
                            log.error("error executing SQL_APPTS.", result.cause())
                            msg.reply(JsonUtil.create("status:error", "message:${result.cause().message}"))
                        }
                    }

                }
            }
        }
    }

    override fun getParam(msg: Message<JsonObject>): Future<JsonObject> {
        val ret = Future.future<JsonObject>()
        val param = msg.body().getString("param")
        when (param) {
            "totalEntries" -> {
                sendQuery("SELECT count(*) from AGNTERMINE") { result ->
                    if (result.succeeded()) {
                        val rs = result.result()
                        if (rs.size > 0) {
                            val r = rs.get(0)
                            ret.complete(JsonUtil.create("status:ok").put("value", r.getLong(0)))
                        } else {
                            ret.complete(JsonUtil.create("status:ok", "value:${0}"))
                        }
                    } else {
                        ret.fail(result.cause())
                    }
                }
            }
            "deletedEntries" -> {
                sendQuery("SELECT count(*) from AGNTERMINE where deleted='1'") { result ->
                    if (result.succeeded()) {
                        val rs = result.result()
                        if (rs.size > 0) {
                            val r = rs.get(0)
                            ret.complete(JsonUtil.create("status:ok").put("value", r.getLong(0)))

                        } else {
                            ret.complete(JsonUtil.create("status:ok", "value:${0}"))
                        }
                    } else {
                        ret.fail(result.cause())
                    }
                }
            }
            else -> ret.fail("parameter ${param} unknown")
        }
        return ret

    }

    override fun createParams(): JsonArray {
        val ret = JsonArray()
                .add(JsonUtil.create("name:totalEntries",
                        "caption:Number of appointments",
                        "type:number",
                        "value:${0}")
                        .put("writable",false)
                )
                .add(JsonUtil.create("name:deletedEntries",
                        "caption:Number of deleted entries",
                        "type:number",
                        "value:${0}")
                        .put("writable",false)
                )
        return ret

    }

    override fun getName() = "Elexis Appointments"

    companion object {
        const val BASE_ADDR = "ch.webelexis.appointments."
        const val ID = "ch.webelexis.verticles.appointments"
        const val SQL_APPTS = "SELECT * FROM AGNTERMINE WHERE Tag>=? AND Tag<=? AND Bereich=? AND deleted='0'"
        val FUNC_LIST = RegSpec(BASE_ADDR + "list", "appnts/list/:resource/:from/:until", "user", "get")
        const val CONTROL_ADDR = BASE_ADDR + "admin"

    }
}