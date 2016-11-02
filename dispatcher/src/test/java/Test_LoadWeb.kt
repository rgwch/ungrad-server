import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.net.URL
import java.net.URLClassLoader

/**
 * Created by gerry on 06.09.16.
 */


class Test_LoadWeb {

    @Test
    @Ignore fun findResource() {
        val cl = URLClassLoader(Array<URL>(1, { URL("file:/home/gerry/git/ungrad-server/dispatcher/target/ungrad-server-dispatcher-0.1.0-SNAPSHOT.jar") }))
        val ins = cl.findResource("web/index.html")
        /*
        while(ins.hasMoreElements()){
            val file=ins.nextElement()
            val inps=file.openStream()
            FileTool.copyStreams(inps,System.out)
            println(file)
        }
        */
        val ins3 = cl.getResource("")
        val ins4 = cl.getResource("../ungrad-server-dispatcher-0.1.0-SNAPSHOT.jar!/web/index.html")
        Assert.assertNotNull(ins)

    }
}