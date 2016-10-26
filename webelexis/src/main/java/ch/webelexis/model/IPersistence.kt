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
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 22.10.2016.
 */
interface IPersistence {

    fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit)
    fun flush(obj:AsyncPersistentObject, handler: (AsyncResult<Boolean>) -> Unit)
    fun find(template:JsonObject, handler: (AsyncResult<List<JsonObject>>) -> Unit)
    fun delete(id:String, ack: (Boolean) -> Unit)
}

class InMemoryPersistence: IPersistence {

    val objects= hashMapOf<String,JsonObject>()

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit) {
        handler(Future.succeededFuture(objects[objid]))
    }

    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        objects.put(obj.id,obj)
        handler(Future.succeededFuture(true))
    }

    override fun find(template: JsonObject, handler: (AsyncResult<List<JsonObject>>) -> Unit) {
        val result=objects.values.filter { obj->
            var matches=true
            for((key,value) in obj.map.entries){
                if(template.containsKey(key) && value!=template.getValue(key)) {
                    matches=false
                    break;
                }
            }
            matches
        }
        handler(Future.succeededFuture(result))
    }

    override fun delete(id:String, ack:(Boolean) -> Unit){
        if(objects.containsKey(id)){
            objects.remove(id)
            ack(true)
        }else{
            ack(false)
        }
    }

}