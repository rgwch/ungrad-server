package ch.rgw.ungrad_backup

import ch.rgw.io.FileTool
import ch.rgw.tools.json.JsonUtil
import ch.rgw.tools.json.decrypt
import ch.rgw.tools.json.encrypt
import ch.rgw.tools.json.get
import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import java.io.ByteArrayOutputStream

/**
 * Created by gerry on 04.12.16.
 */
class SimpleStore(val cfg: JsonObject) {
    val cred=Credentials()
    val s3=AmazonS3Client(cred)

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

    @Throws(AmazonServiceException::class,SdkClientException::class)
    fun getJson(key: String):JsonObject{
        val raw=get(key)
        return decrypt(raw,cfg["objectKey"]?:cred.awsSecretKey!!)
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