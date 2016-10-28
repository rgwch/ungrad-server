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
 * * val fieldnames: Array<String> - name of fields of this class. Any attempt to read or write a field not named here will
 * result in a failure.
 *
 * Implementations can chose to replace the companion's persistence object with any other IPersistence. This must be done before
 * the first access to any of the AsyncPersistentObjects in the application.
 *
 * The default constructor takes a String which should be a GUID.
 * All values are cached for TIMEOUT milliseconds. After that period, a read access will fetch the whole object from the IPersistence.
 * Writes are always written through to the IPersistence.
 *
 * Created by gerry on 22.10.2016.
 */

abstract class AsyncPersistentObject(val id: String) : JsonUtil("""{"id":"$id"}""") {
    var lastUpdate: Long = 0
    var persistence = defaultPersistence
    val observers = mutableListOf<IObserver>()
    abstract val collection: String
    abstract val fieldnames: Array<Field>
    abstract fun getLabel(): Future<String>

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

    fun set(vararg fields: String)=set(json_create(*fields))

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

    fun addObserver(observer: IObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: IObserver) {
        observers.remove(observer)
    }

    fun notifyObservers(observation: Observation) {
        val (obj, name, oldv, newv) = observation
        for (ob in observers) {
            ob.changed(obj, name, oldv, newv)
        }
    }
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

    }
}

interface IObserver {
    fun changed(obj: AsyncPersistentObject, property: String, oldValue: Any?, newValue: Any?)
}

data class Observation(val obj: AsyncPersistentObject, val prop: String = "", val oldVal: Any? = null, val newVal: Any? = null)
data class Field(val label: String, val name: String = label, val caption: String = label)

operator fun Array<Field>.contains(field: String) = this.any { it.label == field }
fun Array<Field>.merge(other: Array<Field>): Array<Field> {
    val ret = mutableListOf<Field>()

    return ret.toTypedArray()
}

