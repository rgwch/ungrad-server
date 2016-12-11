package ch.webelexis.model.fhir

import ch.rgw.tools.TimeTool
import ch.webelexis.model.Field
import ch.webelexis.model.SimpleObject
import java.net.URI

/**
 * A collection of [fhir](http://hl7.org/fhir/) entities. They can be constructed like
 *
 *   val pat=Patient("some-id").set("attribute","value")
 *
 * Conversion to Json is as easy as:
 *
 *   pat.encode(), or
 *   pat.encodePrettily()
 *
 * Create a Patient from Json like this:
 *
 *   val jack=Patient.parse(some_json)
 *
 * Persist an entity like this:
 *
 *   val storage=MysqlPeprsistence(config:JsonObject)
 *   ...
 *   storage.save(jack)
 *
 * or fetch it like this:
 *
 *   storage.load(jacks_id){ result ->
 *      if(result.succeeded()){
 *          val jack=result.result()
 *      }else{
 *          // oops!
 *      }
 *   }
 *
 * Created by gerry on 08.12.16.
 */

// ---------------- FHIR dat types --------------

class Coding : SimpleObject("system", "version", "code", "display", "userSelected")

class CodeableConcept : SimpleObject(arrayOf(Field("coding"), Field("text", Coding::class)))
class Period : SimpleObject(arrayOf(Field("start", TimeTool::class), Field("end", TimeTool::class)))
class Reference : SimpleObject("reference", "text")


class Identifier : SimpleObject(arrayOf(Field("use"), Field("code", CodeableConcept::class), Field("system"), Field("value"),
        Field("period", Period::class), Field("assigner", Reference::class)))

class Extension : SimpleObject(arrayOf(Field("url", String::class), Field("value", Any::class)))

open class Element(ext: Array<Field>) : SimpleObject(ext + arrayOf(Field("id"), Field("extension", Extension::class)))

class Meta : Element(arrayOf(Field("versionId"), Field("lastUpdated", Long::class), Field("profile", List::class),
        Field("security", Coding::class), Field("tag", Coding::class)))

enum class Gender{
    MALE,FEMALE,OTHER,UNKNOWN
}
// --------------- FHIR Resources --------------

val resourceFields = arrayOf(
        Field("id", String::class),
        Field("meta", Meta::class, lazy = true),
        Field("implicitRules", URI::class),
        Field("language",String::class)
)

abstract class Resource(val extraFields:Array<Field>) : SimpleObject(extraFields+resourceFields) {

}

val domainResourceFields=arrayOf(
        Field("text")
)
abstract class DomainResource(extraFields:Array<Field>):Resource(extraFields+domainResourceFields){

}