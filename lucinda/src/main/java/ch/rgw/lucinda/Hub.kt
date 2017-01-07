package ch.rgw.lucinda

import API
import EB_SETCONFIG
import RegSpec
import ch.rgw.tools.json.JsonUtil
import io.vertx.core.*
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 07.01.17.
 */

var vx: Vertx? = null

fun saveConfig() {
    vx?.eventBus()?.send(EB_SETCONFIG, JsonObject().put("id", "ch.rgw.lucinda.lucindaConfig").put("value", lucindaConfig))
}

const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"

val lucindaConfig = JsonUtil()
val log = LoggerFactory.getLogger("Lucinda")


fun register(serverDesc: JsonObject, vararg funcs:RegSpec) {
    funcs.forEach { func ->
        vx?.eventBus()?.send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                .put("ebaddress", func.addr)
                .put("rest", "$API/${func.rest}").put("method", func.method).put("role", func.role)
                .put("server", serverDesc), RegHandler(func))
    }

}


class Hub : AbstractVerticle() {
    var indexManager: IndexManager?=null
    var autoscannerID: String? = null
    var communicatorID: String? = null

    override fun start(startResult: Future<Void>) {
        vx = vertx
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

        lucindaConfig.mergeIn(config())
        indexManager = IndexManager(lucindaConfig.getString("fs_indexdir", "target/store"), lucindaConfig.getString("default_language", "de"))
        val dispatcher = Dispatcher(indexManager)
        vertx.deployVerticle(Autoscanner(dispatcher), DeploymentOptions().setConfig(lucindaConfig).setWorker(true)) { result ->
            if (result.succeeded()) {
                autoscannerID = result.result()
                autoScannerResult.complete()
            }
        }
        vertx.deployVerticle(Communicator(dispatcher), DeploymentOptions().setConfig(lucindaConfig).setWorker(true)) {
            if (it.succeeded()) {
                communicatorID = it.result()
                communicatorResult.complete()
            }
        }
        super.start()
        launchResult.complete()

    }

    override fun stop(stopResult: Future<Void>) {
        indexManager?.shutDown()
        autoscannerID?.let { vertx.undeploy(it) }
        communicatorID?.let { vertx.undeploy(it) }
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