package ch.elexis.ungrad.server

import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.*

/**
 * Created by gerry on 04.08.16.
 */
class JsonUtil: JsonObject {

    constructor(jo: JsonObject=JsonObject()): super(){
      mergeIn(jo)
    }
    constructor(json: String) :super(json){

    }
    fun replace(other: JsonObject){
        clear()
        mergeIn(other)
    }


    /**
     * get a required integer value

     * @param field field to get
     * @param min   minimum accepted value
     * @param max   maximum accepted value
     * @return the integer from the field "field"
     * @throws ParametersException if "field" was not set, or if the value was not between (not
     * *                             including) min and max.
     */
    @Throws(ParametersException::class)
    fun getInt(field: String, min: Int, max: Int): Int {
        if (!containsKey(field)) {
            throw ParametersException("field $field was not set.")
        } else {
            val `val` = getInteger(field)!!
            if (`val` < min || `val` > max) {
                throw ParametersException("field value out of range")
            } else {
                return `val`
            }
        }
    }

    /**
     * Get a String value

     * @param field   field to get
     * @param pattern regexp-Pattern the String must match
     * @param emptyok if true: missing field is acceptable
     * @return
     * @throws ParametersException if the value is not set and emptyok was false, or if the value
     * *                             was set but did nor match the pattern.
     */
    @Throws(ParametersException::class)
    operator fun get(field: String, pattern: String, emptyok: Boolean): String {
        val raw = getString(field)
        if (raw == null || raw.length == 0) {
            if (emptyok) {
                return raw
            } else {
                throw ParametersException("field $field was not set.")
            }
        }
        if (raw.matches(pattern.toRegex())) {
            return raw
        } else {
            throw ParametersException("value of $field does not match expected criteria")
        }
    }

    /**
     * Get an optional String field without checks

     * @param field        field to get
     * @param defaultValue value to return if the field is nor set
     * @return the requested field
     */
    fun getOptional(field: String, defaultValue: String): String {
        val raw = getString(field)
        if (raw != null) {
            return raw
        } else {
            return defaultValue
        }
    }

    /**
     * Get an optional Array without checks

     * @param name         name of the field to get
     * @param defaultValue value to return, if the field was not set
     * @return ClassCastException if the field exists, but is not a JsonArray
     */
    fun getArray(name: String, defaultValue: JsonArray): JsonArray {
        val ret = getJsonArray(name)
        return ret ?: defaultValue
    }


    /**
     * send a canned "error" response (message with only one field:
     * "status":"error")
     */
    fun makeError(): JsonUtil {
        put("status", "error")
        return this
    }

    /**
     * send a custom "error" response

     * @param msg a String describing the error. The method sends
     * *            {"status":"error","message":msg}
     */
    fun makeError(msg: String): JsonUtil {
        put("status", "error").put("message", msg)
        return this
    }

    /**
     * send a canned "ok" response
     */
    fun makeOk(): JsonUtil {
        put("status", "ok")
        return this
    }

    /**
     * send a custom "ok" response

     * @param msg a String to add to the response. The method sends
     * *            {"status":"ok","message":msg}
     */
    fun makeOk(msg: String): JsonUtil {
        put("status", "ok").put("message", msg)
        return this
    }

    /**
     * send a custom status message

     * @param msg any String describing a status. The method sends {"status":msg}
     */
    fun makeStatus(msg: String): JsonUtil {
        put("status", msg)
        return this
    }

    /**
     * generate a String representation of the original message.body() in the
     * constructor
     */
    override fun toString(): String {
        return encodePrettily()
    }


    companion object {
        // /^\d{1,2}\.\d{1,2}\.(?:\d{4}|\d{2})$/
        val ELEXISDATE = "[12][09][0-9]{6,6}"
        val NAME = "[0-9a-zA-ZäöüÄÖÜéàèß \\.-]+"
        val WORD = "[a-zA-ZäöüÄÖÜéàèß]+"
        val NOTEMPTY = ".+"
        val DATE = "[0-3]?[0-9]\\.[01]?[0-9]\\.[0-9]{2,4}"
        val PHONE = "\\+?[0-9  -/]{7,20}"
        val MAIL = ".+@[a-zA-Z_0-9\\.]*[a-zA-Z_0-9]{2,}\\.[a-zA-Z]{2,3}"
        val TIME = "[0-2][0-9]:[0-5][0-9]"
        val TEXT = "[^%\\*;:]+"
        val URL = "https?://[a-zA-Z0-9-_]+(\\.[a-zA-Z0-9-_]+)*(:[0-9]+)?"
        val IP = "[0-2]?[0-9]?[0-9]\\.[0-2]?[0-9]?[0-9]\\.[0-2]?[0-9]?[0-9]\\.[0-2]?[0-9]?[0-9]"
        val ZIP = "[A-Za-z 0-9]{4,8}"
        val UID = "[a-zA-Z0-9][a-fA-F0-9-]{8,}"
        val NUMBER = "[0-9]+"

        /**
         * Create a JsonObject from a file. // Comments are stripped before parsing

         * @param fpath filename (with relative or absolute path)
         * *
         * @return a JsonObject created from that file
         * *
         * @throws IOException     if the file was not found or could not be read
         * *
         * @throws DecodeException if the file was not a valid Json Object
         */
        @Throws(IOException::class, DecodeException::class)
        fun createFromFile(file: File): JsonUtil {

            if (!file.exists()) {
                println(file.absolutePath)
                throw IOException("File not found " + file.absolutePath)
            }
            val buffer = CharArray(file.length().toInt())
            val fr = FileReader(file)
            fr.read(buffer)
            fr.close()
            val conf = String(buffer).replace("//\\s+.+\\r?\\n+\\r?".toRegex(), "")
            return JsonUtil(conf)
        }

        @Throws(IOException::class, DecodeException::class)
        fun createFromStream(`is`: InputStream): JsonUtil {
            val ir = InputStreamReader(`is`)
            val sb = StringBuilder(10000)
            val buffer = CharArray(2048)
            var chars = 0
            do {
                chars = ir.read(buffer)
                sb.append(buffer, 0, chars)
            } while (chars == buffer.size)
            ir.close()
            var conf = sb.toString().replace("//\\s+.+\\r?\\n+\\r?".toRegex(), "")
            conf = conf.replace("[\\n\\r]".toRegex(), " ")
            conf = conf.replace("\\/\\*.+?\\*\\/".toRegex(), "")
            val ret = JsonUtil(conf)
            return ret

        }
        fun load(vararg names: String) : JsonUtil{
            for(name in names){
                var file= File(name)
                if(file.exists() && file.canRead()){
                    return createFromFile(file)
                }else {
                    var ins = ClassLoader.getSystemClassLoader().getResourceAsStream(name)
                    if (ins != null) {
                        return createFromStream(ins)
                    }
                }

            }
            return JsonUtil()
        }
    }
}

class ParametersException(text: String) : Exception(text) {
    companion object {
        private val serialVersionUID = 2984974711577234030L
    }
}

