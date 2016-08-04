package ch.elexis.ungrad.server.console

import ch.rgw.tools.Configuration
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

/**
 * Created by gerry on 31.07.16.
 */
class Console(val config:Configuration) : AbstractVerticle() {
  val API="1.0/ungrad-server"
    val eb: EventBus by lazy {
        vertx.eventBus()
    }



    override fun start() {
        super.start()

        fun register(func: RegSpec) {
            eb.send<JsonObject>(REGISTER_ADDRESS, JsonObject()
                    .put("ebaddress", BASEADDR + func.addr)
                    .put("rest", "${API}/${func.rest}").put("method", func.method), RegHandler(func))
        }

        register(FUNC_LIST) // list of registered servers
        register(FUNC_INFO) // Info about a server
        register(FUNC_SET)

        vertx.eventBus().consumer<JsonObject>(BASEADDR+ FUNC_LIST.addr){ msg ->
            val j=msg.body()
            log.info("got message ${FUNC_LIST.addr}")

        }

        val registerMsg=JsonObject().put("rest","1.0/console/list")

    }

    class RegHandler(val func: RegSpec) : AsyncResultHandler<Message<JsonObject>> {
        override fun handle(result: AsyncResult<Message<JsonObject>>) {
            if (result.failed()) {
                log.error("could not register ${func.addr} for ${func.rest}")
            }
        }

    }

    data class RegSpec(val addr: String, val rest: String, val method: String)

}