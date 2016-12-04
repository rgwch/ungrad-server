import ch.rgw.io.FileTool;
import ch.rgw.tools.json.JsonUtil;
import ch.rgw.ungrad_backup.SimpleStore;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by gerry on 04.12.16.
 */
public class Test_S3 {
    static String testDir="target/test";
    JsonObject conf;

    @Before
    public void setUp(){
        FileTool.deltree(testDir);
        File dir=new File(testDir);
        dir.mkdirs();
        JsonObject cfg= JsonUtil.Companion.load("../ungrad-user.json");
        conf=cfg.getJsonArray("launch").getJsonObject(0).getJsonObject("config");

    }

    @Test
    public void test_s3() throws Exception{
        SimpleStore s3=new SimpleStore(conf.getJsonObject("glacier-default"));
        File test=new File(testDir,"test");
        FileTool.writeRandomFile(test,2000L);
        s3.put("test",FileTool.readFile(test));
        byte[] ret=s3.get("test");
        Assert.assertArrayEquals(ret,FileTool.readFile(test));
        s3.delete("test");
    }
}
