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

import ch.rgw.tools.CmdLineParser
import ch.rgw.tools.JsonUtil
import ch.rgw.tools.net.NetTool
import com.hazelcast.config.Config
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*


/**
 * Created by gerry on 06.07.16.
 */

val config: JsonUtil by lazy {
    JsonUtil.load("user.json", "user.cfg", "default.json", "default.cfg")
}
var ip: String = ""
val log = LoggerFactory.getLogger("Ungrad Launcher")
val authProvider: AccessController by lazy {
    val users = config.getJsonObject("users") ?: JsonObject()
    AccessController(users)
}
val verticles = HashMap<String, String>()

fun main(args: Array<String>) {
    var restpointID = ""
    var cmdline = CmdLineParser(switches = "ip,config,daemon")
    if (!cmdline.parse(args)) {
        println(cmdline.errmsg)
        System.exit(-1)
    }
    if (cmdline.parsed.containsKey("config")) {
        val file = File(cmdline.get("config"))
        if (file.exists() && file.canRead()) {
            log.info("replacing default configuration with ${file.absolutePath}")
            config.replace(JsonUtil.createFromFile(file))
        } else {
            log.error("tried to replace config with ${file.absolutePath}, but could not read.")
        }
    }


    val net = cmdline.get("ip")
    ip = if (net.isEmpty()) {
        ""
    } else {
        NetTool.getMatchingIP(net) ?: ""
    }
    val hazel = Config()
    val vertxOptions = VertxOptions().setClustered(true)
            .setMaxEventLoopExecuteTime(5000000000L)
            .setBlockedThreadCheckInterval(2000L)

    if (ip.isNotEmpty()) {
        val network = hazel.networkConfig
        network.interfaces.setEnabled(true).addInterface(ip)
        network.publicAddress = ip
        vertxOptions.setClusterHost(ip)
    }
    val mgr = HazelcastClusterManager(hazel)

    Vertx.clusteredVertx(vertxOptions.setClusterManager(mgr)) { result ->
        if (result.succeeded()) {
            val vertx = result.result()
            vertx.deployVerticle(Restpoint(config)) { rpResult ->
                if (rpResult.succeeded()) {
                    fun deployResult(isOk: Boolean, message: String): Unit {
                        if (isOk) {
                            log.info(message)
                        } else {
                            log.error(message)
                        }
                    }

                    restpointID = rpResult.result()
                    log.info("Launched Restpoint")
                    ch.elexis.ungrad.server_test.start(vertx, config, ::deployResult)
                    ch.rgw.lucinda.start(vertx, config, ::deployResult)
                    config.getArray("launch", JsonArray()).forEach { launcher ->
                        try {

                            val jo = launcher as JsonObject
                            //val cl = UngradClassLoader(jo.getString("url"))
                            //val completeURL=jo.getString("url")+"!/"+jo.getString("verticle").replace("\\.".toRegex(),"/")+".class"
                            val url = URL(jo.getString("url"))
                            val file = File(url.file)
                            if (!file.exists() || !file.canRead()) {
                                log.error("can't read ${file.absolutePath}")
                            }

                            val cl = URLClassLoader(Array<URL>(1, { url }))
                            val clazz = cl.loadClass(jo.getString("verticle"))
                            val verticle = clazz.newInstance()
                            val options = DeploymentOptions().setConfig(jo.getJsonObject("config", JsonObject()))
                            vertx.deployVerticle(verticle as Verticle, options) { handler ->
                                if (handler.succeeded()) {
                                    verticles.put(jo.getString("name"), handler.result())
                                    log.info("launched ${jo.getString("name")}")
                                } else {
                                    log.error("could not launch ${jo.getString("name")}", handler.cause())
                                }
                            }


                        } catch(ex: Exception) {
                            log.error("could not launch ${launcher.toString()}", ex)
                        }
                    }
                } else {
                    log.error("Could not launch Verticles: ${rpResult.cause().message}")
                }
            }
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    println("Shutdown signal received")
                    vertx.undeploy(restpointID)
                    ch.rgw.lucinda.stop()
                    ch.elexis.ungrad.server_test.stop()
                    vertx.close()
                }
            })
        } else {
            log.error("Could not set up vertx cluster: ${result.cause().message}")
        }

    }

}
