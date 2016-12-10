package ch.webelexis.model

import io.vertx.core.json.JsonObject
import kotlin.reflect.KClass

/**
 * A SimpleObject is a JsonObject with Field definitions. The connstructor receives an Array of valid fields.
 * Any attempt to set or get an invalid field will throw an IllegalArgumentException
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
 * [type]: The Object class of the field contents (defaults to String::class)
 * [lazy]: Whether this field should be loaded lazily (defaults to false)
 * [fieldname]: The name of the field in the backing storage (defaults to [label])
 * [caption]: a (possibly translatable) caption to use in UI (defaults to [label])
 * [required]: if the object is validated, all required Fields must be present and set.
 * [group]: If several fields belong to the same group, only one of them is allowed.
 * only the [label] field is mandatory. All other fields are set to defaults.
 */

data class Field(val label: String,
                 val type: KClass<*> = String::class,
                 val lazy:Boolean=false,
                 val fieldname:String=label,
                 val caption:String=label,
                 val required:Boolean=false,
                 val group:Int=0)
