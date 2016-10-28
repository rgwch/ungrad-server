package ch.webelexis.model

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import java.util.*

/**
 * Created by gerry on 24.10.16.
 */

open class Contact(id: String) : AsyncPersistentObject(id) {

    constructor():this(UUID.randomUUID().toString())

    override val collection = coll
    override val fieldnames = arrayOf(
            Field("Bezeichnung1"),
            Field("Bezeichnung2"),
            Field("Bezeichnung3"),
            Field("E-Mail"),
            Field("phone1"),
            Field("phone2"),
            Field("mobile"),
            Field("fax"),
            Field("street"),
            Field("zip"),
            Field("city"),
            Field("website"),
            Field("address")
    )

    override fun getLabel(): Future<String> {
        val ret = Future.future<String>()
        persistence.fetch(collection,id) {
            if(it.succeeded()){
                val apo=it.result()
                if(apo!=null) {
                    ret.complete(apo.getString("Bezeichnung1") as String+" "+apo.getString("Bezeichnung2"))
                }else{
                    ret.fail("object not found")
                }
            }else{
                ret.fail(it.cause())
            }
        }
        return ret
    }
    companion object{
        val coll="KONTAKT"
        fun load(id:String) : Future<Contact>{
            val ret=Future.future<Contact>()
            val ctk=Contact(id)
            AsyncPersistentObject.load(ctk) {
                if(it.succeeded() && it.result()){
                    ret.complete(ctk)
                }else{
                    ret.fail(it.cause())
                }
            }
            return ret
        }

    }

}

open class Person(id: String) : Contact(id) {
    val pfields = listOf(
            Field("name", "Bezeichnung1"),
            Field("firstname", "Bezeichnung2"),
            Field("gender", "Geschlecht"),
            Field("dob", "Geburtsdatum"),
            Field("title", "Titel"),
            Field("suffix", "Titelsuffix"))

    val t = super.fieldnames.union(pfields)
    override val fieldnames by lazy {
        super.fieldnames.union(pfields.asIterable()).toTypedArray()
    }

}

class Patient(id: String) : Person(id) {

    override val fieldnames by lazy {
        super.fieldnames.union(listOf(
                Field("persAnamnese"),
                Field("famAnamnese")
        )).toTypedArray()
    }
}