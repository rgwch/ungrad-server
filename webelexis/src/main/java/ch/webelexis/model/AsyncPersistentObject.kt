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

import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import java.util.*
import java.util.Map

/**
 * An AsyncPersistentObject is a PersistentObject with asynchroneous access methods (you don't say).
 * As such, it suits better the needs of modern distributed clients (in-house, web, cloud based).
 * All methods return a Future which can be used consequently to retrieve the result of the operation.
 *
 * This is an abstract base class. Subclasses must implement
 * * val collection: String - name of the collection this class belongs to
 * * val fieldnames: Array<String> - name of fields of this class. Any attempt to read or write a field not named here will
 * result in a failure.
 *
 * Implementations can chose to replace the companion's persistence object with any other IPersistence. This must be done before
 * the first access to any of the AsyncPersistentObjects in the application.
 *
 * The default constructor takes a String which should be a GUID.
 *
 * Created by gerry on 22.10.2016.
 */

abstract class AsyncPersistentObject(val id:String) : Observable(){
    var lastUpdate: Long=0
    val fields=hashMapOf<String,String>("id" to id)
    abstract val collection:String
    abstract val fieldnames: Array<String>


    fun get(field: String): Future<String?>{
        val ret=Future.future<String>()
        if(lastUpdate==0L || (System.currentTimeMillis()-lastUpdate)> TIMEOUT){
            persistence.fetch(collection,id) { result ->
                if (result.succeeded()) {
                    result.result()?.fields?.forEach { entry ->
                        if (entry.value != fields[entry.key]) {
                            fields[entry.key] = entry.value
                            notifyObservers(Pair(entry.key,entry.value))
                        }
                    }
                    lastUpdate = System.currentTimeMillis()
                    ret.complete(fields[field] ?: "")
                } else {
                    ret.fail(result.cause())
                }

            }
        }else{
            ret.complete(fields[field])
        }
        return ret
    }

    fun set(field:String,value:String) : Future<Boolean>{
        val ret=Future.future<Boolean>()
        if(!(field in fieldnames)){
            ret.fail("illegal field name: $field")
        }else {
            fields[field] = value
            persistence.flush(this) { result ->
                if (result.succeeded()) {
                    notifyObservers(Pair(field, value))
                    ret.complete(true)
                } else {
                    ret.fail(result.cause())
                }
            }
        }
        return ret
    }
    companion object{
        const val TIMEOUT=60000L;
        var persistence = InMemoryPersistence()
    }

}
