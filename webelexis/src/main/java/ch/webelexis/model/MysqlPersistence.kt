/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 */
package ch.webelexis.model

import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.ResultSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 22.10.2016.
 */
class MysqlPersistence(val config: JsonObject, vertx: Vertx) : IPersistence {
    val log : Logger =LoggerFactory.getLogger("MysqlPersistence")

    val database : AsyncSQLClient by lazy{
        MySQLClient.createShared(vertx,config)
    }
    fun sendQuery(query: String, handler: ((AsyncResult<ResultSet>) -> Unit)) {
        log.debug("got query: " + query)
        try {
            database.getConnection { con ->
                if (con.succeeded()) {
                    val conn = con.result()
                    conn.query(query) { result ->
                        if (result.succeeded()) {
                            val rs = result.result()
                            handler(Future.succeededFuture(rs))
                        } else {
                            log.error("query error ", result.cause())
                            handler(Future.failedFuture(result.cause()))
                        }
                        conn.close()
                    }
                } else {
                    log.error("connection error ", con.cause())
                    handler(Future.failedFuture(con.cause()))
                }
            }
        } catch(rh: Throwable) {
            log.error("error accessing database", rh)
        }
    }
    override fun delete(id: String, ack: (Boolean) -> Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit) {
        sendQuery("SELECT * FROM $collection WHERE ID='$objid' and deleted='0'"){result ->
            if(result.succeeded()){
                val resultSet=result.result()
                if(resultSet.numRows==0){
                    handler(Future.succeededFuture(null))
                }else if(resultSet.numRows>1){
                    handler(Future.failedFuture("ID is not unique: $objid"))
                }else{
                    val row=resultSet.rows[0]
                    handler(Future.succeededFuture(row))
                }

            }else{
                handler(Future.failedFuture(result.cause()))
            }
        }
    }

    override fun find(template: JsonObject, handler: (AsyncResult<List<JsonObject>>) -> Unit) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun flush(obj: AsyncPersistentObject, handler: (AsyncResult<Boolean>)->Unit) {
        database.getConnection(){con ->
            if(con.succeeded()){
                val conn=con.result()
                conn.execute(""){}
            }
        }
    }



}