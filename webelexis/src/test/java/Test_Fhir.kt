import ch.webelexis.model.fhir.Patient
import org.junit.Test


/**
 * Created by gerry on 09.12.16.
 */
class Test_Fhir {

    @Test(expected = IllegalArgumentException::class)
    fun test_badField(){
        val el= Patient()
        el.set("a","b")
        el.set("id","123")
        el.set("wrong","should throw")
    }

    @Test
    fun test_correctFields(){
        val el=Patient()
        el.set("a","ok")
        el.set("b",1000L)
        println(el.encodePrettily())
    }

}