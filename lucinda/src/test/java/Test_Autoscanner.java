import ch.rgw.io.FileTool;
import ch.rgw.lucinda.Autoscanner;
import ch.rgw.lucinda.Communicator;
import ch.rgw.lucinda.CommunicatorKt;
import ch.rgw.lucinda.IndexManager;
import ch.rgw.tools.json.JsonUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * Created by gerry on 29.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_Autoscanner {
    static final String basedir="target/store/autoscanner/";
    static final String indexdir=basedir+"index";
    static final String watchdir=basedir+"watch";
    static Vertx vertx;

    @BeforeClass
    public static void setUp(){
        FileTool.deltree(indexdir);
        FileTool.deltree(watchdir);
        new File(indexdir).mkdirs();
        new File(watchdir).mkdirs();
        CommunicatorKt.getLucindaConfig().put("fs_basedir",basedir);
        Communicator.Companion.setIndexManager(new IndexManager(indexdir,"de"));
        vertx=Vertx.vertx();

    }

    @Test
    public void test_scanner(TestContext ctx) throws IOException{
        Async async=ctx.async();
        Autoscanner testee=new Autoscanner();
        testee.setRunning(true);
        vertx.deployVerticle(testee);
        File watchd=new File(watchdir);
        watchd.mkdirs();
        testee.register(new JsonArray().add(watchd.getAbsolutePath()));
        new Thread() {
            @Override
            public void run() {
                testee.loop();
            }
        }.start();
        FileTool.copyFile(new File("target/test-classes/testodt.odt"),new File(watchdir,"testodt.odt"),FileTool.REPLACE_IF_EXISTS);
        /*
         * unfortunately, this is time critical. We don't know exactly, how long importing of the file copied above will take.
         * This depends of several things we don't know in advance. So we allow a quite reasonable time of 1.5s before we check
         * for existence of the newly indexed file.
         */
        vertx.setTimer(1500L, timeriD ->{
            JsonArray res=Communicator.Companion.getIndexManager().queryDocuments("Reihe",1);
            ctx.assertTrue(res.size()>0);
            JsonObject doc=res.getJsonObject(0);
            async.complete();

        });


    }
}
