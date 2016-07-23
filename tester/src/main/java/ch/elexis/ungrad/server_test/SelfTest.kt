/**
 *
 */
package ch.elexis.ungrad.server_test

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * This module tests the basic functionality of the Dispatcher. It is launched automatically and always.
 * Created by gerry on 07.07.16.
 */
class SelfTest: AbstractVerticle() {
    val REGISTER_ADDRESS="ch.elexis.ungrad.server.register"
    val MY_ADDRESS="ch.elexis.ungrad.server-test"

    val log=LoggerFactory.getLogger(this.javaClass)

    override fun start() {
        super.start()
        /**
         * we'll listen to MY_ADDRESS. The Dispatcher will convert REST calls to "1.0/ping/:plus/:minus" to
         * EventBus messages with the parameters "plus" and "minus". We reply with "pong" and echo the
         * parameters.
         */
        vertx.eventBus().consumer<JsonObject>(MY_ADDRESS){msg ->
            val cmd=msg.body()
            val plus=cmd.getString("plus")
            val minus=cmd.getString("minus")
            msg.reply(JsonObject().put("Answer:","Pong, ${plus}, ${minus}"))

        }
        /**
         * On startup, register our EventBus address with the dispatcher and tell him, that we are
         * interested in "get" requests with the given address scheme.
         */
        val registerMsg=JsonObject().put("rest","1.0/ping/:plus/:minus")
        .put("ebaddress",MY_ADDRESS)
        .put("method","get");
        vertx.eventBus().send<JsonObject>(REGISTER_ADDRESS,registerMsg) { reply ->
            if(reply.succeeded()){
                log.info("successfully registered TestVerticle")
            }else{
                log.error("Could not register TestVerticle")
            }
        }
    }
}