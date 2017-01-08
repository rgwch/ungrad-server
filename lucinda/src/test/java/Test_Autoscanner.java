import ch.rgw.io.FileTool;
import ch.rgw.lucinda.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
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
    static final String basedir = "target/store/autoscanner/";
    static final String indexdir = basedir + "index";
    static final String watchdir = basedir + "watch";
    static Vertx vertx;
    static IndexManager indexManager;
    static Dispatcher dispatcher;

    @BeforeClass
    public static void setUp() {
        FileTool.deltree(indexdir);
        FileTool.deltree(watchdir);
        new File(indexdir).mkdirs();
        new File(watchdir).mkdirs();
        HubKt.getLucindaConfig().put("fs_basedir", basedir);
        vertx = Vertx.vertx();
        indexManager = new IndexManager(basedir, "de");
        dispatcher = new Dispatcher(indexManager);
    }

    @Test
    public void test_scanner(TestContext ctx) throws IOException {
        Async deploy = ctx.async();
        Async copy=ctx.async();
        Autoscanner testee = new Autoscanner(dispatcher);
        testee.setRunning(true);
        vertx.deployVerticle(testee, result -> {
            Assert.assertTrue(result.succeeded());
            deploy.complete();
            File watchd = new File(watchdir);
            watchd.mkdirs();
            vertx.eventBus().send(AutoscannerKt.ADDR_START, new JsonObject().put("interval",1000).put("dirs",new JsonArray().add(watchd.getAbsolutePath())), reply -> {
                Assert.assertTrue(reply.succeeded());
                JsonObject answer= (JsonObject) reply.result().body();
                Assert.assertEquals("ok",answer.getString("status"));
                FileTool.copyFile(new File("target/test-classes/testodt.odt"), new File(watchdir, "testodt.odt"), FileTool.REPLACE_IF_EXISTS);
                        /*
         * unfortunately, this is time critical. We don't know exactly, how long importing of the file copied above will take.
         * This depends of several things we don't know in advance. So we allow a quite reasonable time of 1.5s before we check
         * for existence of the newly indexed file.
         */
                vertx.setTimer(1500L, timeriD -> {
                    JsonArray res = indexManager.queryDocuments("Reihe", 1);
                    ctx.assertTrue(res.size() > 0);
                    JsonObject doc = res.getJsonObject(0);
                    copy.complete();

                });

            });


        });

    }
}
