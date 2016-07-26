package ch.rgw.lucinda

import ch.rgw.tools.Configuration
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 26.07.16.
 */

var communicatorID = ""
var vertx: Vertx? = null

fun start(v: Vertx, config: Configuration, tellResult: (success: Boolean, message: String) -> Unit) {
    vertx = v
    vertx?.deployVerticle(Communicator(config)) { result ->
        if (result.succeeded()) {
            communicatorID = result.result()
            tellResult(true, "ok")
        } else {
            tellResult(false, result.cause().message ?: result.result() ?: "general failure")
        }
    }
}

fun stop() {
    vertx?.undeploy(communicatorID)
}

const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
const val BASEADDR = "ch.rgw.lucinda"

/** reply address for error messages */
val FUNC_ERROR = Communicator.RegSpec(".error", "lucinda/error", "get")
/** Add a file to the storage */
val FUNC_IMPORT = Communicator.RegSpec(".import", "lucinda/import", "post")
/** Index a file in-place (don't add it to the storage) */
val FUNC_INDEX = Communicator.RegSpec(".index", "lucinda/index/:url", "get")
/** Retrieve a file by _id*/
val FUNC_GETFILE = Communicator.RegSpec(".get", "lucinda/get/:id", "get")
/** Get Metadata of files matching a search query */
val FUNC_FINDFILES = Communicator.RegSpec(".find", "lucinda/find/:spec", "get")
/** Update Metadata of a file by _id*/
val FUNC_UPDATE = Communicator.RegSpec(".update", "lucinda/update", "post")
/** Connection check */
val FUNC_PING = Communicator.RegSpec(".ping", "lucinda/ping/:var", "get")
val log = LoggerFactory.getLogger("lucinda.Communicator")

var indexManager: IndexManager? = null;
val config: Configuration = Configuration();
