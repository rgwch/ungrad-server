package ch.webelexis.model

import io.vertx.core.Future

/**
 * Created by gerry on 27.10.16.
 */

class LabItem(id: String) : AsyncPersistentObject(id) {
    override val collection = "LABORITEMS"
    override val fieldnames = arrayOf(
            Field("Title"),
            Field("refMale", "RefMann"),
            Field("refFemale", "RefFrauOrTx"),
            Field("priority", "prio"),
            Field("group", "Gruppe"),
            Field("type", "Typ"),
            Field("unit", "Einheit"),
            Field("labID", "LaborID"),
            Field("shortname", "Kuerzel"),
            Field("formula"),
            Field("digits"),
            Field("visible"),
            Field("billingcode"),
            Field("loinc", "loinccode")

    )

    override fun getLabel(): Future<String> {
        return Future.future()
    }

}

class LabResult(id: String) : AsyncPersistentObject(id) {
    override val collection = "LABORWERTE"
    override val fieldnames = arrayOf(
            Field("date", "Datum"),
            Field("time", "Zeit"),
            Field("flags", "Flags"),
            Field("comment", "Kommentar"),
            Field("result", "Resultat"),
            Field("itemID", "ItemID"),
            Field("patID", "PatientID")
    )

    override fun getLabel(): Future<String> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class Labor(id: String) : Contact(id) {

}