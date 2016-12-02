package ch.rgw.ungrad_backup

import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * Created by gerry on 02.12.16.
 */
class Reducer {
    
    fun reduce(dir: File, archive:File, sorter: Comparator<File>) : File?{
        val files=dir.listFiles{ file ->
            !file.isDirectory()
        }.sortedArrayWith(sorter)
        if(files.size>0){
           val toKeep=files.get(0)
            val archived=toKeep.copyTo(archive,true)
            files.forEach { file -> file.delete() }
            return archived;
        }
        return null;
    }
}