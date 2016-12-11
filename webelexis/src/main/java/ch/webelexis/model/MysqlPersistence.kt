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

import ch.rgw.tools.json.get
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.ResultSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by gerry on 22.10.2016.
 */
class MysqlPersistence(val config: JsonObject, val vertx: Vertx) : IPersistence {
    val log: Logger = LoggerFactory.getLogger("MysqlPersistence")

    val database: AsyncSQLClient by lazy {
        MySQLClient.createShared(vertx, config)
    }

    /**
     * Execute a list of [sql] commands. Execution is not parallel, but in the given order, each line waiting to
     * complete before sending the next.
     * @return a Future which resolves to true on success or carries the bad line on failure.
     */
    fun exec(sql: List<String>): Future<Boolean> {
        val ret = Future.future<Boolean>()
        database.getConnection { con ->
            if (con.succeeded()) {
                val conn = con.result()
                fun internal_exec(list: List<String>) {
                    if (list.isEmpty() && !ret.isComplete) {
                        ret.complete(true)
                    } else {
                        val line = list.first()
                        try {
                            conn.execute(line) {
                                if (it.succeeded()) {
                                    internal_exec(list.drop(1))
                                } else {
                                    ret.fail(line + " -\n - " + it.cause().message)
                                }
                            }
                        } catch(ex: Exception) {
                            log.error(ex.message)
                            ret.fail(ex.message)
                        }
                    }
                }
                internal_exec(sql)
            } else {
                ret.fail(con.cause())
            }
        }
        return ret
    }

    /**
     * Send a [query] to the database.
     */
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

    /**
     * Fetch a JsonObject, identified by the its [objid], from a named [collection] of the database.
     * calls the [handler] with the result of the query.
     * fails if there is more than one row with that objid in the collection.
     */
    override fun fetch(collection: String, objid: String, handler: (AsyncResult<JsonObject?>) -> Unit) {
        sendQuery("SELECT * FROM $collection WHERE ID='$objid' and deleted='0'") { result ->
            if (result.succeeded()) {
                val resultSet = result.result()
                if (resultSet.numRows == 0) {
                    handler(Future.succeededFuture(null))
                } else if (resultSet.numRows > 1) {
                    handler(Future.failedFuture("ID is not unique: $objid"))
                } else {
                    val row = resultSet.rows[0]
                    handler(Future.succeededFuture(row))
                }

            } else {
                handler(Future.failedFuture(result.cause()))
            }
        }
    }

    override fun find(template: SimpleObject, handler: (AsyncResult<List<JsonObject>>) -> Unit) {
        val sql = StringBuilder()
        sql.append("SELECT * FROM ${template.get("collection")} WHERE ")
        for ((key, value) in template.map) {
            val rname = template.fields.find {
                it.label == key
            }
            sql.append("$rname LIKE ${value.toString().replace('*', '%')} AND ")
        }
        sql.append("deleted='0';")
        sendQuery(sql.toString()) {
            handler(Future.succeededFuture(it.result().rows))
        }
    }


    /**
     * Write an AsyncPersistentObject [obj] to the backing store. The method will check if the remote object
     * is newer and fail if so.
     * Success or failure is reported to the [handler]
     */
    override fun flush(obj: SimpleObject, handler: (AsyncResult<Boolean>) -> Unit) {
        database.getConnection() { con ->
            if (con.succeeded()) {
                val conn = con.result()
                fun insert(): Unit {
                    val sql = apo2sql(obj)
                    conn.update(sql) { updateresult ->
                        if (updateresult.succeeded()) {
                            handler(Future.succeededFuture(true))
                        } else {
                            handler(Future.failedFuture(updateresult.cause()))
                        }
                    }
                }
                conn.query("SELECT lastupdate FROM ${obj.get("collection")} where ID='${obj.get("id")}'") { remoteObj ->
                    if (remoteObj.succeeded()) {
                        val rs = remoteObj.result()
                        if (rs.numRows == 0) {
                            insert()
                        } else if (rs.numRows == 1) {
                            val ro1 = rs.rows[0]
                            val remote_lastupdate = ro1.getLong("lastupdate")
                            if (obj.getLong("lastupdate") < remote_lastupdate) {
                                handler(Future.failedFuture("conflict: remote update"))
                            } else {
                                insert()
                            }
                        } else {
                            handler(Future.failedFuture("ID is not unique " + obj["id"]))
                        }

                    } else {
                        handler(Future.failedFuture(remoteObj.cause()))
                    }
                }
                conn.close()

            } else {
                log.error("failed to connect ", con.cause())
                handler(Future.failedFuture(con.cause()))
            }

        }

    }

    fun apo2sql(obj: SimpleObject): String {
        fun findField(label: String): String? {
            return obj.fields.find {
                it.label == label
            }?.label
        }

        val fields = StringBuilder()
        val values = StringBuilder()
        val upd = StringBuilder()
        for ((key, value) in obj.map) {
            fields.append(findField(key)).append(",")
            values.append("'$value',")
            if (key != "id") {
                upd.append("$key='$value',")
            }
        }
        val now = System.currentTimeMillis()
        val ret = "INSERT INTO ${obj["collection"]} (" + fields.toString() + "deleted,lastupdate) VALUES (" +
                values.toString() + "'0',$now) ON DUPLICATE KEY UPDATE " + upd.toString() +
                "deleted='0', lastupdate=$now;"
        return ret
    }

}