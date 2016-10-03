package ch.rgw.lucinda

import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.json_error
import ch.rgw.tools.json.json_ok
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * This is a Singleton implementation of a Message handler, used for handling of admin messages from the Communicator
 * verticle
 */
object Admin : Handler<Message<JsonObject>> {
    override fun handle(message: Message<JsonObject>) {
        val msg = message.body()
        val result:Future<Any> = when(msg.getString("command")) {
            "getParam" -> getParam(msg)
            "setParam" -> setParam(msg)
            "exec" -> exec(msg)
            else -> Future.failedFuture("Not implemented ${msg.getString("command")}")
        }
        if(result.failed()){
            message.reply(json_error("illegal function call"))
        }else {
            message.reply(json_ok().put("value",result.result()))
        }
    }

    fun exec(msg: JsonObject) : Future<Any>{
        return Future.failedFuture("Not implemented")
    }

    fun getParam(msg: JsonObject): Future<Any> {
        val ret = Future.future<Any>()
        val param = msg.getString("param")
        val value = when (param) {
            "indexdir" -> lucindaConfig.getString("fs_indexdir", "not set")
            "datadir" -> lucindaConfig.getString("fs_basedir", "not set")
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
        val param=msg.getString("param")
        val value=msg.getValue("value")

        lucindaConfig.put(when(param){
            "indexdir" -> "fs_indexdir"
            "datadir" -> "fs_basedir"
            else -> "error"
        },value)
        saveConfig()
        ret.complete(true)
        return ret
    }
}