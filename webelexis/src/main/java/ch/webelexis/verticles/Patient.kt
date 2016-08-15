package ch.webelexis.verticles

import ch.rgw.tools.JsonUtil
import ch.rgw.tools.SimpleObject

/**
 * Created by gerry on 14.08.16.
 */
class Patient(lastname: String, firstname: String, id: String) :JsonUtil(){
    init{
        put("lastname",lastname)
        put("firstname",firstname)
        put("id",id)
    }
}