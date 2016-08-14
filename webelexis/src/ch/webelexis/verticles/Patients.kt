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
class Patients : AbstractVerticle() {
    val API = "1.0"

    val database: AsyncSQLClient by lazy {
        log.debug("creating database " + config().encodePrettily())
        if (config().getString("type", "mysql") == "mysql") {
            MySQLClient.createShared(vertx, config())
        } else {
            PostgreSQLClient.createShared(vertx, config())
        }
    }

    override fun start() {
        log.info("Start")
        fun register(func: RegSpec) {
            vertx.eventBus().send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                    .put("ebaddress", BASE_ADDR + func.addr)
                    .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                    .put("server-id", "ch.webelexis").put("server-control", CONTROL_ADDR), RegHandler(func))
        }

        register(FUNC_PATLIST)

        vertx.eventBus().consumer<JsonObject>(CONTROL_ADDR, Admin)

        val addr = BASE_ADDR + FUNC_PATLIST.addr
        vertx.eventBus().consumer<JsonObject>(addr) { msg ->
            database.getConnection { result ->
                if (result.succeeded()) {
                    val conn = result.result()
                    val parm = msg.body().getString("pattern")
                    conn.queryWithParams(PATLIST, JsonArray().add(parm).add(parm)) { answer ->
                        if (answer.succeeded()) {
                            val rs = answer.result() as ResultSet
                            val ret = JsonArray()
                            while (rs.next()) {
                                val patient = Patient(rs.getString("Bezeichnung1"), rs.getString("Bezeichnung2"), rs.getString("id"))
                                patient.put("patnr", rs.getString("patientnr"))
                                ret.add(patient)
                            }
                            msg.reply(JsonUtil.create("status:ok").put("result", ret))
                        } else {
                            log.error("Error executing PATLIST with ${msg.body().encodePrettily()}", answer.cause())
                            msg.reply(JsonUtil.create("status:error", "message:${answer.cause().message}"))
                        }
                    }

                } else {
                    log.error("could not connect to database", result.cause())
                }
            }
        }
    }

    override fun stop() {
        log.info("stop")
        database.close()
    }

    companion object {
        const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
        const val BASE_ADDR = "ch.webelexis.patients."
        const val CONTROL_ADDR = BASE_ADDR + "admin"
        const val LIST = BASE_ADDR + "list"
        val FUNC_PATLIST = RegSpec("list", "patients/list/:pattern", "user", "get")
        val log = LoggerFactory.getLogger(Patients::class.java)
        val PATLIST = "SELECT patientnr,id,Bezeichnung1,Bezeichnung2,Bezeichnung3,FROM KONTAKT WHERE Bezeichnung1 like ? OR Bezeichnung2 like ?"
    }

    data class RegSpec(val addr: String, val rest: String, val role: String, val method: String)

    class RegHandler(val func: RegSpec) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.failed()) {
                log.error("could not register ${func.addr} for ${func.rest}: ${result.cause()}")
            } else {
                if ("ok" == result.result().body().getString("status")) {
                    log.debug("registered ${func.addr}")
                } else {
                    log.error("registering of ${func.addr} failed: ${result.result().body().getString("message")}")
                }
            }
        }

    }


}