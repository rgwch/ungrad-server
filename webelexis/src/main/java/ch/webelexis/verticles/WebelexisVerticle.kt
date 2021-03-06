/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 */
package ch.webelexis.verticles

import ch.rgw.tools.json.json_create
import ch.rgw.tools.json.json_error
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLConnection
import org.slf4j.Logger

/**
 * Created by gerry on 15.08.16.
 */
abstract class WebelexisVerticle(val ID: String, val CONTROL_ADDR: String) : AbstractVerticle() {
    val API = "1.0"
    val log: Logger = org.slf4j.LoggerFactory.getLogger("Webelexis")
    val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
    /*
    val database: AsyncSQLClient by lazy {
        log.debug ("creating database " + config().encodePrettily())
        if (config().getString("type", "mysql") == "mysql") {
            MySQLClient.createShared(vertx, config())
        } else {
            PostgreSQLClient.createShared(vertx, config())
        }
    }
*/
    override fun stop(stopResult: Future<Void>) {
        database.close() { result ->
            if (result.succeeded()) {
                stopResult.complete()
                log.info("${getName()} stopped.")
            } else {
                stopResult.fail(result.cause())
            }
        }
    }

    override fun start() {
        super.start()
        val eb = vertx.eventBus()
        database = MySQLClient.createNonShared(vertx, config())

        eb.consumer<JsonObject>(CONTROL_ADDR) { msg ->
            when (msg.body().getString("command")) {
                "getParam" -> {
                    val result = getParam(msg)
                    result.setHandler { async ->
                        if (async.succeeded()) {
                            msg.reply(async.result())
                        } else {
                            msg.reply(json_error(async.cause().message))
                        }
                    }
                }
                "setParam" -> {

                }
                "exec" -> {

                }
            }
        }

    }

    abstract fun createParams(): JsonArray
    abstract fun getName(): String
    abstract fun getParam(msg: Message<JsonObject>): Future<JsonObject>


    override fun stop() {
        log.info(getName() + " stopped.")
        database.close()
    }

    fun sendQuery(query: String, handler: ((AsyncResult<List<JsonArray>>) -> Unit)) {
        log.debug("got query: " + query)
        try {
            database.getConnection { con ->
                if (con.succeeded()) {
                    val conn = con.result()
                    conn.query(query) { result ->
                        if (result.succeeded()) {
                            val rs = result.result()
                            handler(Future.succeededFuture<List<JsonArray>>(rs.results))
                        } else {
                            log.error("query error ", result.cause())
                            handler(Future.failedFuture(result.cause()))
                        }
                        conn.close()
                    }
                } else {
                    log.error("connection error ", con.cause())
                    handler(Future.failedFuture(con.cause()))
                }
            }
        } catch(rh: Throwable) {
            log.error("error accessing database", rh)
        }
    }

    fun getConnection(msg: Message<JsonObject>, handler: ((AsyncResult<SQLConnection>) -> Unit)) {
        database.getConnection() { con ->
            if (con.succeeded()) {
                handler.invoke(con)
            } else {
                log.error("could not connect to database", con.cause())
                msg.reply(json_error("message:internal server error: database"))
            }
        }
    }

    fun register(func: RegSpec) {
        vertx.eventBus().send(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server", json_create("id:$ID", "name:${getName()}", "address:$CONTROL_ADDR").put("params", createParams())))

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

    companion object {
        lateinit var database: AsyncSQLClient
    }

}