package ch.webelexis.model

import ch.webelexis.model.AsyncPersistentObject

/**
 * Created by gerry on 24.10.16.
 */
class DummyPersistentObject(id: String) : AsyncPersistentObject(id){
    override val collection="DUMMY"
    override val fieldnames=arrayOf("p_1","p_2","p_3")

}