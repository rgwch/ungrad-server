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
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServer
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

class Restpoint(val persist:IPersistor) : AbstractVerticle() {
    val API = "1.0"
    val log = LoggerFactory.getLogger("Restpoint")
    val verticles = HashMap<String, String>()
    val router: Router by lazy {
        Router.router(vertx)
    }
    val registrar=Registrar(this)
    val launchManager=LaunchManager(this)
    lateinit var httpServer:HttpServer

    override fun start(future: Future<Void>) {
        super.start()
        vertx.eventBus().consumer<JsonObject>(ADDR_REGISTER, registrar)
        vertx.eventBus().consumer<JsonObject>(ADDR_LAUNCH, launchManager)
        vertx.eventBus().consumer<JsonObject>(ADDR_LOG){msg->
            when(msg.body().getString("level")){
                "debug" -> log.debug(msg.body().getString("message"))
                "info" ->log.info(msg.body().getString("message"))
                "warn" ->log.warn(msg.body().getString("message"))
                "error"->log.error(msg.body().getString("message"))
                else -> log.error("bad option for logging ")
            }
        }
        vertx.eventBus().consumer<JsonObject>(ADDR_CONFIG_GET){msg ->
            msg.reply(persist.read(msg.body().getString("id")))
        }
        vertx.eventBus().consumer<JsonObject>(ADDR_CONFIG_SET){
            persist.write(it.body().getString("id"),JsonUtil(it.body().getJsonObject("value")))
        }
        /*
         * Create the Router and HTTP Server and add some handlers for session management and authentication/authorizastion
         */
        try {
            router.route().handler(io.vertx.ext.web.handler.CookieHandler.create())
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)))
            router.route().handler(io.vertx.ext.web.handler.UserSessionHandler.create(authProvider))
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
                registrar.checkAuth(context, "admin") {
                    val result = JsonArray()
                    registrar.servers.forEach {
                        result.add(it.value)
                    }
                    context.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(Json.encode(result))
                }
            }
            router.get("/api/services/:service/getParam/:name").handler { ctx ->
                registrar.checkAuth(ctx, "admin") {
                    val serverID = ctx.request().getParam("service")
                    val serviceDesc = registrar.servers[serverID]
                    if (serviceDesc != null) {
                        val serviceAddr = serviceDesc.getString("address")
                        vertx.eventBus().send<JsonObject>(serviceAddr, JsonUtil.create("command:getParam", "param:${ctx.request().getParam("name")}"), ResultHandler(ctx))
                    } else {
                        ctx.response().setStatusCode(404).end("${serverID} not found")
                    }

                }
            }
            router.post("/api/services/setParam").handler { ctx ->
                registrar.checkAuth(ctx, "admin") {
                    ctx.request().bodyHandler { buffer ->
                        val cmd = buffer.toJsonObject()
                        val serverID = cmd.getString("service")
                        val serviceDesc = registrar.servers[serverID]
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
                registrar.checkAuth(ctx, "admin"){
                    val serverID = ctx.request().getParam("service")
                    val serviceDesc = registrar.servers[serverID]
                    if (serviceDesc != null) {
                        val serviceAddr = serviceDesc.getString("address")
                        vertx.eventBus().send<JsonObject>(serviceAddr, JsonUtil.create("command:exec", "action:${ctx.request().getParam("action")}"), ResultHandler(ctx))
                    } else {
                        ctx.response().setStatusCode(404).end("${serverID} not found")
                    }
                }

            }
            // calls to other resources go to the web interface
            router.route("/ui/*").handler(UIHandler(config()))
            val hso = HttpServerOptions().setCompressionSupported(true).setIdleTimeout(0).setTcpKeepAlive(true)
            httpServer=vertx.createHttpServer(hso)
                    .requestHandler { request -> router.accept(request) }
                    .listen(JsonUtil(config()).getOptional("rest_port", 2016)) {
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

    /**
     * Stop all Verticles previously launched by this RestPoint.
     */
    override fun stop(stopResult:Future<Void>) {
        val futures=ArrayList<Future<Void>>()
        httpServer.close()
        for((name, deploymentID) in verticles){
            val undeployResult= Future.future<Void>()
            log.info("stopping ${name} - ${deploymentID}")
            futures.add(undeployResult)
            vertx.undeploy(deploymentID,undeployResult.completer())

        }
        if(futures.isEmpty()){
            stopResult.complete()
        }else {
            CompositeFuture.all(futures as List<Future<*>>?).setHandler { result ->
                if (result.succeeded()) {
                    stopResult.complete()
                } else {
                    log.error("fail: ${result.toString()}; ${result.result()}; ${result.cause().message}")
                    stopResult.fail(result.cause())
                }
            }
        }
    }

    fun sayError(ctx: RoutingContext, errno: Int, errmsg: String) {
        ctx.response().setStatusCode(errno).end(errmsg)
    }



    companion object {
        val ADDR_REGISTER = "ch.elexis.ungrad.server.register"
        val ADDR_LAUNCH = "ch.elexis.ungrad.server.launch"
        val ADDR_CONFIG_GET="ch.elexis.ungrad.server.getconfig"
        val ADDR_CONFIG_SET="ch.elexis.ungrad.server.setconfig"
        val ADDR_LOG="ch.elexis.ungrad.server.log"
        val AUTH_ERR = "bad username or password"
        val authProvider: AccessController by lazy {
            val users = config.getJsonObject("users") ?: JsonObject()
            AccessController(users)
        }
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
                if ("ok" == msg["status"]) {
                    // request completed successfully - send answer to the client.
                    ctx.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
                            .end(msg.encode())
                } else {
                    // some error while processing the request. The service hopefully provided a meaningful error message
                    log.error(result.result().body().encodePrettily(), result.cause())
                    ctx.response().setStatusCode(400).putHeader("content-type", "text/plain; charset=utf-8")
                            .end(msg.encodePrettily())
                }
            } else {
                // message was not handled or returned correctly -> internal server error
                log.error(result.result().body()?.encodePrettily() ?: "error", result.cause())
                ctx.response().setStatusCode(500).end(result.cause().message)
            }
        }

    }
}