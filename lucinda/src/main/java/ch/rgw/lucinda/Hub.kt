package ch.rgw.lucinda

import EB_SETCONFIG
import RegSpec
import API
import ch.rgw.tools.json.JsonUtil
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 07.01.17.
 */

var vx:Vertx?=null
fun saveConfig() {
    vx?.eventBus().send(EB_SETCONFIG, JsonObject().put("id", "ch.rgw.lucinda.lucindaConfig").put("value", lucindaConfig))
}
const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"

val lucindaConfig = JsonUtil()
val log = LoggerFactory.getLogger("Lucinda")


fun register(func: RegSpec, serverDesc:JsonObject) {
        vx?.eventBus().send<JsonObject>(REGISTER_ADDRESS, JsonObject()
            .put("ebaddress", func.addr)
            .put("rest", "$API/${func.rest}").put("method", func.method).put("role", func.role)
            .put("server", serverDesc), RegHandler(func))
}

var autoscannerID:String=""
var commincatorID:String=""

class Hub : AbstractVerticle() {



    override fun start(startResult: Future<Void>) {
        vx=vertx
        val autoScannerResult = Future.future<Void>()
        val communicatorResult = Future.future<Void>()
        val launchResult = Future.future<Void>()
        val consolidatedResult = listOf(
                autoScannerResult, launchResult, communicatorResult
        )
        CompositeFuture.all(consolidatedResult).setHandler { result ->
            if (result.succeeded()) {
                startResult.complete()
            } else {
                startResult.fail(result.cause())
            }
        }

        super.start()
        val eb = vertx.eventBus()
        lucindaConfig.mergeIn(config())
        val indexManager = IndexManager(lucindaConfig.getString("fs_indexdir", "target/store"), lucindaConfig.getString("default_language", "de"))
        val dispatcher = Dispatcher(lucindaConfig.getString("fs_import", "target/store"), lucindaConfig.getString("default_language", "de"))
        vertx.deployVerticle(Autoscanner(), DeploymentOptions().setConfig(lucindaConfig).setWorker(true)) { result ->
            if (result.succeeded()) {
                autoscannerID = result.result()
                autoScannerResult.complete()
            }
        }
        vertx.deployVerticle(Communicator(), DeploymentOptions().setConfig(lucindaConfig).setWorker(true)){
            if(it.succeeded()){
                commincatorID = it.result()
                communicatorResult.complete()
            }
        }
        launchResult.complete()

    }
    override fun stop(stopResult: Future<Void>) {
        indexManager.shutDown()
        vertx.undeploy(autoScanner)
        stopResult.complete()
        log.info("Lucinda stopped")
    }

}

class RegHandler(val func: RegSpec) : AsyncResultHandler<Message<JsonObject>> {
    override fun handle(result: AsyncResult<Message<JsonObject>>) {
        if (result.failed()) {
            log.error("could not register ${func.addr} for ${func.rest}: ${result.cause()}")
        } else {
            if ("ok" == result.result().body().getString("status")) {
                log.debug("registered ${func.addr}")
            } else {
                log.error("registering of ${func.addr} failed: ${result.result().body().getString("message")}")
            }
        }
    }

}