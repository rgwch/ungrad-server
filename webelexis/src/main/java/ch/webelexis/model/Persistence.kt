package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler

/**
 * Created by gerry on 22.10.2016.
 */
interface Persistence {

    fun fetch(collection: String, objid: String, handler: (AsyncResult<Map<String, String>>) -> Unit)
    fun flush(collection: String, obj: AsyncPersistentObject, handler: AsyncResultHandler<Boolean>)
}
