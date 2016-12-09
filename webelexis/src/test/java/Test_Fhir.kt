import ch.webelexis.model.Element
import ch.webelexis.model.SimpleObject
import org.junit.Test
import org.junit.rules.ExpectedException


/**
 * Created by gerry on 09.12.16.
 */
class Test_Fhir {

    @Test(expected = IllegalArgumentException::class)
    fun test_badField(){
        val el= Element(arrayOf(SimpleObject.Field("a",String::class)))
        el.set("a","b")
        el.set("id","123")
        el.set("wrong","should throw")
    }

    @Test
    fun test_correctFields(){
        val el=Element(arrayOf(SimpleObject.Field("a",String::class), SimpleObject.Field("b",Long::class)))
        el.set("a","ok")
        el.set("b",1000L)
        println(el.encodePrettily())
    }

}