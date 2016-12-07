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

/**
 * Created by gerry on 30.11.16.
 */
import ch.rgw.tools.json.get
import ch.rgw.tools.json.json_error
import ch.rgw.tools.json.json_ok
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressEventType
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.glacier.AmazonGlacierClient
import com.amazonaws.services.glacier.model.*
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Store arbitrary data on cheap "slow" storage provided by Amazon Glacier
 * The constructor takes a JsonObject which must contain at least the following fields:
 * * * user - the AmazonAWS user
 * * accountID - the accountID of the holder of the Amazon account
 * * accessKey - the public accessKey of the AmazonAWS user (does not need to be the same as the holder of the accountID)
 * * secretKey - the private secret key of the AmazonAWS user
 * * vault - the vault this instance should operate on
 */
class Glacier(val cfg: JsonObject) : ProgressListener {

    val log = LoggerFactory.getLogger("Glacier Backup")
    val cred = Credentials()
    val client = AmazonGlacierClient(cred)
    val s3=SimpleStore(cfg)
    var busy = false;
    var totalBytesTransferred = 0L

    init {
        client.setEndpoint(cred.getRegion())
    }

    /**
     * Send a File [file] to the [vault].
     * This method blocks until the transfer is complete, which might be a very long time.
     * The file may be arbitrarly large an will be sent in chunks. If a network error happens, the
     * failed chunk is sent again.
     * @return The archiveID of the stored object
     */
    fun transmit(vault: String, file: File): JsonObject {
        try {
            if (busy) {
                return json_error("busy")
            } else {
                busy = true
                val meta = s3.getJson(vault)
                totalBytesTransferred = 0L
                log.info("Transmitting file ${file.absolutePath} to Glacier")
                val desc = file.name
                val atm = ArchiveTransferManager(client, cred)
                val result = atm.upload(cred.getAccountID(), vault, desc, file, this)
                meta.put(file.name, result.archiveId)
                s3.putJson(vault, meta)
                busy = false;
                return json_ok().put("archiveID",result.archiveId);
            }
        }catch(ex: Exception){
            log.error(ex.message)
            ex.printStackTrace()
            return json_error(ex.message)
        }
    }

    /**
     * List all vaults of the current AmazonAWS user
     */
    fun listVaults() = client.listVaults(ListVaultsRequest().withAccountId(cred.getAccountID())).vaultList

    fun listVaultContents(vault:String) : List<VaultEntry>{
        val meta=s3.getJson(vault)
        val ret= ArrayList<VaultEntry>()
        for((key,id) in meta){
            ret.add(VaultEntry(key,id as String))
        }
        return ret
    }

    /**
     * Start an inventory of a [vault].
     * Note: This method will return immediately, but the result must be retrieved later with getJobResult(). Typically, it
     * will be available after several hours.
     * @return A Job ID to retrueve the result later
     */
    fun initGetInventory(vault: String) :String {
        val initJobRequest = InitiateJobRequest()
                .withVaultName(vault)
                .withJobParameters(JobParameters().withType("inventory-retrieval"))
        return client.initiateJob(initJobRequest).jobId
    }

    /**
     * Initiate a fetch of an object with the given [archiveID] from the goven [vault].
     * @return a JobID which can be used to retrieve the File later (after several hours)
     */
    fun initFetch(vault:String, archiveID:String):String {
        val initJobRequest=InitiateJobRequest()
        .withVaultName(vault)
        .withJobParameters(JobParameters().withType("archive-retrieval").withArchiveId(archiveID))
        return client.initiateJob(initJobRequest).jobId
    }

    /**
     * Ask for the state of a given [jobID] in the given [vault]
     * @return true if the Job is completed and the data is available.
     */
    fun requestJobState(vault:String,jobID: String)=client.describeJob(DescribeJobRequest().withJobId(jobID).withVaultName(vault)).completed

