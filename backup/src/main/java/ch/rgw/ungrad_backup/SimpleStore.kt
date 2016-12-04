package ch.rgw.ungrad_backup

import ch.rgw.io.FileTool
import ch.rgw.tools.crypt.TwofishInputStream
import ch.rgw.tools.crypt.TwofishOutputStream
import ch.rgw.tools.crypt.Twofish_Algorithm
import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.decrypt
import ch.rgw.tools.json.encrypt
import ch.rgw.tools.json.get
import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.sun.deploy.uitoolkit.impl.text.TextWindowFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * Created by gerry on 04.12.16.
 */
class SimpleStore(val cfg: JsonObject) {
    val cred=Credentials()
    val s3=AmazonS3Client(cred)

    fun exists(key:String):Boolean{
        val bucket=cfg["s3bucket"]
        return s3.doesObjectExist(bucket,key)
    }
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun put(key:String,value:JsonObject){
        put(key, value.encrypt(cfg["objectKey"]?:cred.awsSecretKey!!))
    }
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun put(key:String,value:ByteArray){
        val bucket=cfg["s3bucket"]
        if(!s3.doesBucketExist(bucket)){
            s3.createBucket(bucket)
        }
        val meta=ObjectMetadata()
        meta.contentLength=value.size.toLong()
        meta.contentType="Application/octet-stream"
        s3.putObject(bucket,key,value.inputStream(), meta)
    }
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun putEncrypted(key:String,data:ByteArray,keyObject:String=cfg["objectKey"]?:cred.awsSecretKey!!){
        val tfkey=Twofish_Algorithm.makeKey(keyObject.toByteArray(Charset.forName("UTF-8")))
        val bos=ByteArrayOutputStream()
        val os=TwofishOutputStream(bos,tfkey)
        FileTool.copyStreams(data.inputStream(),os)
        os.close()
        put(key,bos.toByteArray())
    }
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun getEncrypted(key: String, keyObject:String=cfg["objectKey"]?:cred.awsSecretKey!!) : ByteArray{
        val tfkey=Twofish_Algorithm.makeKey(keyObject.toByteArray(Charset.forName("UTF-8")))
        val raw=get(key).inputStream()
        val tf=TwofishInputStream(raw,tfkey)
        val os=ByteArrayOutputStream()
        FileTool.copyStreams(tf,os)
        tf.close()
        os.close()
        return os.toByteArray()
    }

    @Throws(AmazonServiceException::class,SdkClientException::class)
    fun getJson(key: String):JsonObject{
        try {
            val raw = get(key)
            return decrypt(raw, cfg["objectKey"] ?: cred.awsSecretKey!!)
        }catch(ex: Exception){
            return JsonObject()
        }
    }
    @Throws(AmazonServiceException::class,SdkClientException::class)
    fun get(key:String) : ByteArray{
        val bucket=cfg["s3bucket"]
        val inp=s3.getObject(bucket,key)
        val ret=ByteArrayOutputStream()
        FileTool.copyStreams(inp.objectContent.buffered(),ret)
        inp.close()
        return ret.toByteArray()
    }


    fun delete(key:String){
        val bucket=cfg["s3bucket"]
        s3.deleteObject(bucket,key)
    }
    inner class Credentials : AWSCredentials{
        override fun getAWSAccessKeyId() = cfg["accessKey"]
        override fun getAWSSecretKey() = cfg["secretKey"]
        fun getUser() = cfg["user"]
        fun getAccountID() = cfg["accountID"]
        fun getRegion() = cfg["region"]

    }
}