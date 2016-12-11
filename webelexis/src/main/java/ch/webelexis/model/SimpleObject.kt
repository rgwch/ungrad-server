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

    val observers = mutableListOf<IObserver>()
    var dirty:Boolean=false;

    @Throws(IllegalArgumentException::class)
    fun set(field: String, value: Any) :JsonObject{
        checkField(field,value)
        val oldValue=getValue(field)
        put(field, value)
        dirty=true
        observers.forEach { it.changed(this,field,oldValue,value) }
        return this
    }

    @Throws(IllegalArgumentException::class)
    open fun get(field: String): Any {
        checkField(field,null)
        return get(field)
    }

    override fun put(name: String, value:Any):JsonObject{
        return set(name,value)
    }

    fun addObserver(ob: IObserver){
        observers.add(ob)
    }

    fun removeObserver(ob: IObserver){
        observers.remove(ob)
    }
    private fun checkField(fieldname: String, value: Any?){
        val field=fields.find { it.label==fieldname }
        if(field==null){
            throw IllegalArgumentException("$fieldname is not in field list")
        }
        if(value != null){
            if(value.javaClass!=field.type.java){
                throw IllegalArgumentException("incorrect data type for $fieldname")
            }
            if(field.group!=0) {
                if (fields.find { it.group == field.group } != null) {
                    throw IllegalArgumentException("mutually exclusive field $fieldname set")
                }
            }
        }

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

interface IObserver {
    fun changed(obj: SimpleObject, property: String, oldValue: Any?, newValue: Any?)
    fun stored(obj: SimpleObject)
}

data class Observation(val so: SimpleObject, val property: String, val oldValue: Any, val newValue: Any )