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
package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 22.10.2016.
 */
class MysqlPersistence : IPersistence {
    override fun delete(id: String, ack: (Boolean) -> Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun find(template: JsonObject, handler: (AsyncResult<List<JsonObject>>) -> Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}