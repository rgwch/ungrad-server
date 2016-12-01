import ch.rgw.io.FileTool;
import ch.rgw.ungrad_backup.Chopper;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by gerry on 01.12.16.
 */
public class Test_Chopper {
    static String testDir="target/test";

    @Before
    public void setUp(){
        FileTool.deltree(testDir);
        File dir=new File(testDir);
        dir.mkdirs();
    }
    @Test
    public void testChop() throws Exception{
        Chopper chopper=new Chopper();
        File testFile=new File(testDir,"testFile");
        FileTool.writeRandomFile(testFile,64001L);
        File dir=new File(testDir,"dest");
        chopper.chop(testFile,dir,18000,"password".getBytes());
        testFile.renameTo(new File(testDir,"old"));
        chopper.unchop(dir,new File(testDir),"password".getBytes());
        byte[] orig=FileTool.readFile(new File("testDir","old"));
        byte[] decoded=FileTool.readFile(new File("testDir","testFile"));
        Assert.assertArrayEquals(orig,decoded);
    }
}
