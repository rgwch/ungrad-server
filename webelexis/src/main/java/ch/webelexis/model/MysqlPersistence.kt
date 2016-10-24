package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler

/**
 * Created by gerry on 22.10.2016.
 */
class MysqlPersistence : Persistence{
    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<AsyncPersistentObject>)->Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}