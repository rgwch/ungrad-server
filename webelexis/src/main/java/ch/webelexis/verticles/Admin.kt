package ch.webelexis.verticles

import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.json_error
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 14.08.16.
 */
object Admin : Handler<Message<JsonObject>> {
    override fun handle(msg: Message<JsonObject>) {
        msg.reply(json_error("not implemented"))
    }

}