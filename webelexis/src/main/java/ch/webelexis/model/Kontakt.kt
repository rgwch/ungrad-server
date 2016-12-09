package ch.webelexis.model

import io.vertx.core.Future

/**
 * Some Contact-related classes. Since this is Kotlin, we can have these classes together in one file
 * Created by gerry on 24.10.16.
 */

/**
 * A general contact
 */
open class Contact(id: String = uuid) : AsyncPersistentObject(id) {

    override val collection = "KONTAKT"
    override val fieldnames = super.fieldnames + arrayOf(
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
        persistence.fetch(collection, id) {
            if (it.succeeded()) {
                val apo = it.result()
                if (apo != null) {
                    ret.complete(apo.getString("Bezeichnung1") as String + " " + apo.getString("Bezeichnung2"))
                } else {
                    ret.fail("object not found")
                }
            } else {
                ret.fail(it.cause())
            }
        }
        return ret
    }

}

/**
 *  A Person is a Contact with some personal properties
 */
open class Person(id: String = uuid) : Contact(id) {
    val pfields = listOf(
            Field("lastname", String::class, false, "Bezeichnung1"),
            Field("firstname", String::class, false, "Bezeichnung2"),
            Field("gender", fieldname="Geschlecht"),
            Field("birthdate", fieldname="Geburtsdatum"),
            Field("title", fieldname="Titel"),
            Field("suffix", fieldname="Titelsuffix"))

    override val fieldnames by lazy {
        super.fieldnames.union(pfields.asIterable()).toTypedArray()
    }

}

/**
 * A User is a Person with the right to use our application
 */
open class User(id: String = uuid) : Person(id) {
    override val fieldnames by lazy {
        super.fieldnames.union(listOf(
                Field("username"),
                Field("password"))
        ).toTypedArray()
    }
}

/**
 * A Patient is a Person with medical data attached
 */
class Patient(id: String = uuid) : Person(id) {

    override val fieldnames by lazy {
        super.fieldnames.union(listOf(
                Field("persAnamnese"),
                Field("famAnamnese")
        )).toTypedArray()
    }

}