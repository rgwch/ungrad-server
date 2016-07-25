package ch.elexis.ungrad.server

import ch.rgw.tools.Configuration
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by gerry on 06.07.16.
 */

class Restpoint(val cfg: Configuration) : AbstractVerticle() {
    val handlers = HashMap<String, String>()
    val servers = HashMap<String,String>()
    val log=LoggerFactory.getLogger(this.javaClass)
    val params="\\/:[a-z]+".toRegex()

    override fun start(future:Future<Void>) {
        super.start()
        val router = Router.router(vertx)
        vertx.eventBus().consumer<Message<JsonObject>>(ADDR_REGISTER) { msg ->
            val j = msg.body() as JsonObject
            val rest = j.getString("rest")
            val ebmsg = j.getString("ebaddress")
            val method = j.getString("method")
            val sname=j.getString("server-id")
            val admin=j.getString("server-control")
            if (handlers.containsKey(rest)) {
                msg.reply(JsonObject().put("status", "error").put("message", "REST address already registered"))
            } else if (handlers.containsValue(ebmsg)) {
                msg.reply(JsonObject().put("status", "error").put("message", "EventBus address already registered"))
            } else {
                handlers.put(rest, ebmsg)
                log.debug("registering /api/${rest} for ${ebmsg}")
                when (method) {
                    "get" -> router.get("/api/${rest}").handler { context ->
                        val cmd = JsonObject()
                        params.findAll(rest).forEach { match ->
                            val parm=match.value.substring(2)
                            cmd.put(parm, context.request().getParam(parm))
                        }
                        vertx.eventBus().send<JsonObject>(ebmsg, cmd) { response ->
                            if (response.succeeded()) {
                                val res = response.result().body()
                                context.response().setStatusCode(200)
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(Json.encode(res))
                            }
                        }
                    }
                    "post" -> router.post("/api/${rest}").handler { context ->
                        context.request().bodyHandler { buffer ->
                            val cmd= buffer.toJsonObject()
                            vertx.eventBus().send<JsonObject>(ebmsg,cmd) { response ->
                                if(response.succeeded()){
                                    val res=response.result().body()
                                    context.response().setStatusCode(200)
                                            .putHeader("content-type", "application/json; charset=utf-8")
                                            .end(Json.encode(res))


                                }
                            }
                        }
                    }
                }
            }
        }
        val hso= HttpServerOptions().setCompressionSupported(true).setIdleTimeout(0).setTcpKeepAlive(true)
        vertx.createHttpServer(hso)
                .requestHandler { request -> router.accept(request) }
                .listen(cfg.get("rest_port", "2016").toInt()) {
                    result ->
                    if (result.succeeded()) {
                        future.complete()
                    } else {
                        future.fail(result.cause())
                    }
                }

    }


    companion object {
        val ADDR_REGISTER = "ch.elexis.ungrad.server.register";
    }
}