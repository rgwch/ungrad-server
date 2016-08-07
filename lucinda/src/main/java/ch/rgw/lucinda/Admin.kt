package ch.rgw.lucinda

import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 04.08.16.
 */
object Admin : Handler<Message<JsonObject>> {
    override fun handle(message: Message<JsonObject>) {
        val msg = message.body()
        when (msg.getString("command")) {
            "start" ->
                if (communicatorID == null) {
                    start(vertx!!, JsonObject(), { b: Boolean, s: String ->
                        if (b) {
                            message.reply(JsonObject().put("status", "ok"))
                        } else {
                            message.reply(JsonObject().put("status", "error").put("message", "launch failed"))
                        }
                    })
                }
            "stop" -> {
                stop()
                message.reply(JsonObject().put("status", "ok"))
            }
            "getName" -> message.reply(JsonObject().put("name", "Lucinda").put("status", "ok"))
            "getParams" -> {
                val params=createParams()
                message.reply(params)
            }
            else -> message.reply(JsonObject().put("status", "error").put("message", "illegal function call"))
        }

    }

    fun createParams(): JsonObject {
        val ret = """
        {
            "status": "ok",
            "result":{
                "indexdir":{
                    "caption":"index directory",
                    "type": "string"
                },
                "datadir":{
                    "caption":"import directory",
                    "type": "string"
                }
            }
        }
        """
        return JsonObject(ret)
    }


}