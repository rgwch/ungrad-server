package ch.webelexis.model

import ch.rgw.tools.TimeTool
import java.util.*

/**
 * Created by gerry on 08.12.16.
 */


class Coding : SimpleObject("system", "version", "code", "display", "userSelected") {

}

class CodeableConcept : SimpleObject(arrayOf(Field("coding"), Field("text", Coding::class))) {

}

class Period : SimpleObject(arrayOf(Field("start", TimeTool::class), Field("end", TimeTool::class))) {}


class Reference : SimpleObject("reference", "text") {}


class Identifier : SimpleObject(arrayOf(Field("use"), Field("code", CodeableConcept::class), Field("system"), Field("value"),
        Field("period", Period::class), Field("assigner", Reference::class)))

class Extension : SimpleObject(arrayOf(Field("url", String::class), Field("value", Any::class)))

open class Element(ext:Array<Field>) : SimpleObject(ext+arrayOf(Field("id"), Field("extension", Extension::class)))

class Meta : Element(arrayOf(Field("versionId"),Field("lastUpdated",Long::class),Field("profile", List::class),
        Field("security",Coding::class),Field("tag",Coding::class)))

val resourceFields = arrayOf(
        SimpleObject.Field("lastUpdated", Long::class)
)

abstract class Resource(val type: String, val ii: String) : SimpleObject(resourceFields) {
    val identifiers = mutableListOf<Identifier>()
    val meta = mutableListOf<Meta>()

}
