import ch.rgw.lucinda.Communicator;
import ch.rgw.tools.Configuration;
import io.vertx.core.Context;
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
    static HttpClient htc;
    static Vertx vertx;

    @BeforeClass
    public static void createClient(TestContext ctx){
        vertx= Vertx.vertx();
        htc=vertx.createHttpClient();
        Configuration cfg=new Configuration();
        Async async=ctx.async();
        vertx.deployVerticle(new Communicator(cfg), test -> {
            Assert.assertTrue(test.succeeded());
            async.complete();
        });
    }

    @AfterClass
    public static void shutDown(){
        htc.close();
        vertx.close();
    }
    @Test
    public void testPing(TestContext ctx){
        /*
        htc.getNow(2016,"localhost","/api/1.0/lucinda/ping/one", response -> {
           response.bodyHandler(buffer -> {
               JsonObject resp=buffer.toJsonObject();
               Assert.assertEquals("one",resp.getString("var"));
           });
        });
        */
        Async async=ctx.async();
        vertx.eventBus().send("ch.rgw.lucinda.ping", new JsonObject().put("var","testvar"), response -> {
            Assert.assertTrue(response.succeeded());
            JsonObject msg= (JsonObject) response.result().body();
            Assert.assertEquals("ok",msg.getString("status"));
            Assert.assertEquals("testvar",msg.getString("pong"));
            async.complete();
        });
    }
}
