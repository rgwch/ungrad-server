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


package ch.rgw.ungrad_backup

import ch.rgw.io.FileTool
import ch.rgw.tools.json.get
import ch.rgw.tools.json.json_create
import ch.rgw.tools.json.json_ok
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Created by gerry on 02.12.16.
 */
class Verticle : AbstractVerticle() {
    val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
    val BASE_ADDRESS = "ch.rgw.ungrad.backup"
    val CONTROL_ADDRESS = BASE_ADDRESS + ".admin"
    val ID = "ch.rgw.ungrad.backup"
    val API = "1.0"
    val FUNC_REDUCE = RegSpec(BASE_ADDRESS + ".reduce", "/reduce/:sourcedir/:destfile", "admin", "GET")
    val FUNC_CHOP = RegSpec(BASE_ADDRESS + ".chop", "/chop/:sourcefile/:destdir/:size/:pwd", "admin", "GET")
    val FUNC_UNCHOP = RegSpec(BASE_ADDRESS + ".unchop", "/unchop/:sourcefile/:destdir/:pwd", "admin", "GET")
    val FUNC_SCP_SEND = RegSpec(BASE_ADDRESS + "scp-send", "/scp/:file", "admin", "GET")
    val FUNC_SCP_FETCH = RegSpec(BASE_ADDRESS + "scp-fetch", "/scp-get/:file", "admin", "GET")
    val FUNC_GLACIER_SEND = RegSpec(BASE_ADDRESS + "glacier-send", "/glacier-send/:file", "admin", "GET")
    val FUNC_GLACIER_FETCH = RegSpec(BASE_ADDRESS + "gacier-fetch", "/glacier-fetch/:file", "admin", "GET")


    override fun start() {
        regexec(FUNC_SCP_SEND, { msg ->
            execute({ Scp(config()).transmit(File(msg.body()["file"])) }, msg)
        })
        regexec(FUNC_SCP_FETCH, { msg ->
            execute({ Scp(config()).fetch(msg.body()["filename"]!!, File(msg.body()["destDir"])) }, msg)
        })
        regexec(FUNC_CHOP, { msg ->
            val parm = msg.body()
            execute({ Chopper().chop(File(parm["sourcefile"]), File(parm["destdir"]), parm.getLong("size"), parm["pwd"]!!.toByteArray()) }, msg)
        })
        regexec(FUNC_UNCHOP, { msg ->
            val parm = msg.body()
            execute({ Chopper().unchop(File(parm["sourcefile"]), File(parm["destdir"]), parm["pwd"]!!.toByteArray()) }, msg)
        })
        regexec(FUNC_REDUCE, { msg ->
            val parm = msg.body()
            execute({
                Reducer().reduce(File(parm["directory"]), File(parm["archive"]), object : Comparator<File> {
                    override fun compare(f1: File, f2: File): Int {
                        return f1.lastModified().compareTo(f2.lastModified())
                    }

                })
            }, msg)
        })
        regexec(FUNC_GLACIER_SEND) { msg ->
            val parm = msg.body()
            execute({ Glacier(config()).transmit(parm["vault"]!!, File(parm["file"])) }, msg)
        }

        register(FUNC_GLACIER_FETCH)
        vertx.eventBus().consumer<JsonObject>(FUNC_GLACIER_FETCH.addr) { msg ->
            val parm = msg.body()
            Glacier(config()).asyncFetch(parm["vault"]!!, parm["archiveID"]!!, object : AsyncResultHandler<InputStream> {
                override fun handle(result: AsyncResult<InputStream>) {
                    if (result.succeeded()) {
                        val out = File(parm["destFile"])
                        FileTool.copyStreams(result.result().buffered(), out.outputStream())
                        result.result().close()
                    }
                }

            })
            msg.reply(json_ok())
        }
    }

    fun execute(func: () -> JsonObject, msg: Message<JsonObject>) {
        vertx.executeBlocking<JsonObject>(object : Handler<Future<JsonObject>> {
            override fun handle(result: Future<JsonObject>) {
                val res = func()
                result.complete(res)
            }

        }, object : Handler<AsyncResult<JsonObject>> {
            override fun handle(result: AsyncResult<JsonObject>) {
                msg.reply(result.result())
            }
        })
    }

    fun register(func: RegSpec) {
        vertx.eventBus().send(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "${API}/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server", json_create("id:$ID", "name:Backup", "address:$CONTROL_ADDRESS").put("params", createParams())))

    }

    fun regexec(func: RegSpec, handler: (Message<JsonObject>) -> Unit) {
        register(func)
        vertx.eventBus().consumer<JsonObject>(func.addr, handler)

    }

    data class RegSpec(val addr: String, val rest: String, val role: String, val method: String)

    fun createParams(): JsonArray {
        return JsonArray()
    }
}