    /**
     * Get the data associated with a previous initFetch or initGetInventory. This will succeed only after requestJobState returned true.
     */
    fun getJobResult(vault:String, jobID: String)=client.getJobOutput(GetJobOutputRequest().withJobId(jobID).withVaultName(vault)).body


    /**
     * Fetch an object with the given [archiveID] from the given [vault] asynchronously.
     * Note: It wil take several hours before the [handler] is called.
     * On termination, the handler is called with an AsyncResult which can be succeeded or failed.
     * if it is succeeded, its result-field will be an InputStream to fetch the requested data.
     * It is mandatory to close and free this InputStream after reading.
     *
     */
    fun asyncFetch(vault:String,archiveID:String,handler:AsyncResultHandler<InputStream>){
        log.debug("trying to retrieve: ${archiveID}")
        val jobID=initFetch(vault,archiveID)
        val test=requestJobState(vault,jobID)
        val timer=fixedRateTimer("checkArchive",true,60000L,30*60000L, {
            log.debug("checking archive at ${Date()}")
            if(requestJobState(vault,jobID)){
                log.info("Result ready")
                val result=client.getJobOutput(GetJobOutputRequest().withJobId(jobID).withVaultName(vault))
                val ins=result.body.buffered()
                handler.handle(Future.succeededFuture(ins))
            }
        })
    }

    /**
     * Create a [vault]. If a vault with this name exists already in this instance's Region, the existing
     * vault is used.
     */
    fun createOrGetVault(name: String): Boolean {
        try {
            listVaults().forEach { vault ->
                if (vault.vaultName == name) {
                    println("found: ${vault.vaultARN}, ${vault.vaultName}")
                    return true;
                }
            }
            val result = client.createVault(CreateVaultRequest().withVaultName(name))
            return true
        } catch(ex: Exception) {
            log.error(ex.message)
            ex.printStackTrace()
            return false
        }
    }

    /**
     * Delete an Archivewith a given [id] from a [vault].
     * Note: It can take several hours before the archive disappeares effectively from the inventory.
     */
    fun deleteArchive(vault: String, id: String) {
        val meta = s3.getJson(vault)
        client.deleteArchive(DeleteArchiveRequest().withArchiveId(id).withVaultName(vault))
        for((key,value) in meta){
            if(value as String == id){
                meta.remove(key)
            }
        }
        s3.putJson(vault,meta)
    }

    /**
     * Delete a [vault]. This will succeed only, if the vault is empty.
     */
    fun deleteVault(name: String) {
        client.deleteVault(DeleteVaultRequest().withVaultName(name))
        s3.delete(name)
    }


    override fun progressChanged(pe: ProgressEvent) {
        val bytes = pe.bytesTransferred
        totalBytesTransferred += bytes
        when (pe.eventType) {
            ProgressEventType.TRANSFER_CANCELED_EVENT -> log.error("Transfer was cancelled after $totalBytesTransferred bytes.")
            ProgressEventType.TRANSFER_COMPLETED_EVENT -> log.info("Success: Transfer completed after $totalBytesTransferred bytes.")
            ProgressEventType.TRANSFER_FAILED_EVENT -> log.error("Transfer failed after $totalBytesTransferred bytes.")
            ProgressEventType.TRANSFER_PART_COMPLETED_EVENT -> log.info("Transferred part of $bytes, totalling $totalBytesTransferred")
            ProgressEventType.TRANSFER_PART_FAILED_EVENT -> log.error("Transfer of part failed after $totalBytesTransferred. Restarting this part.")
            ProgressEventType.TRANSFER_PART_STARTED_EVENT -> log.info("Started transfer of new part at $totalBytesTransferred")
            else -> log.error("Unknown event type received: ${pe.eventType}")
        }
    }

    inner class Credentials : AWSCredentials {
        override fun getAWSAccessKeyId() = cfg["accessKey"]
        override fun getAWSSecretKey() = cfg["secretKey"]
        fun getUser() = cfg["user"]
        fun getAccountID() = cfg["accountID"]
        fun getRegion() = cfg["region"]

    }

}

data class VaultEntry(val name:String, val id:String)
