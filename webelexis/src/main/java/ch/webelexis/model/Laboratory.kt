package ch.webelexis.model

import io.vertx.core.Future

/**
 * Created by gerry on 27.10.16.
 */

class LabItem(id: String) : AsyncPersistentObject(id) {
    override val collection = "LABORITEMS"
    override val fieldnames = arrayOf(
            Field("Title"),
            Field("refMale", fieldname="RefMann"),
            Field("refFemale", fieldname="RefFrauOrTx"),
            Field("priority", fieldname="prio"),
            Field("group", fieldname="Gruppe"),
            Field("type", fieldname="Typ"),
            Field("unit", fieldname="Einheit"),
            Field("labID", fieldname="LaborID"),
            Field("shortname", fieldname="Kuerzel"),
            Field("formula"),
            Field("digits"),
            Field("visible"),
            Field("billingcode"),
            Field("loinc", fieldname="loinccode")

    )

    override fun getLabel(): Future<String> {
        return Future.future()
    }

}

class LabResult(id: String) : AsyncPersistentObject(id) {
    override val collection = "LABORWERTE"
    override val fieldnames = arrayOf(
            Field("date", fieldname="Datum"),
            Field("time", fieldname="Zeit"),
            Field("flags", fieldname="Flags"),
            Field("comment", fieldname="Kommentar"),
            Field("result", fieldname="Resultat"),
            Field("itemID", fieldname="ItemID"),
            Field("patID", fieldname="PatientID")
    )

    override fun getLabel(): Future<String> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class Labor(id: String) : Contact(id) {

}