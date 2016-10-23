package ch.webelexis.model

import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import java.util.*

/**
 * Created by gerry on 22.10.2016.
 */

abstract class AsyncPersistentObject(val id:String) : Observable(){
    var lastUpdate: Long=0
    val fields=hashMapOf<String,String>("id" to id)
    abstract val collection:String
    abstract val fieldnames: Array<String>
    fun fetch(field: String): Future<String>{
        val ret=Future.future<String>()
        if(lastUpdate==0L || (System.currentTimeMillis()-lastUpdate)> TIMEOUT){
            persistence.fetch(collection,id) { result ->
                if (result.succeeded()) {
                    result.result().forEach { entry ->
                        if (entry.value != fields[entry.key]) {
                            fields[entry.key] = entry.value
                            notifyObservers(entry)
                        }
                    }
                    lastUpdate = System.currentTimeMillis()
                    ret.complete(fields[field])
                } else {
                    ret.fail(result.cause())
                }

            }
        }else{
            ret.complete(fields[field])
        }
        return ret
    }

    companion object{
        const val TIMEOUT=60000L;
        var persistence = MysqlPersistence()
    }

}
