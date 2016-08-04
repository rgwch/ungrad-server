package ch.elexis.ungrad.server.console

import ch.rgw.tools.Configuration
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 31.07.16.
 */
var ConsoleID = ""
var vertx: Vertx? = null

fun start(v: Vertx, config: JsonObject, tellResult: (success: Boolean, message: String) -> Unit) {
    vertx = v
    vertx?.deployVerticle(Console(config)) { result ->
        if (result.succeeded()) {
            ConsoleID = result.result()
            tellResult(true, "ok")
        } else {
            tellResult(false, result.cause().message ?: result.result() ?: "general failure")
        }
    }
}

fun stop() {
    vertx?.undeploy(ConsoleID)
}

const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
const val BASEADDR = "ch.elexis.ungrad.server.console"

/** reply address for error messages */
val FUNC_LIST = Console.RegSpec(".list", "list", "get")
val FUNC_INFO= Console.RegSpec(".info","info/:id","get")
val FUNC_SET=Console.RegSpec(".set","set/:serverID/:parameter/:value","get")
val log=LoggerFactory.getLogger("ungrad console")