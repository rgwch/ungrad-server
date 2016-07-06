package elexis.ungrad.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.util.*

/**
 * Created by gerry on 06.07.16.
 */

class Restpoint : AbstractVerticle() {
    val handlers=HashMap<String,String>()

    override fun start() {
        super.start()
        val router=Router.router(vertx)
        vertx.eventBus().consumer<Message<JsonObject>>(ADDR_REGISTER) { msg ->
            val j=msg.body() as JsonObject
            val rest=j.getString("rest")
            val ebmsg=j.getString("msg")
            val method=j.getString("method")
            if(handlers.containsKey(rest)){
                msg.reply(JsonObject().put("status","error").put("message","REST address already registered"))
            }else if(handlers.containsValue(ebmsg)){
                msg.reply(JsonObject().put("status","error").put("message","EventBus address already registered"))
            }else{
                handlers.put(rest,ebmsg)
           }
        }

    }


    companion object {
        val ADDR_REGISTER = "ch.elexis.ungrad.server.register";
    }
}