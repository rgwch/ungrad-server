package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.asyncsql.PostgreSQLClient
import org.slf4j.LoggerFactory
import java.sql.ResultSet

/**
 * Created by gerry on 14.08.16.
 */
class Patients(val cfg: JsonObject) : AbstractVerticle() {

    val database:AsyncSQLClient by lazy{
        if(cfg.getString("type","mysql")=="mysql"){
            MySQLClient.createShared(vertx,cfg)
        }else{
            PostgreSQLClient.createShared(vertx,cfg)
        }
    }
    override fun start(){

        vertx.eventBus().consumer<JsonObject>(LIST) { msg ->
            database.getConnection { result ->
                if(result.succeeded()){
                    val conn=result.result()
                    val parm=msg.body().getString("query")
                    conn.queryWithParams(PATLIST, JsonArray().add(parm).add(parm)) {answer ->
                        if(answer.succeeded()){
                            val rs=answer.result() as ResultSet
                            val ret=JsonArray()
                            while(rs.next() != null){
                                val patient=Patient(rs.getString("Bezeichnung1"),rs.getString("Bezeichnung2"),rs.getString("id"))
                                patient.put("patnr",rs.getString("patientnr"))
                                ret.add(patient)
                            }
                            msg.reply(JsonUtil.create("status:ok").put("result",ret))
                        }else{
                            log.error("Error executing PATLIST with ${msg.body().encodePrettily()}", answer.cause())
                            msg.reply(JsonUtil.create("status:error","message:${answer.cause().message}"))
                        }
                    }

                }else{
                    log.error("could not connect to database",result.cause())
                }
            }
        }
    }

    override fun stop(){
        database.close()
    }
    companion object{
        val BASE_ADDR="ch.webelexis.patients."
        val LIST= BASE_ADDR+"list"
        val log=LoggerFactory.getLogger(javaClass)
        val PATLIST="SELECT patientnr,id,Bezeichnung1,Bezeichnung2,Bezeichnung3,FROM KONTAKT WHERE Bezeichnung1 like ? OR Bezeichnung2 like ?"
    }
}