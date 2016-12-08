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


package ch.rgw.ungrad_backup

import ch.rgw.io.FileTool
import ch.rgw.tools.crypt.TwofishInputStream
import ch.rgw.tools.crypt.TwofishOutputStream
import ch.rgw.tools.crypt.Twofish_Algorithm
import ch.rgw.tools.json.decrypt
import ch.rgw.tools.json.encrypt
import ch.rgw.tools.json.get
import ch.rgw.tools.json.validate
import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import io.vertx.core.json.JsonObject
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * A very simple key-value store based on Amazon S3
 * Created by gerry on 04.12.16.
 *
 * Constructor takes a JsonObject with must contain at least the following fields:
 * * user - the AmazonAWS user
 * * accountID - the accountID of the holder of the Amazon account
 * * accessKey - the public accessKey of the AmazonAWS user (does not need to be the same as the holder of the accountID)
 * * secretKey - the private secret key of the AmazonAWS user
 * * s3bucket - the bucket this SimpleStore should operate on
 */
class SimpleStore(val cfg: JsonObject) {
    val cred = Credentials()
    val s3 = AmazonS3Client(cred)
    val bucket = cfg["s3bucket"]

    init {
        if (!cfg.validate("user:string", "accountID:string", "accessKey:string", "secretKey:string", "s3bucket:string")) {
            throw IllegalArgumentException("invalid config file for S3-SimpleStore ${cfg.encodePrettily()}")
        }
    }

    /**
     * Check if an object with a given [key] exists
     * @return true if such an objects exists
     */
    fun exists(key: String) = s3.doesObjectExist(bucket, key)

    /**
     * Store a JsonObject (encrypted)
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun putJson(key: String, value: JsonObject, keyObject: String = cfg["objectKey"] ?: cred.awsSecretKey!!) {
        put(key, value.encrypt(keyObject))
    }

    /**
     * Store a ByteArray
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun put(key: String, value: ByteArray) {
        val bucket = cfg["s3bucket"]
        if (!s3.doesBucketExist(bucket)) {
            s3.createBucket(bucket)
        }
        val meta = ObjectMetadata()
        meta.contentLength = value.size.toLong()
        meta.contentType = "Application/octet-stream"
        s3.putObject(bucket, key, value.inputStream(), meta)
    }

    /**
     * Store a ByteArray encrypted
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun putEncrypted(key: String, data: ByteArray, keyObject: String = cfg["objectKey"] ?: cred.awsSecretKey!!) {
        val tfkey = Twofish_Algorithm.makeKey(keyObject.toByteArray(Charset.forName("UTF-8")))
        val bos = ByteArrayOutputStream()
        val os = TwofishOutputStream(bos, tfkey)
        FileTool.copyStreams(data.inputStream(), os)
        os.close()
        put(key, bos.toByteArray())
    }

    /**
     * Retrieve an encrypted ByteArray
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun getEncrypted(key: String, keyObject: String = cfg["objectKey"] ?: cred.awsSecretKey!!): ByteArray {
        val tfkey = Twofish_Algorithm.makeKey(keyObject.toByteArray(Charset.forName("UTF-8")))
        val raw = get(key).inputStream()
        val tf = TwofishInputStream(raw, tfkey)
        val os = ByteArrayOutputStream()
        FileTool.copyStreams(tf, os)
        tf.close()
        os.close()
        return os.toByteArray()
    }

    /**
     * Retrieve an encrypted JsonObject
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun getJson(key: String, keyObject: String = cfg["objectKey"] ?: cred.awsSecretKey!!): JsonObject {
        try {
            val raw = get(key)
            return decrypt(raw, keyObject)
        } catch(ex: Exception) {
            return JsonObject()
        }
    }

    /**
     * Retrieve a Byte Array
     */
    @Throws(AmazonServiceException::class, SdkClientException::class)
    fun get(key: String): ByteArray {
        val bucket = cfg["s3bucket"]
        val inp = s3.getObject(bucket, key)
        val ret = ByteArrayOutputStream()
        FileTool.copyStreams(inp.objectContent.buffered(), ret)
        inp.close()
        return ret.toByteArray()
    }


    /**
     * Delete an Object
     */
    fun delete(key: String) {
        val bucket = cfg["s3bucket"]
        s3.deleteObject(bucket, key)
    }

    inner class Credentials : AWSCredentials {
        override fun getAWSAccessKeyId() = cfg["accessKey"]
        override fun getAWSSecretKey() = cfg["secretKey"]
        fun getUser() = cfg["user"]
        fun getAccountID() = cfg["accountID"]
        fun getRegion() = cfg["region"]

    }
}