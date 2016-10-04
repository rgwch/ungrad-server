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
 ******************************************************************************/

package ch.rgw.lucinda

import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.json_create
import ch.rgw.tools.json.json_error
import io.vertx.core.*
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
Verticle for handling Lucinda requests from the EventBus
 */

const val API = "1.0"
const val EB_SETCONFIG = "ch.elexis.ungrad.server.setconfig"

data class RegSpec(val addr: String, val rest: String, val role: String, val method: String)

fun saveConfig() {
    Communicator.eb.send(EB_SETCONFIG, JsonObject().put("id", "ch.rgw.lucinda.lucindaConfig").put("value", lucindaConfig))
}

val lucindaConfig = JsonUtil()
val log = LoggerFactory.getLogger("Lucinda")


class Communicator : AbstractVerticle() {
    lateinit var autoScanner: String

    override fun stop(stopResult: Future<Void>) {
        indexManager.shutDown()
        vertx.undeploy(autoScanner)
        stopResult.complete()
        log.info("Lucinda Communicator stopped")
    }

    override fun start(startResult: Future<Void>) {
        val autoScannerResult = Future.future<Void>()
        val launchResult = Future.future<Void>()
        val consolidatedResult = listOf(
                autoScannerResult, launchResult
        )
        CompositeFuture.all(consolidatedResult).setHandler { result ->
            if (result.succeeded()) {
                startResult.complete()
            } else {
                startResult.fail(result.cause())
            }
        }

        super.start()
        eb = vertx.eventBus()
        lucindaConfig.mergeIn(config())
        indexManager = IndexManager(lucindaConfig.getString("fs_indexdir", "target/store"), lucindaConfig.getString("default_language", "de"))
        val dispatcher = Dispatcher(vertx, lucindaConfig.getString("fs_import", "target/store"))
        vertx.deployVerticle(Autoscanner(), DeploymentOptions().setConfig(lucindaConfig)) { result ->
            if (result.succeeded()) {
                autoScanner = result.result()
                autoScannerResult.complete()
            }
        }
        fun register(func: RegSpec) {
            eb.send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                    .put("ebaddress", BASEADDR + func.addr)
                    .put("rest", "$API/${func.rest}").put("method", func.method).put("role", func.role)
                    .put("server", serverDesc), RegHandler(func))
        }


        eb.send<JsonObject>("ch.elexis.ungrad.server.getconfig", json_create("id:ch.rgw.lucinda.lucindaConfig")) { reply ->
            if (reply.succeeded()) {
                lucindaConfig.mergeIn(reply.result().body())
            } else {
                log.error("could not retrieve configuration")
            }
        }
        register(FUNC_IMPORT)
        register(FUNC_FINDFILES)
        register(FUNC_GETFILE)
        register(FUNC_INDEX)
        register(FUNC_UPDATE)
        register(FUNC_PING)

        eb.consumer<JsonObject>(CONTROL_ADDR, Admin)

        eb.consumer<JsonObject>(BASEADDR + FUNC_PING.addr) { reply ->
            log.info("we got a Ping!")
            val msg = reply.body()
            val parm = msg.getString("var")
            reply.reply(JsonObject().put("status", "ok").put("pong", parm))
        }

