package ch.rgw.ungrad_backup

/**
 * Created by gerry on 30.11.16.
 */
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class Glacier : AbstractVerticle(){
    val profileCredentialsProvider=ProfileCredentialsProvider()

    override fun start(startResult: Future<Void>){

    }
}