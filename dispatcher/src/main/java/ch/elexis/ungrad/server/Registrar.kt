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

package ch.elexis.ungrad.server

import ch.rgw.tools.json.*
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.util.*

/**
 * Created by gerry on 05.09.16.
 */
class Registrar(val restPoint:Restpoint): Handler<Message<JsonObject>> {
    val handlers = HashMap<String, String>()
    val servers = HashMap<String, JsonObject>()
    val params = "\\/:[a-z]+".toRegex()


    override fun handle(msg: Message<JsonObject>) {

        val j = msg.body()
        if (!j.validate("rest:string", "method:string", "ebaddress:string")) {
            msg.reply(JsonUtil.create("status:error", "message:format error of register message " + j.encodePrettily()))
        } else {
            val rest = j.getString("rest")
            val ebmsg = j.getString("ebaddress")
            val method = j.getString("method")
            val role = j.getString("role") ?: "guest"
            val server = j.getJsonObject("server")
            if (server != null) {
                if (JsonUtil(server).validate("id:string", "name:string", "address:string")) {
                    servers.put(server["id"]!!, server)
                } else {
                    msg.reply(JsonUtil.create("status:error", "message:format error of server definition " + j.encodePrettily()))
                    log.error("message:format error of server definition " + j.encode())

                }
            }

            if (handlers.containsKey(rest)) {
                msg.reply(JsonUtil.create("status:error", "message:REST address already registered"))
            } else if (handlers.containsValue(ebmsg)) {
                msg.reply(JsonUtil.create("status:error", "message:EventBus address already registered"))
            } else {
                handlers.put(rest, ebmsg)
                log.debug("registering /api/${rest} for ${ebmsg}")
                when (method) {
                    "get" -> restPoint.router.get("/api/${rest}").handler { context ->
                        checkAuth(context, role) {
                            val cmd = JsonObject()
                            params.findAll(rest).forEach { match ->
                                val parm = match.value.substring(2)
                                cmd.put(parm, context.request().getParam(parm))
                            }
                            restPoint.vertx.eventBus().send<JsonObject>(ebmsg, cmd, Restpoint.ResultHandler(context))
                        }

                    }
                    "post" -> restPoint.router.post("/api/${rest}").handler { context ->
                        context.request().bodyHandler { buffer ->
                            checkAuth(context, role) {
                                val cmd = buffer.toJsonObject()
                                restPoint.vertx.eventBus().send<JsonObject>(ebmsg, cmd, Restpoint.ResultHandler(context))
                            }
                        }
                    }
                }
                msg.reply(JsonUtil.create("status:ok"))
            }
        }
    }
    /**
     * Check if the current context's user has the given role. If so, call action(), if not, send an error message.
     */
    fun checkAuth(context: RoutingContext, role: String, action: () -> Unit) {
        if (context.user() == null) {
            if (config.getString("mode", "prod") == "debug") {
                action()
            } else {
                context.response().setStatusCode(401).end("please login first")
            }
        } else {
            context.user().isAuthorised(role) {
                if (it.succeeded()) {
                    if (it.result()) {
                        action()
                    } else {
                        // not authorized
                        context.response().setStatusCode(403).end("Not authorized for ${role}.")
                    }
                } else {
                    //internal server error
                    context.response().setStatusCode(500).end(it.result().toString())
                }
            }
        }
    }
}