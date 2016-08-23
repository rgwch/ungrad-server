package ch.rgw.lucinda

import ch.rgw.tools.JsonUtil
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * This is a Singleton implementation of a Message handler, used for handling of admin messages from the Communicator
 * verticle
 */
object Admin : Handler<Message<JsonObject>> {
    override fun handle(message: Message<JsonObject>) {
        val msg = message.body()
        when (msg.getString("command")) {
            "getName" -> message.reply(JsonObject().put("name", "Lucinda").put("status", "ok"))
            "getParams" -> {
                val params = createParams()
                message.reply(params)
            }
            else -> message.reply(JsonObject().put("status", "error").put("message", "illegal function call"))
        }

    }

    fun createParams(): JsonObject {

        val indexdir = """
           {
                "name":"indexdir",
                "caption":"index directory",
                "type": "string",
                "value": "/some/dir",
                "writable":true
            }
        """
        val datadir = """
            {
                "name":"datadir",
                "caption":"import directory",
                "type": "string",
                "value":"/some/dir/data",
                "writable":true
            }
        """
        val ret = JsonUtil.create("status:ok")
        val result= JsonArray()
        val jIndex=JsonObject(indexdir)
        val jData=JsonObject(datadir)
        jIndex.put("value", Communicator.config.getString("fs_indexdir", "target/store/index"))
        jData.put("value",Communicator.config.getString("fs_watch","target/store"))
        result.add(jIndex).add(jData)
        ret.put("result",result)
        return ret;
    }


}