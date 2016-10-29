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

import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.json_create
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import java.util.*

/**
 * An AsyncPersistentObject is a PersistentObject with asynchroneous access methods (you don't say).
 * As such, it suits better the needs of modern distributed clients (in-house, web, cloud based).
 * All methods return a Future which can be used consequently to retrieve the result of the operation.
 *
 * This is an abstract base class. Subclasses must implement
 * * val collection: String - name of the collection this class belongs to
 * * val fieldnames: Array<Field> - description of fields of this class. Any attempt to read or write a field not named here will
 * result in a failure.
 * * fun getLabel(): return some human readable description for the object
 *
 * Implementations can chose to replace the companion's [defaultPersistence] object with any other IPersistence. This must be done before
 * the first access to any of the AsyncPersistentObjects in the application. Newly created AsyncPersisterntObjecs will send and
 * receive contents via this DegfaultPersistence by default. But an application can chose to set an individual IPersistence on some or
 * all AsyncPersistentObjects to allow for different storage concepts by data type.
 *
 * The default constructor takes a String which should be a GUID. If no parameter is given, a UUID is generated.
 * All values are cached for TIMEOUT milliseconds. After that period, a read access will fetch the whole object from the IPersistence.
 * Writes are always written through to the IPersistence.
 *
 * Created by gerry on 22.10.2016.
 */

abstract class AsyncPersistentObject(val id: String=uuid) : JsonUtil("""{"id":"$id"}""") {
    var lastUpdate: Long = 0
    var persistence = defaultPersistence
    val observers = mutableListOf<IObserver>()
    abstract val collection: String
    abstract val fieldnames: Array<Field>
    abstract fun getLabel(): Future<String>

    /**
     * Get the value of a [field].
     * @return a Future, which resolves to the contents of the field, or fails, if the field was not a found
     * in [fieldnames] or if the value could not b retrieved.
     *
     */
    fun get(field: String): Future<Any> {
        val ret = Future.future<Any>()
        if (field in fieldnames) {
            if (lastUpdate == 0L || (System.currentTimeMillis() - lastUpdate) > TIMEOUT) {
                persistence.fetch(collection, id) { result ->
                    if (result.succeeded()) {
                        result.result()?.forEach {
                            val (key, value) = it
                            if (value != getValue(key)) {
                                val observation = Observation(this, key, getValue(key) ?: "", value)
                                put(key, value)
                                notifyObservers(observation)
                            }
                        }
                        lastUpdate = System.currentTimeMillis()
                        ret.complete(getValue(field) ?: "")
                    } else {
                        ret.fail(result.cause())
                    }

                }
            } else {
                ret.complete(getValue(field))
            }
        } else {
            ret.fail("illegal field name: $field")
        }
        return ret
    }

    /**
     * Set several [fields] in one call, using a syntax like: ("fieldname:fieldvalue","otherfield:othervalue"...)
     * @return a Future resolving to a Boolean 'true' on success.
     * fails if: One or more of the fields are not found in [fieldnames] or if a write error occurred
     */
    fun set(vararg fields: String)=set(json_create(*fields))

    /**
     * set several [fields] in one call, using a JsonObject to carry the fields and their values.
     * @return a Future resolving zo a Boolean 'true' on success.
     * fails if: One or more of the fields are not found in [fieldnames] or if a write error occurred
     */
    fun set(fields: JsonObject): Future<Boolean> {
        val ret = Future.future<Boolean>()
        for ((key) in fields.map) {
            if (key !in fieldnames) {
                ret.fail("illegal field name: $key")
            }
        }
        if (!ret.failed()) {
            mergeIn(fields)
            persistence.flush(this) {
                if (it.succeeded()) {
                    notifyObservers(Observation(this))
                    ret.complete(true)
                }
            }
        }
        return ret
    }

    /**
     * set a single [field] to a [value]
     * @return a Future resolving to a Boolean true
     * fails if: The fields is not found in [fieldnames] or if a write error occurred

     */
    fun set(field: String, value: String): Future<Boolean> {
        val ret = Future.future<Boolean>()
        if (field !in fieldnames) {
            ret.fail("illegal field name: $field")
        } else {
            val observation = Observation(this, field, getValue(field), value)
            put(field, value)
            persistence.flush(this) { result ->
                if (result.succeeded()) {
                    notifyObservers(observation)
                    ret.complete(true)
                } else {
                    ret.fail(result.cause())
                }
            }
        }
        return ret
    }

    /**
     * Add an [observer]. The Observer is called, whenever a field changes its value
     */
    fun addObserver(observer: IObserver) {
        observers.add(observer)
    }

    /**
     * remove an [observer]. If the Observer was not added before, nothing happens.
     */
    fun removeObserver(observer: IObserver) {
        observers.remove(observer)
    }


    protected fun notifyObservers(observation: Observation) {
        val (obj, name, oldv, newv) = observation
        for (ob in observers) {
            ob.changed(obj, name, oldv, newv)
        }
    }

    /**
     * Load the contents of this object (identified by its collection and its id) from the IPersistence.
     * to load an object:
     * * create an empty Object of a subcalss, e.g. val ct=Contact("123")
     * * load its contents via ct.load(handler)
     *
     */
    fun load(handler: (AsyncResult<Boolean>) -> Unit) {
        persistence.fetch(collection, id) {
            if (it.succeeded()) {
                if (it.result() == null) {
                    handler(Future.succeededFuture(false))
                } else {
                    mergeIn(it.result())
                    handler(Future.succeededFuture(false))
                }
            } else {
                handler(Future.failedFuture(it.cause()))
            }
        }
    }

    /**
     * Find Objects matching given criteria
     * to query for objects:
     * * create a query object, e.g: val qbe=Person(); qbe.set("name:meier")
     * * run the  query: qbe.find()
     *
     * @return A Future resolving to a List of matching objects
     */
    fun find(): Future<List<JsonObject>> {
        val ret = Future.future<List<JsonObject>>()
        persistence.find(this) {
            if (it.succeeded()) {
                ret.complete(it.result())
            } else {
                ret.fail(it.cause())
            }
        }
        return ret
    }

    companion object {
        const val TIMEOUT = 60000L;
        var defaultPersistence=InMemoryPersistence()
        val uuid:String
            get()=UUID.randomUUID().toString()
    }
}

interface IObserver {
    fun changed(obj: AsyncPersistentObject, property: String, oldValue: Any?, newValue: Any?)
}

data class Observation(val obj: AsyncPersistentObject, val prop: String = "", val oldVal: Any? = null, val newVal: Any? = null)

/**
 * Definition of a Field of an AsyncPersistentObject
 * [label]: The label to use with set and get-methods
 * [name]: The name of the field in the backing storage
 * [caption]: a (possibly translatable) caption to use in UI
 * only the [label] field is mandatory
 */
data class Field(val label: String, val name: String = label, val caption: String = label)
operator fun Array<Field>.contains(field: String) = this.any { it.label == field }

