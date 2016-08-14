package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 14.08.16.
 */
object Admin : Handler<Message<JsonObject>> {
    override fun handle(msg: Message<JsonObject>) {
        msg.reply(JsonUtil.create("status:error","message:not implemented"))
    }

}