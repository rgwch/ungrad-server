package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future

/**
 * Created by gerry on 22.10.2016.
 */
interface Persistence {

    fun fetch(collection: String, objid: String, handler: (AsyncResult<Map<String, String>>) -> Unit)
    fun flush(obj:AsyncPersistentObject, handler: (AsyncResult<Boolean>) -> Unit)
}

class InMemoryPersistence:Persistence{
    val objects= hashMapOf<String,Map<String,String>>()

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<Map<String, String>>) -> Unit) {
        handler(Future.succeededFuture(objects[objid]))
    }

    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        handler(Future.succeededFuture(true))
    }

}