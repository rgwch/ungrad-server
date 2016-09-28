/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 */
package ch.elexis.ungrad.server_test

import ch.rgw.tools.json.*
import ch.rgw.tools.TimeTool
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory


/**
 * This module tests and demonstrates the basic functionality of the Dispatcher. It is launched automatically and always.
 * Created by gerry on 07.07.16.
 */
class SelfTest: AbstractVerticle() {
    val REGISTER_ADDRESS="ch.elexis.ungrad.server.register"
    val MY_ADDRESS="ch.elexis.ungrad.server-test"
    val SERVER_CONTROL = "ch.elexis.ungrad.server_test.admin"


    val log=LoggerFactory.getLogger(this.javaClass)

    override fun stop(stopResult:Future<Void>){
        stopResult.complete()
        log.info("Server Info stopped")
    }
    override fun start() {
        super.start()
        /**
         * we'll listen to MY_ADDRESS. The Dispatcher will convert REST calls to "/api/1.0/ping/:plus/:minus" to
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
         * We also listen to the SERVER_CONTROL address for the adninistrative interface. To demonstrate this, we
         * just show some server parameters.
         */
        vertx.eventBus().consumer<JsonObject>(SERVER_CONTROL){msg ->
            if(msg.body()["command"]=="getParam"){
                val answer=when(msg.body()["param"]){
                    "os_desc" -> os_desc
                    "java_desc" -> java_desc
                    "system"->system()
                    "systime"-> systime()
                    "os_name" -> os_name
                    "os_arch" -> os_arch
                    "os_version" -> os_version
                    "java_version" -> java_version
                    "java_vendor" -> java_vendor
                    "processors" -> processors()
                    "total_memory" -> total_memory()
                    "max_memory" -> max_memory()
                    "avail_memory" -> avail_memory()
                    "system_time" -> system_time()
                    else -> "unknown parameter"
                }
                msg.reply(JsonUtil.create("status:ok","value:$answer"))
            }else if(msg.body()["command"]=="setParam"){
                msg.reply(JsonUtil.create("status:error","message:all parameters are read only"))

            }else if(msg.body()["command"]=="exec"){
                msg.reply(JsonUtil.create("status:ok").put("answer",make_overview()))
            }
        }
        /**
         * On startup, register our EventBus address with the dispatcher and tell him, that we are
         * interested in "get" requests with the given address scheme.
         */
        fun registerMsg()= JsonUtil.create("ebaddress:${MY_ADDRESS}","method:get")
                .put("rest","1.0/ping/:plus/:minus")
                .put("server",make_overview())

        vertx.eventBus().send<JsonObject>(REGISTER_ADDRESS,registerMsg()) { reply ->
            if(reply.succeeded()){
                log.info("successfully registered TestVerticle")
            }else{
                log.error("Could not register TestVerticle")
            }

        }

    }
    fun make_overview(): JsonObject{
        return JsonUtil.create("id:ch.elexis.ungrad.server_info","name:Server info","address:$SERVER_CONTROL")
                .put("params",JsonArray(params()))
    }

    /*
     * static properties and methods.
     */
    companion object{
        val os_name=System.getProperty("os.name")
        val os_arch=System.getProperty("os.arch")
        val os_version=System.getProperty("os.version")
        val java_version=System.getProperty("java.version")
        val java_vendor=System.getProperty("java.vendor")
        fun processors()=Runtime.getRuntime().availableProcessors().toString()
        fun total_memory()=roundKB(Runtime.getRuntime().totalMemory()).toString()
        fun max_memory()= roundKB(Runtime.getRuntime().maxMemory()).toString()
        fun avail_memory()=roundKB(Runtime.getRuntime().freeMemory()).toString()
        fun system_time()=TimeTool().toString(TimeTool.DATETIME_XML)
        fun roundKB(b:Long)=Math.round(b.toDouble()/(1024*1024))
        val os_desc="$os_name $os_version / $os_arch"
        val java_desc="$java_version / $java_vendor"
        fun system()="${processors()} Processors, JVM-Memory total ${max_memory()}M / free ${avail_memory()}M / max usable ${max_memory()}M"
        fun systime()=system_time()
        fun params()="""
        [
            {
                "name":"os_desc","caption":"OS","type":"string","value":"$os_desc","writable":false
            },
            {
                "name":"java_desc","caption":"Java","type":"string","value":"$java_desc", "writable":false
            },
            {
                "name":"system","caption":"System","type":"string",
                "value":"${system()}",
                "writable": false
            },
            {
                "name":"systime","caption":"System time","type":"string","value":"${system_time()}","writable":false
            }
        ]
        """

    }
}