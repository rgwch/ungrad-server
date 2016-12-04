package ch.rgw.ungrad_backup

/**
 * Created by gerry on 30.11.16.
 */
import ch.rgw.tools.json.get
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

    @Throws(Exception::class)
    fun transmit(vault: String, file: File): String {
        if (busy) {
            return ""
        } else {
            busy = true
            val meta=s3.getJson(vault)
            totalBytesTransferred = 0L
            log.info("Transmitting file ${file.absolutePath} to Glacier")
            val desc = file.name
            val atm = ArchiveTransferManager(client, cred)
            val result = atm.upload(cred.getAccountID(), vault, desc, file, this)
            meta.put(file.name,result.archiveId)
            s3.put(vault,meta)
            busy = false;
            return result.archiveId;
        }

    }

    fun listVaults() = client.listVaults(ListVaultsRequest().withAccountId(cred.getAccountID())).vaultList

    fun listVaultContents(vault:String) : List<VaultEntry>{
        val meta=s3.getJson(vault)
        val ret= ArrayList<VaultEntry>()
        for((key,id) in meta){
            ret.add(VaultEntry(key,id as String))
        }
        return ret
    }

    fun initGetInventory(vault: String) :String {
        val initJobRequest = InitiateJobRequest()
                .withVaultName(vault)
                .withJobParameters(JobParameters().withType("inventory-retrieval"))
        return client.initiateJob(initJobRequest).jobId
    }
    fun initFetch(vault:String, archiveID:String):String {
        val initJobRequest=InitiateJobRequest()
        .withVaultName(vault)
        .withJobParameters(JobParameters().withType("archive-retrieval").withArchiveId(archiveID))
        return client.initiateJob(initJobRequest).jobId
    }

    fun requestJobState(vault:String,jobID: String)=client.describeJob(DescribeJobRequest().withJobId(jobID).withVaultName(vault)).completed
    fun getJobResult(vault:String, jobID: String)=client.getJobOutput(GetJobOutputRequest().withJobId(jobID).withVaultName(vault)).body


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

    fun deleteArchive(vault: String, id: String) {
        val meta = s3.getJson(vault)
        client.deleteArchive(DeleteArchiveRequest().withArchiveId(id).withVaultName(vault))
        for((key,value) in meta){
            if(value as String == id){
                meta.remove(key)
            }
        }
        s3.put(vault,meta)
    }

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