        eb.consumer<JsonObject>(BASEADDR + FUNC_IMPORT.addr) { message ->
            val j = message.body()
            log.info("got message ${FUNC_IMPORT.addr} ${j.getString("title")}")
            if (!checkRequired(j, "_id", "payload", "filename")) {
                message.reply(makeJson("status:error", "message:bad parameters for ${FUNC_IMPORT.addr}"))
            } else {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "import")
                    message.reply(j)
                } else {
                    try {
                        dispatcher.indexAndStore(j, Handler<io.vertx.core.AsyncResult<kotlin.Int>> { result ->
                            if (result.succeeded()) {
                                log.info("imported ${j.getString("url")}")
                                message.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                            } else {
                                log.warn("failed to import ${j.getString("url")}; ${result.cause().message}")
                                message.reply(JsonObject().put("status", "fail").put("_id", j.getString("_id")).put("message", result.cause().message))
                            }
                        })
                    } catch(e: Exception) {
                        e.printStackTrace()
                        log.error("import failed " + e.message)
                        fail("", e)
                    }
                }
            }
        }

        eb.consumer<JsonObject>(BASEADDR + FUNC_INDEX.addr) { msg ->
            val j = msg.body()
            log.info("got message ADDR_INDEX " + Json.encodePrettily(j))
            if (checkRequired(j, "payload")) {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "index")
                    msg.reply(j)
                } else {
                    try {
                        dispatcher.addToIndex(j, Handler<io.vertx.core.AsyncResult<kotlin.Int>> { result ->
                            if (result.succeeded()) {
                                log.info("indexed ${j.getString("title")}")
                                msg.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                            } else {
                                log.warn("failed to import ${j.getString("url")}; ${result.cause().message}")
                                msg.reply(JsonObject().put("status", "fail").put("_id", j.getString("_id")).put("message", result.cause().message))
                            }
                        })
                        msg.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                    } catch(e: Exception) {
                        fail("can't index", e)
                    }
                }
            } else {
                msg.reply(makeJson("status:error", "message:bad parameters for addToIndex"))
            }
        }

        eb.consumer<JsonObject>(BASEADDR + FUNC_GETFILE.addr) { message ->
            val j = message.body()
            log.info("got message ADDR_GETFILE " + Json.encodePrettily(j))
            if (checkRequired(j, "_id")) {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "get")
                    message.reply(j)
                } else {
                    try {
                        val bytes = dispatcher.get(j.getString("_id"))
                        if (bytes == null) {
                            fail("could not read ${j.getString("url")}")
                        } else {
                            val result = JsonObject().put("status", "ok").put("result", bytes)
                            message.reply(result)
                        }

                    } catch(e: Exception) {
                        fail("could not load file; ${e.message}", e)
                    }
                }

            } else {
                message.reply(makeJson("status:error", "message:bad parameters for get"))

            }

        }
        eb.consumer<JsonObject>(BASEADDR + FUNC_FINDFILES.addr) { msg ->
            val j = msg.body()
            log.info("got message ADDR_FINDFILES " + Json.encodePrettily(j))
            if (checkRequired(j, "query")) {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "find")
                    msg.reply(j)
                } else {
                    try {
                        val result = dispatcher.find(j)
                        msg.reply(JsonObject().put("status", "ok").put("result", result))
                    } catch(e: Exception) {

                        fail("", e)
                    }
                }
            } else {
                msg.reply(makeJson("status:error", "message:bad parameters for find"))
            }
        }
        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_UPDATE.addr) { msg ->
            val j = msg.body() as JsonObject
            log.info("got message ADDR_UPDATE " + Json.encodePrettily(j))
            if (checkRequired(j, "_id", "payload")) {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "update")
                    msg.reply(j)
                } else {

                    try {
                        val result = dispatcher.update(j)
                        msg.reply(JsonObject().put("status", "ok").put("result", result))
                    } catch(e: Exception) {

                        fail("", e)
                    }
                }
            } else {
                msg.reply(makeJson("status:error", "message:bad parameters for update"))

            }
        }

        launchResult.complete()

    }

    fun makeJson(vararg args: String): JsonObject {
        val ret = JsonObject()
        for (arg in args) {
            val parm = arg.split(":")
            ret.put(parm[0], parm[1])
        }
        return ret
    }

    fun checkRequired(cmd: JsonObject, vararg required: String): Boolean {
        for (arg in required) {
            if (cmd.getString(arg).isNullOrBlank()) {
                return false
            }
        }
        return true
    }


    fun fail(msg: String, ex: Exception? = null) {
        val j = json_error(msg + (ex?.message ?: "."))
        log.warn(msg + " - " + ex?.message + "\n\n" + ex?.stackTrace.toString())
        eb.send(FUNC_ERROR.addr, j)
    }

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


    companion object {
        const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
        const val BASEADDR = "ch.rgw.lucinda"
        const val CONTROL_ADDR = BASEADDR + ".admin"

        /** reply address for error messages */
        val FUNC_ERROR = RegSpec(".error", "lucinda/error", "user", "get")
        /** Add a file to the storage */
        val FUNC_IMPORT = RegSpec(".import", "lucinda/import", "docmgr", "post")
        /** Index a file in-place (don't add it to the storage) */
        val FUNC_INDEX = RegSpec(".index", "lucinda/index/:url", "docmgr", "get")
        /** Retrieve a file by _id*/
        val FUNC_GETFILE = RegSpec(".get", "lucinda/get/:id", "user", "get")
        /** Get Metadata of files matching a search query */
        val FUNC_FINDFILES = RegSpec(".find", "lucinda/find/:spec", "user", "get")
        /** Update Metadata of a file by _id*/
        val FUNC_UPDATE = RegSpec(".update", "lucinda/update", "docmgr", "post")
        /** Connection check */
        val FUNC_PING = RegSpec(".ping", "lucinda/ping/:var", "guest", "get")

        lateinit var indexManager: IndexManager
        lateinit var eb: EventBus
        val params = """
        [
           {
                "name":"indexdir",
                "caption":"index directory",
                "type": "string",
                "value": "/some/dir",
                "writable":true
            },
            {
                "name":"datadir",
                "caption":"import directory",
                "type": "string",
                "value":"/some/dir/data",
                "writable":true
            }
        ]
        """
        val serverDesc = json_create("id:ch.rgw.lucinda", "name:Lucinda", "address:$CONTROL_ADDR")
                .put("params", JsonArray(params))
    }
}

