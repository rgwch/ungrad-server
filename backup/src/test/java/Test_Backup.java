import ch.rgw.io.FileTool;
import ch.rgw.tools.json.JsonUtil;
import ch.rgw.ungrad_backup.Chopper;
import ch.rgw.ungrad_backup.Glacier;
import ch.rgw.ungrad_backup.Reducer;
import ch.rgw.ungrad_backup.Scp;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;

/**
 * Created by gerry on 01.12.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_Backup {
    static String testDir = "target/test";
    JsonObject conf;


    @Before
    public void setUp() {
        FileTool.deltree(testDir);
        File dir = new File(testDir);
        dir.mkdirs();
        JsonObject cfg = JsonUtil.Companion.load("../ungrad-user.json");
        conf = cfg.getJsonArray("launch").getJsonObject(0).getJsonObject("config");

    }

    @Test
    public void testChop() throws Exception {
        Chopper chopper = new Chopper();
        File testFile = new File(testDir, "testFile");
        FileTool.writeRandomFile(testFile, 64001L);
        File dir = new File(testDir, "dest");
        chopper.chop(testFile, dir, 18000, "password".getBytes());
        testFile.renameTo(new File(testDir, "old"));
        chopper.unchop(dir, new File(testDir), "password".getBytes());
        byte[] orig = FileTool.readFile(new File("testDir", "old"));
        byte[] decoded = FileTool.readFile(new File("testDir", "testFile"));
        Assert.assertArrayEquals(orig, decoded);
    }

    @Test
    public void testReduce() throws Exception {
        File dir = new File(testDir, "files");
        dir.mkdirs();
        File test1 = new File(dir, "zaa");
        File test2 = new File(dir, "abc");
        File test3 = new File(dir, "czya");
        FileTool.writeRandomFile(test1, 1000);
        FileTool.writeRandomFile(test2, 1000);
        FileTool.writeRandomFile(test3, 1000);
        File archiveDir = new File(testDir, "archive");
        archiveDir.mkdir();
        Reducer reducer = new Reducer();
        reducer.reduce(dir, archiveDir, new Comparator<File>() {

            @Override
            public int compare(File t0, File t1) {
                return t0.getName().compareTo(t1.getName());
            }
        });
        Assert.assertTrue(0 == dir.listFiles().length);
        File[] target = archiveDir.listFiles();
        Assert.assertTrue(1 == target.length);
        Assert.assertTrue("abc".equals(target[0].getName()));
    }

    /**
     * Since testSCP and testGlacier need real hosts to send files, we can't enable these tests
     * for regular builds.
     * Please set up your hosts/accounts and create an "ungrad-user.json" in the root dir of ungrad-server
     * Then, remove the @Ignore annotations of the following 2 tests.
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testSCP() throws Exception {
        File test = new File(testDir, "test");
        FileTool.writeRandomFile(test, 2000L);
        Scp scp = new Scp(conf.getJsonObject("scp-default"));
        Assert.assertEquals("ok",scp.transmit(test).getString("status"));
        test.renameTo(new File(testDir, "orig"));
        String fetched = scp.fetch("test", new File(testDir)).getString("file");
        byte[] b1 = FileTool.readFile(new File(testDir, "orig"));
        byte[] b2 = FileTool.readFile(new File(testDir, "test"));
        Assert.assertArrayEquals(b1, b2);
    }

    /*
    Note: This can take several hours, since amazon states, retrieval operations have a typical
    latency of 4 hours.We'll wait 5 hours maximum before failure
     */
    @Test(timeout = 5 * 3600000L)
    @Ignore
    public void testGlacier(TestContext ctx) throws Exception {
        Glacier glacier = new Glacier(conf.getJsonObject("glacier-default"));
        glacier.createOrGetVault("UngradTest");
        File test = new File(testDir, "test");
        FileTool.writeRandomFile(test, 5000L);
        String archiveID = glacier.transmit("UngradTest", test).getString("archiveID");
        Assert.assertNotNull(archiveID);
        Async async = ctx.async();

        glacier.asyncFetch("UngradTest", archiveID, result -> {
            Assert.assertTrue(result.succeeded());
            try {
                FileTool.copyStreams(result.result(), new FileOutputStream(new File(testDir, "result")));
                result.result().close();
                byte[] b1 = FileTool.readFile(new File(testDir, "test"));
                byte[] b2 = FileTool.readFile(new File(testDir, "result"));
                Assert.assertArrayEquals(b1, b2);
                glacier.deleteArchive("UngradTest", archiveID);
                        /*
        We can't delete the vault now, since it's not empty (Well, Amazon Glacier
        doesn't know yet, that it's empty. So, delete the vault manually from the console
         after the next vault inventory happened (probably tomorrow)
         */
                //glacier.deleteVault("UngradTest");

                async.complete();
            } catch (IOException e) {
                e.printStackTrace();
                ctx.fail();
            }
        });

    }
}
