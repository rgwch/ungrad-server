package ch.webelexis.model.fhir

import ch.webelexis.model.Field

/**
 * http://hl7.org/fhir/resourceguide.html#3.1.3
 * Created by gerry on 09.12.16.
 */

val patientFields=arrayOf(
        Field("identifier", Array<Identifier>::class),
        Field("active",Boolean::class),
        Field("name",String::class),
        Field("telecom",String::class),
        Field("gender",Gender::class)
)
class Patient:DomainResource(patientFields){

}