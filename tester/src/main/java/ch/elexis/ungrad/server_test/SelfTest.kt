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

import ch.rgw.tools.JsonUtil
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
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
        vertx.eventBus().consumer<JsonObject>("ch.elexis.ungrad.server_test.admin"){msg ->
            if(msg.body().getString("command")=="getName"){
                msg.reply(JsonUtil.create("status:ok","name:Server self test"))
            }else if(msg.body().getString("command")=="getParams"){
                val result=JsonArray()
                val os=System.getProperty("os.name")+" / "+System.getProperty("os.arch")+", v."+System.getProperty("os.version")
                result.add(JsonUtil.create("name:os","caption:Operating System","type:String").put("writable",false).put("value",os))
                val java=System.getProperty("java.version")+" / "+System.getProperty("java.vendor")
                result.add(JsonUtil.create("name:java","caption:Java","type:String","value:${java}").put("writable",false))
                msg.reply(JsonUtil.create("status:ok").put("result",result))
            }
        }
        /**
         * On startup, register our EventBus address with the dispatcher and tell him, that we are
         * interested in "get" requests with the given address scheme.
         */
        val registerMsg= JsonUtil.create("ebaddress:${MY_ADDRESS}","method:get","server-id:ch.elexis.ungrad.server_test",
                "server-control:ch.elexis.ungrad.server_test.admin")
                .put("rest","1.0/ping/:plus/:minus");
        vertx.eventBus().send<JsonObject>(REGISTER_ADDRESS,registerMsg) { reply ->
            if(reply.succeeded()){
                log.info("successfully registered TestVerticle")
            }else{
                log.error("Could not register TestVerticle")
            }
        }
    }
}