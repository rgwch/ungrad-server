package ch.elexis.ungrad.server

import ch.rgw.tools.JsonUtil
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Verticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * Created by gerry on 05.09.16.
 */
class LaunchManager(val restPoint:Restpoint) : Handler<Message<JsonObject>> {
    override fun handle(msg: Message<JsonObject>) {
        val jo = msg.body()
        val verticle_name = jo.getString("name")
        val verticle_url = jo.getString("url")
        val verticle_class:String?=jo.getString("verticle")
        val static_config = jo.getJsonObject("config", JsonObject())
        val dynamic_config=restPoint.persist.read(verticle_name)
        val verticle_config= JsonUtil(static_config).mergeIn(dynamic_config)
        val url = URL(verticle_url)
        val file = File(url.file)
        if (!file.exists() || !file.canRead()) {
            ch.elexis.ungrad.server.log.error("can't read ${file.absolutePath}")
        } else {
            try {
                val options = DeploymentOptions().setConfig(verticle_config)
                if(verticle_class==null){
                    restPoint.vertx.deployVerticle(file.absolutePath,options){ handler ->
                        if(handler.succeeded()){
                            restPoint.verticles.put(verticle_name,handler.result())
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
                    restPoint.vertx.deployVerticle(verticle as Verticle, options) { handler ->
                        if (handler.succeeded()) {
                            restPoint.verticles.put(verticle_name, handler.result())
                            log.info("launched $verticle_name")
                            msg.reply(JsonUtil.ok())
                        } else {
                            log.error(" *** could not launch $verticle_name", handler.cause())
                            msg.fail(1, handler.cause().message)
                        }
                    }
                }

            } catch(ex: Exception) {
                log.error(" *** Could not launch Verticle $verticle_url", ex)
                msg.fail(2, ex.message)
            }
        }

    }

}