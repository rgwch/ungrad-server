package ch.elexis.ungrad.server

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider

/**
 * Created by gerry on 04.08.16.
 */

class AccessController(val bootstrapIdentity: JsonObject) : AuthProvider {
    override fun authenticate(identity: JsonObject?, handler: Handler<AsyncResult<io.vertx.ext.auth.User>>?) {
        val userid = identity?.getString("username")
        val pwd = identity?.getString("password")
        val entry = bootstrapIdentity.getJsonObject(userid)
        if (entry == null) {
            handler?.handle(AuthResult(null))
        } else {
            if (pwd.equals(entry.getString("password"))) {
                handler?.handle(AuthResult(User(userid, this)))
            }
        }
    }


    fun hasRole(user:User, role: String?) : Boolean{
        val entry=bootstrapIdentity.getJsonObject(user.userid)
        if(entry!=null){
            val roles=entry.getJsonArray("roles")
            if(roles.contains(role)){
                return true
            }
        }
        return false
    }

}

class AuthResult(val user: User?) : AsyncResult<io.vertx.ext.auth.User> {
    override fun cause(): Throwable {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun failed(): Boolean {
        return user == null
    }

    override fun result(): User? {
        return user
    }

    override fun succeeded(): Boolean {
        return user != null
    }

}

class User(val userid: String?, ac: AccessController) : AbstractUser() {
    var lastName = ""
    var firstName = ""
    var provider: AccessController = ac

    override fun principal(): JsonObject {
        return JsonObject().put("username", userid).put("lastname", lastName).put("firstname", firstName)
    }


    override fun setAuthProvider(p0: AuthProvider?) {
        provider = p0 as AccessController
    }

    override fun doIsPermitted(role: String?, handler: Handler<AsyncResult<Boolean>>?) {
        if(provider.hasRole(this,role)){
            handler?.handle(object: AsyncResult<Boolean>{
                override fun cause(): Throwable {
                    return Exception("operation not permitted")
                }

                override fun failed(): Boolean {
                    return false;
                }

                override fun result(): Boolean {
                    return true;
                }

                override fun succeeded(): Boolean {
                    return true
                }

            })
        }else {
            handler?.handle(object : AsyncResult<Boolean> {
                override fun cause(): Throwable {
                    return Exception("operation not permitted")
                }

                override fun failed(): Boolean {
                    return true;
                }

                override fun result(): Boolean {
                    return false;
                }

                override fun succeeded(): Boolean {
                    return false
                }


            })
        }
    }

}