import ch.rgw.io.FileTool;
import ch.rgw.tools.crypt.UtilKt;
import ch.rgw.tools.json.JsonUtil;
import ch.rgw.tools.json.JsonUtilKt;
import ch.rgw.ungrad_backup.SimpleStore;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Created by gerry on 04.12.16.
 */
public class Test_S3 {
    static String testDir="target/test";
    JsonObject conf;
    SimpleStore s3;

    @Before
    public void setUp(){
        FileTool.deltree(testDir);
        File dir=new File(testDir);
        dir.mkdirs();
        JsonObject cfg= JsonUtil.Companion.load("../ungrad-user.json");
        conf=cfg.getJsonArray("launch").getJsonObject(0).getJsonObject("config");
        s3=new SimpleStore(conf.getJsonObject("glacier-default"));

    }

    @Test
    @Ignore
    public void test_s3_raw() throws Exception{
        File test=new File(testDir,"test");
        FileTool.writeRandomFile(test,2000L);
        s3.put("test",FileTool.readFile(test));
        byte[] ret=s3.get("test");
        Assert.assertArrayEquals(ret,FileTool.readFile(test));
        s3.delete("test");
    }

    @Test
    @Ignore public void test_s3_json() throws Exception{
        JsonObject jp= JsonUtilKt.json_create("a:b","c:d","e:f");
        s3.putJson("test",jp,"blabla");
        Assert.assertTrue(s3.exists("test"));
        JsonObject cmp=s3.getJson("test","blabla");
        Assert.assertEquals("b",cmp.getString("a"));
        Assert.assertEquals("d",cmp.getString("c"));
        Assert.assertEquals("f",cmp.getString("e"));
        s3.delete("test");
        Assert.assertFalse(s3.exists("test"));
    }

    @Test
    @Ignore public void test_s3_encrypted()throws Exception{
        byte[] check=UtilKt.randomArray(829);
        s3.putEncrypted("test",check,"ThisIsAVerySecretPassword or a phrase");
        Assert.assertTrue(s3.exists("test"));
        byte[] cmp=s3.getEncrypted("test","ThisIsAVerySecretPassword or a phrase");
        Assert.assertArrayEquals(check,cmp);
        s3.delete("test");
        Assert.assertFalse(s3.exists("test"));
    }
}
