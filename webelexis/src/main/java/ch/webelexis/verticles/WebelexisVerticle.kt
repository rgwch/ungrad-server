package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import com.hazelcast.logging.LoggerFactory
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import io.vertx.ext.sql.SQLConnection
import org.slf4j.Logger

/**
 * Created by gerry on 15.08.16.
 */
abstract class WebelexisVerticle(val ID:String, val CONTROL_ADDR:String) : AbstractVerticle(){
    val API = "1.0"
    val log : Logger=org.slf4j.LoggerFactory.getLogger("Webelexis")
    val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"


    val database: AsyncSQLClient by lazy {
        log.debug ("creating database " + config().encodePrettily())
        if (config().getString("type", "mysql") == "mysql") {
            MySQLClient.createShared(vertx, config())
        } else {
            PostgreSQLClient.createShared(vertx, config())
        }
    }

    override fun start(){
        super.start()
        val eb=vertx.eventBus()
        vertx.eventBus().consumer<JsonObject>(CONTROL_ADDR){msg ->
            when(msg.body().getString("command")){
                "getParam" -> {
                    val result=getParam(msg)
                    result.setHandler{ async ->
                        if(async.succeeded()){
                            msg.reply(async.result())
                        }else{
                            msg.reply(JsonUtil.create("status:error","message:${async.cause().message}"))
                        }
                    }
                }
                "setParam" -> {

                }
                "exec" ->{

                }
            }
        }

    }

    abstract fun createParams(): JsonArray
    abstract fun getName():String
    abstract fun getParam(msg: Message<JsonObject>): Future<JsonObject>
    

    override fun stop() {
        Patients.log.info("stop")
        database.close()
    }
    fun sendQuery(query:String, handler:((AsyncResult<List<JsonArray>>) -> Unit)){
        log.debug("got query: "+query)
        database.getConnection { con ->
            if(con.succeeded()){
                val conn=con.result()
                conn.query(query){ result ->
                    if(result.succeeded()){
                        val rs=result.result()
                        handler(Future.succeededFuture<List<JsonArray>>(rs.results))
                    }else{
                        log.error("query error ",result.cause())
                        handler(Future.failedFuture(result.cause()))
                    }
                    conn.close()
                }
            }else{
                log.error("connection error ",con.cause())
                handler(Future.failedFuture(con.cause()))
            }
        }
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
        vertx.eventBus().send(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server",JsonUtil.create("id:$ID","name:${getName()}","address:$CONTROL_ADDR").put("params",createParams())))

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