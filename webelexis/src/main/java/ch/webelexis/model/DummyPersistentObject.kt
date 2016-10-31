package ch.webelexis.model

import io.vertx.core.Future

/**
 * Created by gerry on 24.10.16.
 */
class DummyPersistentObject(id: String) : AsyncPersistentObject(id) {
    override val collection = "DUMMY"
    override val fieldnames = super.fieldnames + arrayOf(Field("p_1"), Field("p_2"), Field("p_3"))

    override fun getLabel(): Future<String> {
        val ret = Future.future<String>()
        ret.complete(collection)
        return ret
    }

}