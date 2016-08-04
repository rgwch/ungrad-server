package ch.elexis.ungrad.server

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

/**
 * Created by gerry on 04.08.16.
 */

class AccessController : AuthProvider{
    override fun authenticate(identity: JsonObject, result: Handler<AsyncResult<User>>?) {

    }

}


class User(val userid: String) : AbstractUser(){
    var lastName=""
    var firstName=""
    var provider:AuthProvider?=null

    override fun principal(): JsonObject {
        return JsonObject().put("username",userid).put("lastname",lastName).put("firstname",firstName)
    }


    override fun setAuthProvider(p0: AuthProvider?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doIsPermitted(p0: String?, p1: Handler<AsyncResult<Boolean>>?) {

    }

}