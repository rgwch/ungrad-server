package ch.elexis.ungrad.server_test

import ch.rgw.tools.Configuration
import io.vertx.core.Vertx

/**
 * Created by gerry on 26.07.16.
 */

var SelfTestID = ""
var vertx: Vertx? = null

fun start(v: Vertx, config: Configuration, tellResult: (success: Boolean, message: String) -> Unit) {
    vertx = v
    vertx?.deployVerticle(SelfTest()) { result ->
        if (result.succeeded()) {
            SelfTestID = result.result()
            tellResult(true, "ok")
        } else {
            tellResult(false, result.cause().message ?: result.result() ?: "general failure")
        }
    }
}

fun stop() {
    vertx?.undeploy(SelfTestID)
}
