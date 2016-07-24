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


import ch.rgw.tools.Configuration
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Handler
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 20.03.16.
 */


class Communicator(cfg: Configuration) : AbstractVerticle() {
    val API = "1.0"
    val eb: EventBus by lazy {
        vertx.eventBus()
    }

    init {
        config.merge(cfg)
        indexManager=IndexManager(config.get("fs_indexdir","target/store"))
    }

    override fun start() {
        super.start()
        val dispatcher = Dispatcher(vertx)

        fun register(func: RegSpec) {
            eb.send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                    .put("ebaddress", BASEADDR + func.addr)
                    .put("rest", "${API}/${func.rest}").put("method", func.method), RegHandler(func))
        }


        register(FUNC_IMPORT)
        register(FUNC_FINDFILES)
        register(FUNC_GETFILE)
        register(FUNC_INDEX)
        register(FUNC_UPDATE)
        register(FUNC_PING)

        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_PING.addr) { reply ->
            log.info("we got a Ping!")
            val msg = reply.body().body()
            val parm = msg.getString("var")
            reply.reply(JsonObject().put("status", "ok").put("pong", parm))
        }

        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_IMPORT.addr) { message ->
            val j = message.body().body()
            log.info("got message ${FUNC_IMPORT.addr} ${j.getString("title")}")
            try {

                dispatcher.indexAndStore(j, object : Handler<AsyncResult<Int>> {
                    override fun handle(result: AsyncResult<Int>) {
                        if (result.succeeded()) {
                            log.info("imported ${j.getString("url")}")
                            message.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                        } else {
                            log.warn("failed to import ${j.getString("url")}; ${result.cause().message}")
                            message.reply(JsonObject().put("status", "fail").put("_id", j.getString("_id")).put("message", result.cause().message))
                        }
                    }

                })

                /*
                        }
               */
            } catch(e: Exception) {
                e.printStackTrace()
                log.error("import failed " + e.message)
                fail("", e)
            }

        }

        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_INDEX.addr) { msg ->
            val j = msg.body().body()
            log.info("got message ADDR_INDEX " + Json.encodePrettily(j))

            try {
                dispatcher.addToIndex(j, object : Handler<AsyncResult<Int>> {
                    override fun handle(result: AsyncResult<Int>) {
                        if (result.succeeded()) {
                            log.info("indexed ${j.getString("title")}")
                            msg.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                        } else {
                            log.warn("failed to import ${j.getString("url")}; ${result.cause().message}")
                            msg.reply(JsonObject().put("status", "fail").put("_id", j.getString("_id")).put("message", result.cause().message))
                        }
                    }

                })
                msg.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")));
            } catch(e: Exception) {
                fail("can't index", e)
            }
        }

        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_GETFILE.addr) { message ->
            val j = message.body().body()
            log.info("got message ADDR_GETFILE " + Json.encodePrettily(j))

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
        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_FINDFILES.addr) { msg ->
            val j = msg.body() as JsonObject
            log.info("got message ADDR_FINDFILES " + Json.encodePrettily(j))

            try {
                val result = dispatcher.find(j)
                msg.reply(JsonObject().put("status", "ok").put("result", result))
            } catch(e: Exception) {

                fail("", e)
            }
        }
        eb.consumer<Message<JsonObject>>(BASEADDR + FUNC_UPDATE.addr) { msg ->
            val j = msg.body() as JsonObject
            log.info("got message ADDR_UPDATE " + Json.encodePrettily(j))

            try {
                val result = dispatcher.update(j)
                msg.reply(JsonObject().put("status", "ok").put("result", result))
            } catch(e: Exception) {

                fail("", e)
            }
        }


    }

    fun success(msg: Message<Any>, result: JsonObject = JsonObject()) {
        msg.reply(result.put("status", "ok"))
    }

    fun fail(msg: String, ex: Exception? = null) {
        val j = JsonObject().put("message", msg)
        log.warn("failed " + ex)
        if (ex == null) {
            j.put("status", "failed")
        } else {
            j.put("status", "exception").put("message", ex.message)
            log.warn(ex.message + "\n\n" + ex.stackTrace.toString())
        }
        eb.send(FUNC_ERROR.addr, j)
    }


    companion object {
        const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
        const val BASEADDR = "ch.rgw.lucinda"

        /** reply address for error messages */
        val FUNC_ERROR = RegSpec(".error", "lucinda/error", "get")
        /** Add a file to the storage */
        val FUNC_IMPORT = RegSpec(".import", "lucinda/import", "post")
        /** Index a file in-place (don't add it to the storage) */
        val FUNC_INDEX = RegSpec(".index", "lucinda/index/:url", "get")
        /** Retrieve a file by _id*/
        val FUNC_GETFILE = RegSpec(".get", "lucinda/get/:id", "get")
        /** Get Metadata of files matching a search query */
        val FUNC_FINDFILES = RegSpec(".find", "lucinda/find/:spec", "get")
        /** Update Metadata of a file by _id*/
        val FUNC_UPDATE = RegSpec(".update", "lucinda/update", "post")
        /** Connection check */
        val FUNC_PING = RegSpec(".ping", "lucinda/ping/:var", "get")
        val log = LoggerFactory.getLogger("lucinda.Communicator")

        var indexManager: IndexManager? = null;
        val config: Configuration = Configuration();

    }

    class RegHandler(val func: RegSpec) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.failed()) {
                log.error("could not register ${func.addr} for ${func.rest}")
            }
        }

    }

    data class RegSpec(val addr: String, val rest: String, val method: String)

}

