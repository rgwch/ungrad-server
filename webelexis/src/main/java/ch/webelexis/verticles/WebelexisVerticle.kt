package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import com.hazelcast.logging.LoggerFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.sql.SQLConnection
import org.slf4j.Logger

/**
 * Created by gerry on 15.08.16.
 */
open class WebelexisVerticle : AbstractVerticle(){
    val API = "1.0"
    val log : Logger=org.slf4j.LoggerFactory.getLogger(WebelexisVerticle::class.java)
    val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"


    val database: AsyncSQLClient by lazy {
        Patients.log.debug("creating database " + config().encodePrettily())
        if (config().getString("type", "mysql") == "mysql") {
            MySQLClient.createShared(vertx, config())
        } else {
            PostgreSQLClient.createShared(vertx, config())
        }
    }


    override fun stop() {
        Patients.log.info("stop")
        database.close()
    }

    fun getConnection(msg: Message<JsonObject>,handler: ((AsyncResult<SQLConnection>) -> Unit)){
        database.getConnection() { con ->
            if(con.succeeded()){
                handler.invoke(con)
            }else{
                log.error("could not connect to database", con.cause())
                msg.reply(JsonUtil.create("status:error","message:internal server error: database"))
            }
        }
    }
    fun register(func: RegSpec) {
        vertx.eventBus().send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server-id", "ch.webelexis").put("server-control", Patients.CONTROL_ADDR), RegHandler(func))
    }
    data class RegSpec(val addr: String, val rest: String, val role: String, val method: String)

    class RegHandler(val func: RegSpec) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.failed()) {
                Patients.log.error("could not register ${func.addr} for ${func.rest}: ${result.cause()}")
            } else {
                if ("ok" == result.result().body().getString("status")) {
                    Patients.log.debug("registered ${func.addr}")
                } else {
                    Patients.log.error("registering of ${func.addr} failed: ${result.result().body().getString("message")}")
                }
            }
        }

    }
}