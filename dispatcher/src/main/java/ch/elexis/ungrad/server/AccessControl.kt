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
package ch.elexis.ungrad.server

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AbstractUser
import io.vertx.ext.auth.AuthProvider

/**
 * Created by gerry on 04.08.16.
 */

class AccessController(val bootstrapIdentity: JsonObject) : AuthProvider {
    override fun authenticate(identity: JsonObject?, handler: Handler<AsyncResult<io.vertx.ext.auth.User>>?) {
        if (config.getString("mode", "prod") == "debug") {
            handler?.handle(Future.succeededFuture(User("admin", this)))
        } else {
            val userid = identity?.getString("username")
            val pwd = identity?.getString("password")
            val entry = bootstrapIdentity.getJsonObject(userid)
            if (entry == null) {
                handler?.handle(Future.failedFuture("bad userid or password"))
            } else {
                if (pwd.equals(entry.getString("password"))) {
                    handler?.handle(Future.succeededFuture(User(userid, this)))
                } else {
                    handler?.handle(Future.failedFuture("bad userid or password"))
                }
            }
        }
    }


    fun hasRole(user: User, role: String?): Boolean {
        val entry = bootstrapIdentity.getJsonObject(user.userid)
        if (entry != null) {
            val roles = entry.getJsonArray("roles")
            if (roles.contains(role)) {
                return true
            }
            if (roles.contains("admin")) {
                return true;
            }
        }
        return false
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

    override fun doIsPermitted(role: String?, handler: Handler<AsyncResult<Boolean>>) {
        val usr = this
        handler.handle(Future.succeededFuture(provider.hasRole(usr, role)))
        /*
        handler.handle(object : AsyncResult<Boolean> {
            override fun cause(): Throwable {
                return Exception("operation not permitted")
            }

            override fun failed(): Boolean {
                return false;
            }

            override fun result(): Boolean {
                return provider.hasRole(usr, role);
            }

            override fun succeeded(): Boolean {
                return true
            }

        })
        */
    }
}

