package ch.elexis.ungrad.server

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by gerry on 06.08.16.
 */
class UIHandler : Handler<RoutingContext> {
    val df=SimpleDateFormat("E, dd MM yyyy HH:mm:ss z")
    init{
        df.timeZone=TimeZone.getTimeZone("UTC")
    }
    override fun handle(ctx: RoutingContext) {
        val date=Date()
        ctx.response().putHeader("Date",df.format(Date()))
        ctx.response().putHeader("Server","Elexis Ungrad Server")
        val reqpath=ctx.request().path()
        if(reqpath == "/" || reqpath == "/index.html" || reqpath=="/index.htm"){
            val rnd=UUID.randomUUID().toString()

        }else if(reqpath.contains("..")){
            ctx.response().setStatusCode(403).end()
        }

    }
}