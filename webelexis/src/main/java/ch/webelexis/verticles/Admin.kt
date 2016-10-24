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