/*******************************************************************************
 * Copyright (c) 2016-2017 by G. Weirich
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

import RegSpec
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



class Communicator(val dispatcher:Dispatcher) : AbstractVerticle() {

    val eb by lazy{
        vertx.eventBus()
    }

    override fun start(startResult: Future<Void>) {

        eb.send<JsonObject>("ch.elexis.ungrad.server.getconfig", json_create("id:ch.rgw.lucinda.lucindaConfig")) { reply ->
            if (reply.succeeded()) {
                lucindaConfig.mergeIn(reply.result().body())
            } else {
                log.error("could not retrieve configuration")
            }
        }
        register(serverDesc, FUNC_IMPORT,FUNC_FINDFILES, FUNC_GETFILE, FUNC_INDEX, FUNC_UPDATE, FUNC_PING)
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
            if (!checkRequired(j, "payload")) {
                message.reply(makeJson("status:error", "message:bad parameters for ${FUNC_IMPORT.addr}"))
            } else {
                if (j.getBoolean("dry-run")) {
                    j.put("status", "ok").put("method", "import")
                    message.reply(j)
                } else {
                    try {
                        if (dispatcher.addToIndex(j)) {
                            log.info("imported ${j.getString("url")}")
                            message.reply(JsonObject().put("status", "ok").put("_id", j.getString("_id")))
                        } else {
                            log.warn("failed to import ${j.getString("url")}")
                            message.reply(JsonObject().put("status", "fail").put("_id", j.getString("_id")).put("message", "import failed"))
                        }

                    } catch(e: Exception) {
                        e.printStackTrace()
                        log.error("import failed " + e.message)
                        fail("", e)
                    }
                }
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

        startResult.complete()

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




    companion object {
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

