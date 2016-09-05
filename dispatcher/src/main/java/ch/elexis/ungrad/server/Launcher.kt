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
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.LoggerFactory
import java.io.File


/**
 * Created by gerry on 06.07.16.
 */

val config: JsonUtil by lazy {
    JsonUtil.load("default.json").mergeIn(JsonUtil.load("user.json")) as JsonUtil
}
var ip: String = ""
val log = LoggerFactory.getLogger("Ungrad Launcher")
val executionMode: String by lazy{
    config.getString("mode","release")
}
val persistor=JsonFilePersistor()

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
            .setBlockedThreadCheckInterval(if(executionMode == "debug")300000L else 3000L)

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
            vertx.deployVerticle(Restpoint(persistor), DeploymentOptions().setConfig(config)) { rpResult ->
                if (rpResult.succeeded()) {
                    restpointID = rpResult.result()
                    log.info("Launched Restpoint")
                    config.getArray("launch", JsonArray()).forEach {
                        val jo = it as JsonObject
                        vertx.eventBus().send<JsonObject>(Restpoint.ADDR_LAUNCH, jo) { answer ->
                            if(answer.failed()){
                                log.error("failed launching ${jo.getString("name")}: ${answer.cause()}")
                            }
                        }
                    }
                } else {
                    log.error("Could not launch Restpoint: ${rpResult.cause().message}")
                }
            }
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    println("Shutdown signal received")
                    vertx.undeploy(restpointID){stopresult ->
                        if(stopresult.failed()){
                            log.error("graceful shutdown failed")
                        }
                        vertx.close()
                    }
                 }
            })
        } else {
            log.error("Could not set up vertx cluster: ${result.cause().message}")
        }

    }

}
