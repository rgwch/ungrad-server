package ch.rgw.lucinda

import ch.rgw.tools.Configuration
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 26.07.16.
 */

var communicatorID: String? = null
var vertx: Vertx? = null

fun start(v: Vertx, config: JsonObject, tellResult: (success: Boolean, message: String) -> Unit) {
    vertx = v
    val vfg=Configuration().merge(config.map)
    vertx?.deployVerticle(Communicator(vfg)) { result ->
        if (result.succeeded()) {
            communicatorID = result.result()
            tellResult(true, "ok")
        } else {
            tellResult(false, result.cause().message ?: result.result() ?: "general failure")
        }
    }
}

fun stop() {
    if(communicatorID != null) {
        vertx?.undeploy(communicatorID)
        communicatorID = null;
    }
}

const val REGISTER_ADDRESS = "ch.elexis.ungrad.server.register"
const val BASEADDR = "ch.rgw.lucinda"
const val CONTROL_ADDR=BASEADDR+".admin"

/** reply address for error messages */
val FUNC_ERROR = Communicator.RegSpec(".error", "lucinda/error", "user", "get")
/** Add a file to the storage */
val FUNC_IMPORT = Communicator.RegSpec(".import", "lucinda/import", "docmgr", "post")
/** Index a file in-place (don't add it to the storage) */
val FUNC_INDEX = Communicator.RegSpec(".index", "lucinda/index/:url", "docmgr", "get")
/** Retrieve a file by _id*/
val FUNC_GETFILE = Communicator.RegSpec(".get", "lucinda/get/:id", "user", "get")
/** Get Metadata of files matching a search query */
val FUNC_FINDFILES = Communicator.RegSpec(".find", "lucinda/find/:spec", "user", "get")
/** Update Metadata of a file by _id*/
val FUNC_UPDATE = Communicator.RegSpec(".update", "lucinda/update", "docmgr","post")
/** Connection check */
val FUNC_PING = Communicator.RegSpec(".ping", "lucinda/ping/:var", "guest", "get")
val log = LoggerFactory.getLogger("lucinda.Communicator")

var indexManager: IndexManager? = null;
val config = Configuration();
