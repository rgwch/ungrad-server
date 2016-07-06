package main.java.ch.elexis.ungrad.server

import ch.rgw.tools.CmdLineParser
import ch.rgw.tools.Configuration
import ch.rgw.tools.net.NetTool
import com.hazelcast.config.Config
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager


/**
 * Created by gerry on 06.07.16.
 */

val config=Configuration()
var vertx:Vertx?=null
var ip:String=""

fun main(args:Array<String>){
    var mothersID=""
    var cmdline = CmdLineParser(switches = "client,ip,rescan,config,daemon")
    if (!cmdline.parse(args)) {
        println(cmdline.errmsg)
        System.exit(-1)
    }

    val client = cmdline.parsed.containsKey("client")
    if (cmdline.parsed.containsKey("config")) {
        config.merge(Configuration(cmdline.get("config")))
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
        vertx = result.result()
        if (cmdline.parsed.containsKey("config")) {
            config.merge(Configuration(cmdline.get("config")))
        } else {
            config.merge(Configuration("default.cfg", "user.cfg"))
        }

    }
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            println("Shutdown signal received")
            vertx?.undeploy(mothersID)
            vertx?.close()
        }
    })

}