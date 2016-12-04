package ch.rgw.ungrad_backup

/**
 * Created by gerry on 30.11.16.
 */
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject

class Glacier(val cfg: JsonObject){
    val profileCredentialsProvider=ProfileCredentialsProvider()


}