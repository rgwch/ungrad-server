package ch.webelexis.model

import io.vertx.core.json.JsonObject
import kotlin.reflect.KClass

/**
 * Created by gerry on 08.12.16.
 */
open class SimpleObject(val fields: Array<Field>) : JsonObject() {
    constructor(vararg fieldnames:String):this(fieldnames.map { name -> Field(name,String::class) }.toTypedArray())

    @Throws(IllegalArgumentException::class)
    fun set(field: String, value: Any) {
        if (field in fields) {
            put(field, value)
        } else {
            throw IllegalArgumentException("$field is not in field list")
        }
    }

    @Throws(IllegalArgumentException::class)
    fun get(field: String): Any {
        if (field in fields) {
            return get(field)
        } else {
            throw IllegalArgumentException("$field is not in field list")
        }
    }


    operator fun Array<Field>.contains(name: String): Boolean {
        return this.find { it.label == name } != null
    }
}

/**
 * Definition of a Field of a SimpleObject
 * [label]: The label to use with set and get-methods
 * [fieldname]: The name of the field in the backing storage
 * [caption]: a (possibly translatable) caption to use in UI
 * only the [label] field is mandatory
 */

data class Field(val label: String, val type: KClass<*> = String::class, val lazy:Boolean=false, val fieldname:String=label, val caption:String=label)
