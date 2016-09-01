package ch.rgw.lucinda

import ch.rgw.tools.JsonUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Future
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
        val result:Future<Any> = when(msg.getString("command")) {
            "getParam" -> {
                getParam(msg)

            }
            "setParam" -> {
                setParam(msg)
            }
            "exec" -> {
                Future.failedFuture("Not implemented")
            }

            else -> Future.failedFuture("Not implemented")
        }
        if(result.failed()){
            message.reply(JsonObject().put("status", "error").put("message", "illegal function call"))
        }else {
            message.reply(JsonUtil.create("status:ok", "value:${result.result()}"))
        }
    }

    fun getParam(msg: JsonObject): Future<Any> {
        val ret = Future.future<Any>()
        val param = msg.getString("param")
        val value = when (param) {
            "indexdir" -> Communicator.config.getString("fs_indexdir", "not set")
            "datadir" -> Communicator.config.getString("fs_basedir", "not set")
            else -> ""

        }
        if(value==""){
            ret.fail("unknown parameter")
        }else{
            ret.complete(value)
        }
        return ret
    }

    fun setParam(msg: JsonObject): Future<Any> {
        val ret=Future.future<Any>()
        ret.fail("not implemented")
        return ret
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
        val result = JsonArray()
        val jIndex = JsonObject(indexdir)
        val jData = JsonObject(datadir)
        jIndex.put("value", Communicator.config.getString("fs_indexdir", "target/store/index"))
        jData.put("value", Communicator.config.getString("fs_watch", "target/store"))
        result.add(jIndex).add(jData)
        ret.put("result", result)
        return ret;
    }


}