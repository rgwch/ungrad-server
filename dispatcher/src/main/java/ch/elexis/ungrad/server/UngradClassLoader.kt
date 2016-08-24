package ch.elexis.ungrad.server

import java.io.ByteArrayOutputStream
import java.net.JarURLConnection
import java.net.URL

/**
 * Created by gerry on 14.08.16.
 */

class UngradClassLoader(val url: String) : ClassLoader() {
    // e.g. jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class

    override fun loadClass(name: String): Class<*> {
        val bytes = loadClassData(name)
        return if (bytes == null) {
            super.loadClass(name)
        }else {
            val clazz = defineClass(name, bytes, 0, bytes.size)
            resolveClass(clazz)
            clazz
        }
    }
/*
    fun loadClass(name: String, clazz: Class<*>) : Class<*>{
        val ret=loadClass(name)
        resolveClass(ret)
        return ret
    }
    */
    fun loadClassData(name: String): ByteArray? {
        val completeURL=url+"!/"+name.replace("\\.".toRegex(),"/")+".class"
        val url = URL(completeURL)
        val jar = url.openConnection() as JarURLConnection
        try {
            val entry = jar.jarEntry
            val jis = jar.jarFile.getInputStream(entry)
            val baos = ByteArrayOutputStream();
            jis.copyTo(baos)
            return baos.toByteArray()
        }catch(ex:Exception){
            return null;
        }
    }
}