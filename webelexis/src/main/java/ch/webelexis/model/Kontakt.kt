package ch.webelexis.model

import io.vertx.core.Future

/**
 * Created by gerry on 24.10.16.
 */

open class Contact(id: String) : AsyncPersistentObject(id) {
    override val collection = "KONTAKT"
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
        return ret
    }

}

open class Person(id: String) : Contact(id) {
    val pfields = arrayOf(
            Field("name", "Bezeichnung1"),
            Field("firstname", "Bezeichnung2"),
            Field("gender", "Geschlecht"),
            Field("dob", "Geburtsdatum"),
            Field("title", "Titel"),
            Field("suffix", "Titelsuffix"))

    val t = super.fieldnames.union(pfields.asIterable())
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