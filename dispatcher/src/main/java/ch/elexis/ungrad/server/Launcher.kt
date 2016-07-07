package main.java.ch.elexis.ungrad.server

import ch.elexis.ungrad.server_test.SelfTest
import ch.rgw.tools.CmdLineParser
import ch.rgw.tools.Configuration
import ch.rgw.tools.net.NetTool
import com.hazelcast.config.Config
import elexis.ungrad.server.Restpoint
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.LoggerFactory


/**
 * Created by gerry on 06.07.16.
 */

val config=Configuration()
var ip:String=""
val log=LoggerFactory.getLogger("Ungrad Launcher")

fun main(args:Array<String>){
    var restpointID=""
    var selftestID=""
    var consoleID=""
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
        if(result.succeeded()) {
            val vertx = result.result()
            if (cmdline.parsed.containsKey("config")) {
                config.merge(Configuration(cmdline.get("config")))
            } else {
                config.merge(Configuration("default.cfg", "user.cfg"))
            }
            vertx.deployVerticle(Restpoint(config)) { rpResult ->
                if (rpResult.succeeded()) {
                    restpointID=rpResult.result()
                    log.info("Launched Restpoint")
                    vertx.deployVerticle(SelfTest()) { stResult ->
                        if(stResult.succeeded()){
                            log.info("launched SelfTest")

                        }else{
                            log.error("Could not launch SelfTest: ${stResult.cause().message}")
                        }
                    }
                }else{
                    log.error("Could not launch Restpoint: ${rpResult.cause().message}")
                }
            }
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    println("Shutdown signal received")
                    vertx.undeploy(restpointID)
                    vertx.close()
                }
            })
        }else{
            log.error("Could not set up vertx cluster: ${result.cause().message}")
        }

    }

}