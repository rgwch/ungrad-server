package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future

/**
 * Created by gerry on 22.10.2016.
 */
interface Persistence {

    fun fetch(collection: String, objid: String, handler: (AsyncResult<AsyncPersistentObject>) -> Unit)
    fun flush(obj:AsyncPersistentObject, handler: (AsyncResult<Boolean>) -> Unit)
}

class InMemoryPersistence:Persistence{
    val objects= hashMapOf<String,AsyncPersistentObject>()

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<AsyncPersistentObject>) -> Unit) {
        handler(Future.succeededFuture(objects[objid]))
    }

    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        objects.put(obj.id,obj)
        handler(Future.succeededFuture(true))
    }

}