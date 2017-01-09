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

package ch.rgw.lucinda

import ch.rgw.tools.crypt.makeHash
import ch.rgw.tools.json.json_ok
import ch.rgw.tools.json.validate
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.IOException
import java.nio.file.*
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.TimeUnit

const val ADDR_WATCHER_START = BASEADDR + ".watcher.start"
const val ADDR_WATCHER_STOP = BASEADDR + ".watcher.stop"
/** Full rescan or first scan of watch directories */
const val ADDR_WATCHER_RESCAN = BASEADDR + ".watcher.rescan"
var refiner: Refiner = DefaultRefiner()
val watchedDirs = ArrayList<Path>()
fun makeID(file: Path): String = makeHash(file.toFile().absolutePath)


/**
 * Observe one ore more directories for file changes. If such changes occur, add, remove or renew concerned
 * files with the lucinda's index.
 * Usage: Send an ADDR_START message with a JsonObject cotaining a JsonArray 'dirs'.
 * Created by gerry on 25.04.16.
 */
class Autoscanner(val dispatcher: Dispatcher) : AbstractVerticle() {

    val eb: EventBus by lazy {
        vertx.eventBus()
    }
    val watcher: WatchService = FileSystems.getDefault().newWatchService()
    val keys = HashMap<WatchKey, Path>()
    var running = false
    val fileImporter = dispatcher.importer

    override fun start(startResult: Future<Void>) {
        super.start()

        eb.consumer<JsonObject>(ADDR_WATCHER_START) { msg ->
            val j = msg.body()
            if (j.validate("dirs:array", "interval:number")) {
                log.debug("got start message ${Json.encodePrettily(j)}")
                register(j.getJsonArray("dirs"))
                vertx.executeBlocking<Unit>({
                    future ->
                    running = true
                    loop()
                    future.complete()

                }, { result ->
                    if (result.succeeded()) {
                        running = false
                    }
                })
                msg.reply(json_ok())
            } else {
                msg.fail(0, "bad parameters")
            }
        }
        eb.consumer<JsonObject>(ADDR_WATCHER_STOP) {
            log.info("got stop message")
            running = false
        }
        eb.consumer<JsonObject>(ADDR_WATCHER_RESCAN) { msg ->
            log.info("got rescan message")
            watchedDirs.forEach {
                rescan(it)
            }
            msg.reply(json_ok())
        }
        startResult.complete()
    }

    /**
     * Main loop: Runs until running==false or all WatchKeys are removed
     */
    fun loop() {
        while (running) {
            // Wait up to a minute for events, then check if we should still be watching
            watcher.poll(10, TimeUnit.SECONDS)?.let { key ->
                keys[key]?.let { dir ->
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        val ev = event as WatchEvent<Path>
                        val name = ev.context()
                        val child = dir.resolve(name)
                        log.debug("Event: ${event.kind().name()}, file: $child")
                        when (kind) {
                            ENTRY_CREATE -> addFile(child, key)
                            ENTRY_DELETE -> removeFile(child, key)
                            ENTRY_MODIFY -> checkFile(child, key)
                            OVERFLOW -> rescan(dir)
                            else -> log.warn("unknown event kind ${kind.name()}")
                        }
                    }
                }
            }
            if (keys.isEmpty()) {
                running = false;
                return
            }
        }
    }

    /**
     * The WatchKey ist triggered immediately if a file is touched. Give the system some time to complete the file operation
     * before running the autoscanner job.
     */
    fun delay(exe: () -> Unit) {
        vertx.setTimer(1000L) {
            exe()
        }
    }

    /**
     * Register the given directories with the WatchService
     * @param dirs: JsonArray of Strings denoting Paths
     */
    @Throws(IOException::class)
    fun register(dirs: JsonArray) {
        dirs.forEach {
            val dir = Paths.get(it as String)
            watchedDirs.add(dir)
            if (!Files.exists(dir)) {
                Files.createDirectories(dir)
                log.info("created directory $dir")
            }
            rescan_detached(dir)
        }
    }

    fun rescan_detached(dir: Path) {
        vertx.executeBlocking<Int>({ future ->
            rescan(dir)
            future.complete()
        }, { result ->
            if (result.succeeded()) {
                log.info("imported $dir")
            } else {
                log.error("import $dir failed (${result.cause().message})")
            }
        })

    }

    fun rescan(dir: Path) {
        log.debug("Autoscanner: rescan launched")

        Files.walkFileTree(dir, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                try {
                    checkFile(file, null)
                } catch(e: Exception) {
                    log.error("Exception while checking ${file.toAbsolutePath()}, ${e.message}")
                }
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (!(keys.containsValue(dir))) {
                    val key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
                    log.info("added watch key for ${dir.toAbsolutePath()}")
                    keys.put(key, dir)
                }
                return FileVisitResult.CONTINUE
            }
        })
        log.debug("Autoscanner rescan finished")

    }

    /**
     * Check a file to watch. <br />
     * If it is a normal file: Test if it exists in the index
     * <ul>
     *     <li>if it exists in the index: test if it has the same checksum, and if not: reimport</li>
     *     <li>if it does not yet exist, import it
     * </ul>
     */
    fun checkFile(file: Path, watchKey: WatchKey?) {
        log.info("Autoscanner", "checkFile")
        if (!exclude(file)) {
            if (Files.isRegularFile(file) && (!Files.isHidden(file))) {
                val absolute = file.toFile().absolutePath
                log.info("checking $absolute")
                val id = makeID(file)
                dispatcher.indexManager?.getDocument(id) ?: addFile(file, watchKey)
            }
        }
        checkKey(watchKey)
    }

    private fun exclude(file: Path): Boolean {
        val fname = file.fileName.toString()
        return (fname.startsWith(".") || Files.isHidden(file) || (Files.size(file) == 0L)
                || (fname.endsWith(".meta")))

    }

    private fun checkKey(key: WatchKey?) {
        if (key != null) {
            if (!key.reset()) {
                keys.remove(key)
            }
        }
    }

    /**
     * Add an Item. If it is a directory, add it to the watch list. If it is a file, add it to the index
     */
    fun addFile(file: Path, watchKey: WatchKey?) {
        if (!exclude(file)) {
            val filename = file.toFile().absolutePath
            log.info("adding $filename")
            if (Files.isDirectory(file, NOFOLLOW_LINKS)) {
                val key = file.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
                keys.put(key, file)
                checkKey(watchKey)
            } else {
                val fileMetadata = refiner.preProcess(filename, JsonObject()).put("_id", makeID(file))
                val result = fileImporter.process(file, fileMetadata)
                if (!result.isNullOrEmpty()) {
                    val errmsg = "import ${file.toAbsolutePath()} failed." + result
                    log.error(errmsg)
                    vertx.eventBus().publish(Communicator.FUNC_ERROR.addr, JsonObject().put("status", "error").put("message", errmsg))
                }
                checkKey(watchKey)
            }
        }
    }

    /**
     * Remove a deleted file from the index
     */
    fun removeFile(file: Path, watchKey: WatchKey) {
        if (!exclude(file)) {
            val absolute = file.toFile().absolutePath
            log.info("removing $absolute")
            val id = makeID(file)
            dispatcher.indexManager?.removeDocument(id)
            checkKey(watchKey)
        }
    }

}


