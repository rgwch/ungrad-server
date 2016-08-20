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

import ch.rgw.tools.JsonUtil
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.RedirectAuthHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import org.slf4j.LoggerFactory
import java.util.*

/**
 * The Restpoint is a central REST interface for modules of the ungrad-server. A Module registers itself by
 * sending one or more messages to Restpoint.ADDR_REGISTER with the following content:
 * - rest: The REST address to listen for. Note: The Address is always prefixes with /api/. So "1.4/my_server/push" will
 * listen for "<serveradddr>/api/1.4/my_server/push".
 * - ebaddress: The EventBus Address to send REST requests.
 * - role: user role needed to use this command
 * - server-id: A unique ID for this server
 * - server-control: Address of the control interface for this server
 * Created by gerry on 06.07.16.
 */

class Restpoint(val cfg: JsonUtil) : AbstractVerticle() {
    val API = "1.0"
    val handlers = HashMap<String, String>()
    val servers = HashMap<String, String>()
    val log = LoggerFactory.getLogger(this.javaClass)
    val params = "\\/:[a-z]+".toRegex()
    val router: Router by lazy {
        Router.router(vertx)
    }

    override fun start(future: Future<Void>) {
        super.start()
        vertx.eventBus().consumer<Message<JsonObject>>(ADDR_REGISTER) { msg ->
            val j = msg.body() as JsonObject
            val rest = j.getString("rest")
            val ebmsg = j.getString("ebaddress")
            val method = j.getString("method")
            val role = j.getString("role") ?: "guest"
            val sname = j.getString("server-id")
            val admin = j.getString("server-control")
            if (handlers.containsKey(rest)) {
                msg.reply(JsonUtil.create("status:error", "message:REST address already registered"))
            } else if (handlers.containsValue(ebmsg)) {
                msg.reply(JsonUtil.create("status:error", "message:EventBus address already registered"))
            } else {
                handlers.put(rest, ebmsg)
                if (sname != null && admin != null) {
                    servers.put(sname, admin)
                }
                log.debug("registering /api/${rest} for ${ebmsg}")
                when (method) {
                    "get" -> router.get("/api/${rest}").handler { context ->
                        checkAuth(context, role) {
                            val cmd = JsonObject()
                            params.findAll(rest).forEach { match ->
                                val parm = match.value.substring(2)
                                cmd.put(parm, context.request().getParam(parm))
                            }
                            vertx.eventBus().send<JsonObject>(ebmsg, cmd, ResultHandler(context))
                        }

                    }
                    "post" -> router.post("/api/${rest}").handler { context ->
                        context.request().bodyHandler { buffer ->
                            checkAuth(context, role) {
                                val cmd = buffer.toJsonObject()
                                vertx.eventBus().send<JsonObject>(ebmsg, cmd, ResultHandler(context))
                            }
                        }
                    }
                }
                msg.reply(JsonUtil.create("status:ok"))
            }
        }
        /*
         * Create the Router and HTTP Server and add some handlers for session management and authentication/authorizastion
         */
        try {
            router.route().handler(io.vertx.ext.web.handler.CookieHandler.create());
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
            router.route().handler(io.vertx.ext.web.handler.UserSessionHandler.create(authProvider));
            if (config.getString("mode", "prod") != "debug") {
                // if not in debug mode: pass all calls to /api/* through authentication and let users login first

                val redirectAuthHandler = RedirectAuthHandler.create(authProvider, "/login", "reTo")
                router.route("/api/*").handler(redirectAuthHandler)
            }
            router.get("/login").handler { context ->
                context.response().sendFile("login.html")
            }
            router.get("/test").handler { ctx ->
                ctx.response().end("OK")
            }
            router.post("/dologin").handler { context ->
                context.request().bodyHandler { buffer ->
                    val credentials = buffer.toString()
                    if (credentials.isNullOrBlank()) {
                        context.response().setStatusCode(400).end(AUTH_ERR)
                    } else {
                        val parms = credentials.split("&")
                        if (parms.size != 2) {
                            context.response().setStatusCode(400).end(AUTH_ERR)
                        } else {
                            val srt = context.session().get<String>("reTo")
                            authProvider.authenticate(JsonObject().put("username", parms[0].split("=")[1])
                                    .put("password", parms[1].split("=")[1])) { res ->
                                if (res.succeeded()) {
                                    val user = res.result()
                                    context.setUser(user)
                                    if (srt != null) {
                                        context.response().putHeader("Location", srt).setStatusCode(302).end()
                                    } else {
                                        context.response().putHeader("content-type", "application/json; charset=utf-8")
                                                .end(JsonUtil.create("status:ok").encode())
                                    }
                                } else {
                                    context.response().setStatusCode(901).end(AUTH_ERR)
                                }
                            }
                        }
                    }
                }

            }
            router.get("/logout").handler { context ->
                context.session().destroy()
                context.setUser(null)
                context.response().end("You are logged out")
            }
            router.get("/api/getServices").handler { context ->
                checkAuth(context, "admin") {
                    val result = JsonArray()
                    servers.forEach {
                        result.add(JsonUtil().add("name:${it.key}", "address:${it.value}"))
                    }
                    context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encode(result))
                }
            }
            router.get("/api/services/:service/:subcommand/:param").handler { ctx ->
                checkAuth(ctx, "admin") {
                    val param = ctx.request().getParam("param")
                    val serverID = ctx.request().getParam("service")
                    val service = servers.get(serverID)
                    if (service != null) {
                        when (ctx.request().getParam("subcommand")) {
                            "startService" -> vertx.eventBus().send<JsonObject>(service, JsonUtil.create("command:start"), ResultHandler(ctx))
                            "stopService" -> vertx.eventBus().send<JsonObject>(service, JsonUtil.create("command:stop"), ResultHandler(ctx))
                            "getServiceName" -> vertx.eventBus().send<JsonObject>(service, JsonUtil.create("command:getName"), ResultHandler(ctx))
                            "getParams" -> vertx.eventBus().send<JsonObject>(service, JsonUtil.create("command:getParams"), ResultHandler(ctx))
                            else -> ctx.response().setStatusCode(406).end("unknown subcommand")
                        }
                    } else {
                        ctx.response().setStatusCode(404).end("${serverID} not found")
                    }
                }
            }
            // calls to other resources go to the web interface
            router.route("/*").handler(UIHandler(cfg))
            val hso = HttpServerOptions().setCompressionSupported(true).setIdleTimeout(0).setTcpKeepAlive(true)
            vertx.createHttpServer(hso)
                    .requestHandler { request -> router.accept(request) }
                    .listen(cfg.getOptional("rest_port", 2016)) {
                        result ->
                        if (result.succeeded()) {
                            future.complete()
                        } else {
                            future.fail(result.cause())
                        }
                    }

        } catch(th: Throwable) {
            th.printStackTrace()
            log.error("Could not set up Web server ", th)
        }

    }


    fun sayError(ctx: RoutingContext, errno: Int, errmsg: String) {
        ctx.response().setStatusCode(errno).end(errmsg)
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

    companion object {
        val ADDR_REGISTER = "ch.elexis.ungrad.server.register";
        val AUTH_ERR = "bad username or password"
    }

    class ResultHandler(val ctx: RoutingContext) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.succeeded()) {
                val msg = result.result().body()
                if ("ok" == msg.getString("status")) {
                    ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(result.result().body().encode())
                } else {
                    ctx.response().setStatusCode(400).putHeader("content-type", "text/plain; charset=utf-8")
                            .end(result.result().body().encodePrettily())
                }
            } else {
                ctx.response().setStatusCode(500).end(result.cause().message)
            }
        }

    }
}