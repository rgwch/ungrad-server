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
import io.vertx.core.*
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
import java.io.File
import java.net.URL
import java.net.URLClassLoader
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
    val servers = HashMap<String, JsonObject>()
    val log = LoggerFactory.getLogger("Restpoint")
    val params = "\\/:[a-z]+".toRegex()
    val verticles = HashMap<String, String>()
    val router: Router by lazy {
        Router.router(vertx)
    }

    override fun start(future: Future<Void>) {
        super.start()
        vertx.eventBus().consumer<JsonObject>(ADDR_REGISTER) { msg ->
            val j = JsonUtil(msg.body())
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
                        servers.put(server.getString("id"), server)
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
        }
        vertx.eventBus().consumer<JsonObject>(ADDR_LAUNCH) { msg ->
            val jo = msg.body()
            val verticle_name = jo.getString("name")
            val verticle_url = jo.getString("url")
            val verticle_config = jo.getJsonObject("config", JsonObject())
            val verticle_class:String?=jo.getString("verticle")
            val url = URL(verticle_url)
            val file = File(url.file)
            if (!file.exists() || !file.canRead()) {
                ch.elexis.ungrad.server.log.error("can't read ${file.absolutePath}")
            } else {
                try {
                    val options = DeploymentOptions().setConfig(verticle_config)
                    if(verticle_class==null){
                        vertx.deployVerticle(file.absolutePath,options){ handler ->
                            if(handler.succeeded()){
                                verticles.put(verticle_name,handler.result())
                                log.info("launched uncompiled Verticle ${verticle_name}")
                                msg.reply(JsonUtil.ok())
                            }else{
                                log.error(" *** could not launch ${verticle_name}", handler.cause())
                                msg.fail(1, handler.cause().message)
                            }
                        }
                    }else {
                        val cl = URLClassLoader(Array<URL>(1, { url }))
                        val clazz = cl.loadClass(verticle_class)
                        val verticle = clazz.newInstance()
                        vertx.deployVerticle(verticle as Verticle, options) { handler ->
                            if (handler.succeeded()) {
                                verticles.put(verticle_name, handler.result())
                                log.info("launched ${verticle_name}")
                                msg.reply(JsonUtil.ok())
                            } else {
                                log.error(" *** could not launch ${verticle_name}", handler.cause())
                                msg.fail(1, handler.cause().message)
                            }
                        }
                    }

                } catch(ex: Exception) {
                    log.error(" *** Could not launch Verticle ${verticle_url}", ex)
                    msg.fail(2, ex.message)
                }
                /*
                if (verticle_url.endsWith(".jar")) {
                    try {
                        val cl = URLClassLoader(Array<URL>(1, { url }))
                        val clazz = cl.loadClass(jo.getString("verticle"))
                        val verticle = clazz.newInstance()
                        val options = DeploymentOptions().setConfig(verticle_config)
                        vertx.deployVerticle(verticle as Verticle, options) { handler ->
                            if (handler.succeeded()) {
                                verticles.put(verticle_name, handler.result())
                                log.info("launched ${verticle_name}")
                                msg.reply(JsonUtil.create("status:ok"))
                            } else {
                                log.error(" *** could not launch ${verticle_name}", handler.cause())
                                msg.fail(1, handler.cause().message)
                            }
                        }

                    } catch(ex: Exception) {
                        log.error(" *** Could not launch Verticle ${verticle_url}", ex)
                        msg.fail(2, ex.message)
                    }
                } else {
                    try {
                        val cl=URLClassLoader(Array<URL>(1,{url}))
                        val clazz=cl.loadClass(jo.getString("verticle"))
                        val verticle=clazz.newInstance()
                        val options=DeploymentOptions().setConfig(verticle_config)
                        vertx.deployVerticle(verticle as Verticle, options) { handler ->
                            if (handler.succeeded()) {
                                verticles.put(verticle_name, handler.result())
                                log.info("launched ${verticle_name}")
                                msg.reply(JsonUtil.create("status:ok"))
                            } else {
                                log.error(" *** could not launch ${verticle_name}", handler.cause())
                                msg.fail(1, handler.cause().message)

                            }
                        }
                    }catch(ex:Exception){
                        log.error(" *** Could not launch Verticle ${verticle_url}", ex)
                        msg.fail(2, ex.message)

                    }
                }
                */
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
                        result.add(it.value)
                    }
                    context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encode(result))
                }
            }
            router.get("/api/services/:service/getParam/:name").handler { ctx ->
                checkAuth(ctx, "admin") {
                    val serverID = ctx.request().getParam("service")
                    val serviceDesc = servers.get(serverID)
                    if (serviceDesc != null) {
                        val serviceAddr = serviceDesc.getString("address")
                        vertx.eventBus().send<JsonObject>(serviceAddr, JsonUtil.create("command:getParam", "param:${ctx.request().getParam("name")}"), ResultHandler(ctx))
                    } else {
                        ctx.response().setStatusCode(404).end("${serverID} not found")
                    }

                }
            }
            router.post("/api/services/setParam").handler { ctx ->
                checkAuth(ctx, "admin") {
                    ctx.request().bodyHandler { buffer ->
                        val cmd = buffer.toJsonObject()
                        val serverID = cmd.getString("service")
                        val serviceDesc = servers.get(serverID)
                        if (serviceDesc != null) {
                            val serviceAddr = serviceDesc.getString("address")
                            cmd.put("command","setParam")
                            vertx.eventBus().send<JsonObject>(serviceAddr, cmd, ResultHandler(ctx))
                        } else {
                            ctx.response().setStatusCode(404).end("${serverID} not found")
                        }
                    }
                }
            }
            router.get("/api/services/:service/exec/:action").handler{ ctx ->
                checkAuth(ctx, "admin"){
                    val serverID = ctx.request().getParam("service")
                    val serviceDesc = servers.get(serverID)
                    if (serviceDesc != null) {
                        val serviceAddr = serviceDesc.getString("address")
                        vertx.eventBus().send<JsonObject>(serviceAddr, JsonUtil.create("command:exec", "action:${ctx.request().getParam("action")}"), ResultHandler(ctx))
                    } else {
                        ctx.response().setStatusCode(404).end("${serverID} not found")
                    }
                }

            }
            // calls to other resources go to the web interface
            router.route("/ui/*").handler(UIHandler(cfg))
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

    override fun stop() {
        verticles.forEach {
            vertx.undeploy(it.value)
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
        val ADDR_LAUNCH = "ch.elexis.ungrad.server.launch"
        val AUTH_ERR = "bad username or password"
    }

    /**
     * This is the result from the service via EventBus: Tha last checkpoint for results before sending to the
     * REST client.
     */
    class ResultHandler(val ctx: RoutingContext) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.succeeded()) {
                // message returned technically correct but might still indicate an error condition
                val msg = result.result().body()
                if ("ok" == msg.getString("status")) {
                    // request completed successfully - send answer to the client.
                    ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(result.result().body().encode())
                } else {
                    // some error while processing the request. The service hopefully provided a meaningful error message
                    log.error(result.result().body().encodePrettily(), result.cause())
                    ctx.response().setStatusCode(400).putHeader("content-type", "text/plain; charset=utf-8")
                            .end(result.result().body().encodePrettily())
                }
            } else {
                // message was not handled or returned correctly -> internal server error
                log.error(result?.result()?.body()?.encodePrettily() ?: "error", result?.cause())
                ctx.response().setStatusCode(500).end(result.cause().message)
            }
        }

    }
}