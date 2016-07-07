package ch.elexis.ungrad.server_test

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 07.07.16.
 */
class SelfTest: AbstractVerticle() {
    val REGISTER_ADDRESS="ch.elexis.ungrad.server.register"
    val MY_ADDRESS="ch.elexis.ungrad.server-test"

    val log=LoggerFactory.getLogger(this.javaClass)

    override fun start() {
        super.start()
        vertx.eventBus().consumer<JsonObject>(MY_ADDRESS){msg ->
            val cmd=msg.body()
            val parm=cmd.getString("parm")
            msg.reply(JsonObject().put("Answer:","Pong, ${parm}"))

        }
        val registerMsg=JsonObject().put("rest","1.0/ping/:parm")
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