package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import org.slf4j.LoggerFactory
import java.sql.ResultSet

/**
 * Created by gerry on 14.08.16.
 */
class Patients : WebelexisVerticle() {


    override fun start() {
        log.info("Start")


        register(FUNC_PATLIST)
        register(FUNC_PATDETAIL)

        vertx.eventBus().consumer<JsonObject>(CONTROL_ADDR, Admin)

        vertx.eventBus().consumer<JsonObject>(FUNC_PATLIST.addr) { msg ->
            getConnection(msg) { result ->
                if (result.succeeded()) {
                    val conn = result.result()
                    val parm = msg.body().getString("pattern").replace('*', '%')
                    val params = JsonArray().add(parm).add(parm)
                    conn.queryWithParams(SQL_PATLIST, params) { answer ->
                        if (answer.succeeded()) {
                            val rs = answer.result()
                            val ret = JsonArray()
                            rs.results.forEach {
                                val patient = Patient(it.getString(2), it.getString(3), it.getString(1))
                                patient.put("patnr", it.getString(0))
                                ret.add(patient)
                            }
                            msg.reply(JsonUtil.create("status:ok").put("result", ret))
                            conn.close()
                        } else {
                            log.error("Error executing PATLIST with ${msg.body().encodePrettily()}", answer.cause())
                            msg.reply(JsonUtil.create("status:error", "message:${answer.cause().message}"))
                            conn.close()
                        }
                    }

                }
            }
        }

        vertx.eventBus().consumer<JsonObject>(FUNC_PATDETAIL.addr) { msg ->
            getConnection(msg) { con ->
                if (con.succeeded()) {
                    val conn = con.result()
                    val parm = msg.body().getString("id")
                    val params = JsonArray().add(parm)
                    conn.queryWithParams(SQL_PATDETAIL, params) { result ->
                        if (result.succeeded()) {
                            val rs = result.result()
                            if (rs.numRows != 1) {
                                msg.reply(JsonUtil.create("status:error", "message:${rs.numRows} results"))
                            } else {
                                val pat = rs.rows[0]
                                msg.reply(pat)
                            }
                            conn.close()
                        } else {
                            log.error("Error executing PATDETAIL with ${msg.body().encodePrettily()}", result.cause())
                            msg.reply(JsonUtil.create("status:error", "message:${result.cause().message}"))
                            conn.close()
                        }
                    }

                }
            }
        }
    }


    companion object {
        const val BASE_ADDR = "ch.webelexis.patients."
        const val CONTROL_ADDR = BASE_ADDR + "admin"
        val FUNC_PATLIST = RegSpec(BASE_ADDR+"list", "patients/list/:pattern", "user", "get")
        val FUNC_PATDETAIL = RegSpec(BASE_ADDR+"detail", "patients/detail/:id", "user", "get")
        val log = LoggerFactory.getLogger(Patients::class.java)
        val SQL_PATLIST = "SELECT patientnr,id,Bezeichnung1,Bezeichnung2,Bezeichnung3 FROM KONTAKT WHERE (Bezeichnung1 like ? OR Bezeichnung2 like ?) AND deleted='0'"
        val SQL_PATDETAIL = "SELECT * FROM KONTAKT WHERE id=? AND deleted='0'"
    }


}