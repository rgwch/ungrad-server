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

package ch.elexis.ungrad.server

import ch.rgw.io.FileTool
import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.get
import ch.rgw.tools.json.validate
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Verticle
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by gerry on 05.09.16.
 * Launch Verticles from definitions in JsonObjects:
 * * name: Human readable name of the Verticle
 * * url: Where to load the Verticle from (can be local or remote location, can point to a Verticle source or to a *.jar file)
 * * verticle: class containing the verticle (if url was a *.jar-file). Full qualified class name.
 * * config: JsonObject containing configuration for the Verticle
 */
class LaunchManager(val restPoint: Restpoint) : Handler<Message<JsonObject>> {
    override fun handle(msg: Message<JsonObject>) {
        val jo = msg.body()
        if (!jo.validate("name:string", "url:string")) {
            throw IllegalArgumentException("bad arguments for launcher")
        }
        val verticle_name = jo["name"]!!
        val verticle_url = jo["url"]!!
        val verticle_class: String? = jo["verticle"]
        val static_config = jo.getJsonObject("config", JsonObject())
        val dynamic_config = restPoint.persist.read(verticle_name)
        val verticle_config = JsonUtil(static_config).mergeIn(dynamic_config)
        val url = if (verticle_url.startsWith("file:") && verticle_url.contains("[\\*\\?]".toRegex())) {
            val fullname = verticle_url.substring(5)
            val pname = FileTool.getFilepath(fullname)
            val fname = FileTool.getFilename(fullname)
            val dirlist = Files.newDirectoryStream(Paths.get(pname), fname)
            URL("file:"+dirlist.first().toAbsolutePath().toString())
        } else {
            URL(verticle_url)
        }
        val file = File(url.file)
        if (!file.exists() || !file.canRead()) {
            ch.elexis.ungrad.server.log.error("can't read ${file.absolutePath}")
        } else {
            try {
                val options = DeploymentOptions().setConfig(verticle_config)
                // If no class attribute is given, it's an uncompiled Verticle
                if (verticle_class == null) {
                    restPoint.vertx.deployVerticle(file.absolutePath, options) { handler ->
                        if (handler.succeeded()) {
                            restPoint.verticles.put(verticle_name, handler.result())
                            log.info("launched uncompiled Verticle ${verticle_name}")
                            msg.reply(JsonUtil.ok())
                        } else {
                            log.error(" *** could not launch ${verticle_name}", handler.cause())
                            msg.fail(1, handler.cause().message)
                        }
                    }
                } else {
                    // If we have a class attribute - instantiate the class
                    val cl = URLClassLoader(Array<URL>(1, { url }))
                    val clazz = cl.loadClass(verticle_class)
                    val verticle = clazz.newInstance() as? Verticle
                    restPoint.vertx.deployVerticle(verticle, options) { handler ->
                        if (handler.succeeded()) {
                            restPoint.verticles.put(verticle_name, handler.result())
                            log.info("launched $verticle_name")
                            msg.reply(JsonUtil.ok())
                        } else {
                            log.error(" *** could not launch $verticle_name", handler.cause())
                            msg.fail(1, handler.cause().message)
                        }
                    }
                }

            } catch(ex: Exception) {
                log.error(" *** Could not launch Verticle $verticle_url", ex)
                msg.fail(2, ex.message)
            }
        }
    }

}

