import ch.rgw.io.FileTool;
import ch.rgw.ungrad_backup.Chopper;
import ch.rgw.ungrad_backup.Reducer;
import ch.rgw.ungrad_backup.Scp;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gerry on 01.12.16.
 */
public class Test_Backup {
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

    @Test
    public void testReduce() throws Exception{
        File dir=new File(testDir,"files");
        dir.mkdirs();
        File test1=new File(dir,"zaa");
        File test2=new File(dir,"abc");
        File test3=new File(dir,"czya");
        FileTool.writeRandomFile(test1,1000);
        FileTool.writeRandomFile(test2,1000);
        FileTool.writeRandomFile(test3,1000);
        File archiveDir=new File(testDir,"archive");
        archiveDir.mkdir();
        Reducer reducer=new Reducer();
        reducer.reduce(dir,archiveDir,new Comparator<File>(){

            @Override
            public int compare(File t0, File t1) {
                return t0.getName().compareTo(t1.getName());
            }
        });
        Assert.assertTrue(0==dir.listFiles().length);
        File[] target=archiveDir.listFiles();
        Assert.assertTrue(1==target.length);
        Assert.assertTrue("abc".equals(target[0].getName()));
    }

    @Test
    public void testSCP() throws Exception{
        File test=new File(testDir,"test");
        FileTool.writeRandomFile(test,2000L);
        JsonObject cfg=new JsonObject();
        cfg.put("scp-user","dummy").put("scp-pwd","dummy").put("scp-host","192.168.0.1").put("scp-directory","scpbackup");
        Scp scp=new Scp(cfg);
        Assert.assertTrue(scp.transmit(test));
        test.renameTo(new File(testDir,"orig"));
        File fetched=scp.fetch("test",new File(testDir));
        byte[] b1=FileTool.readFile(new File(testDir,"orig"));
        byte[] b2=FileTool.readFile(new File(testDir,"test"));
        Assert.assertArrayEquals(b1,b2);
    }
}
