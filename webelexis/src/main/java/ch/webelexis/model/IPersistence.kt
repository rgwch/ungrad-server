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
import io.vertx.core.Future

/**
 * Created by gerry on 22.10.2016.
 */
interface IPersistence {

    fun fetch(collection: String, objid: String, handler: (AsyncResult<AsyncPersistentObject?>) -> Unit)
    fun flush(obj:AsyncPersistentObject, handler: (AsyncResult<Boolean>) -> Unit)
}

class InMemoryPersistence: IPersistence {
    val objects= hashMapOf<String,AsyncPersistentObject>()

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<AsyncPersistentObject?>) -> Unit) {
        handler(Future.succeededFuture(objects[objid]))
    }

    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        objects.put(obj.id,obj)
        handler(Future.succeededFuture(true))
    }

}