import ch.rgw.lucinda.Communicator;
import ch.rgw.tools.Configuration;
import ch.rgw.tools.JsonUtil;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by gerry on 24.07.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_LucindaInterface {
    static Vertx vertx;

    @BeforeClass
    public static void createClient(TestContext ctx){
        vertx= Vertx.vertx();
        JsonObject cfg=new JsonObject();
        Async async=ctx.async();
        vertx.deployVerticle(new Communicator(),new DeploymentOptions().setConfig(cfg), test -> {
            Assert.assertTrue(test.succeeded());
            async.complete();
        });
    }

    @AfterClass
    public static void shutDown(){
        vertx.close();
    }
    @Test
    public void testPing(TestContext ctx){
        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.ping", new JsonObject().put("var","testvar"), response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            ctx.assertEquals("ok",msg.getString("status"));
            ctx.assertEquals("testvar",msg.getString("pong"));
            async.complete();
        });
    }

    @Test
    public void testImport(TestContext ctx){
        JsonObject parm=new JsonObject();
        parm.put("dry-run",true).put("_id","123").put("payload",new byte[]{1,2,3}).put("filename","testfile");
        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.import", parm, response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            ctx.assertEquals("ok",msg.getString("status"));
            ctx.assertEquals("import",msg.getString("method"));
            async.complete();
        });
    }

    @Test
    public void testIndex(TestContext ctx){
        JsonObject parm=new JsonObject();
        parm.put("dry-run",true);

        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.index", parm, response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            async.complete();
        });
    }

    @Test
    public void testGet(TestContext ctx){
        JsonObject parm=new JsonObject();
        parm.put("dry-run",true);

        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.get", parm, response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            async.complete();
        });
    }

    @Test
    public void testFind(TestContext ctx){
        JsonObject parm=new JsonObject();
        parm.put("dry-run",true);

        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.find", parm, response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            async.complete();
        });
    }

    @Test
    public void testUpdate(TestContext ctx){
        JsonObject parm=new JsonObject();
        parm.put("dry-run",true);

        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.update", parm, response -> {
            ctx.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            async.complete();
        });
    }
}
