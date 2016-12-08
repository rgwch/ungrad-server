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

import ch.rgw.tools.json.json_error
import ch.rgw.tools.json.json_ok
import io.vertx.core.json.JsonObject
import java.io.File
import java.util.*

/**
 * Archive one file from a directory and delete all files
 * Created by gerry on 02.12.16.
 */
class Reducer {

    /**
     * sort all files (but not subdirectories) of [dir] with a user-supplied Comparator.
     * The first file resulting from this sort is copied to the [archive] dir. Then,
     * all Files (but not subdirectories) in [dir] are deleted.
     * @return the archived file or null (if [dir] did not contain any files)
     */
    fun reduce(dir: File, archive: File, sorter: Comparator<File>): JsonObject {
        val files = dir.listFiles { file ->
            !file.isDirectory()
        }.sortedArrayWith(sorter)
        if (files.size > 0) {
            val toKeep = files.get(0)
            val archived = toKeep.copyTo(File(archive, toKeep.name), true)
            files.forEach { file -> file.delete() }
            return json_ok().put("survivor", archived.absolutePath)
        }
        return json_error("empty directory")
    }
}