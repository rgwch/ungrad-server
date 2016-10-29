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
    /**
     * load an object from the backing-store.
     * @param collection: An application defined collection (e.g. a database table)
     * @param objid: ID of the object to load
     * @param handler: A method to call after completion of the fetch
     */
    fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit)

    /**
     * save an object to the backing store
     * @param obj: The object to store
     * @param handler: a method to call avter completion of the write
     */
    fun flush(obj:AsyncPersistentObject, handler: (AsyncResult<Boolean>) -> Unit)

    /**
     * retrieve a List of zero or more objects matching some criteria
     * @param template: An object with the criteria
     * @param handler: a method to call with the result
     */
    fun find(template:JsonObject, handler: (AsyncResult<List<JsonObject>>) -> Unit)

    /**
     * delete an object from the backing store
     */
    fun delete(id:String, ack: (Boolean) -> Unit)
}

/**
 * Default volatile  IPersistence which simply stores in memory.
 */
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
                if(key!="id" && template.containsKey(key)){
                    val tval=template.getValue(key)
                    val cmpfun = if(tval is String && tval.endsWith("*")){
                        { x: Any,y: Any -> if((x as String).startsWith((y as String).substringBefore('*'))) true else false}
                    }else{
                        { x: Any,y:Any -> if(x==y) true else false}

                    }
                    matches=cmpfun(value,tval)
                    if(matches==false){
                        break
                    }
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