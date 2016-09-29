import ch.rgw.lucinda.Admin;
import ch.rgw.lucinda.Communicator;
import ch.rgw.lucinda.CommunicatorKt;
import ch.rgw.tools.json.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by gerry on 28.09.16.
 */
@RunWith(VertxUnitRunner.class)


public class Test_Admin {
    static Vertx vertx;

    @BeforeClass
    public static void setUp() {
        vertx = Vertx.vertx();
        Communicator.Companion.setEb(vertx.eventBus());
        CommunicatorKt.getLucindaConfig().clear();
        vertx.eventBus().consumer(CommunicatorKt.EB_SETCONFIG, msg -> {
            msg.reply(JsonUtil.Companion.ok());
        });
    }

    @AfterClass
    public static void tearDown() {
        vertx.close();
    }

    @Test
    public void test_getParam(TestContext ctx) {
        Async async1 = ctx.async();
        Async async2 = ctx.async();
        Async async3 = ctx.async();
        Future<Object> future1 = Admin.INSTANCE.getParam(JsonUtil.Companion.create("param:indexdir"));
        future1.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            Assert.assertNotNull(result.result());
            async1.complete();
        });
        Future<Object> future2 = Admin.INSTANCE.getParam(JsonUtil.Companion.create("param:datadir"));
        future2.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            Assert.assertNotNull(result.result());
            async2.complete();
        });
        Future<Object> future3 = Admin.INSTANCE.getParam(JsonUtil.Companion.create("param:wrong"));
        future3.setHandler(result -> {
            Assert.assertTrue(result.failed());
            async3.complete();
        });
    }

    @Test
    public void test_setParam(TestContext ctx) {
        Async async1 = ctx.async();
        Async async2 = ctx.async();
        Async async3 = ctx.async();
        Future<Object> future1 = Admin.INSTANCE.setParam(JsonUtil.Companion.create("param:indexdir", "value:target/store/foobar"));
        Future<Object> future2 = Admin.INSTANCE.setParam(JsonUtil.Companion.create("param:datadir", "value:target/store/foobar"));
        Future<Object> future3 = Admin.INSTANCE.setParam(JsonUtil.Companion.create("param:wrong", "value:target/store/foobar"));
        future1.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async1.complete();
        });
        future2.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async2.complete();
        });
        future3.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async3.complete();
        });

    }

    @Test
    public void test_badCommand(TestContext ctx) {
        Async async1 = ctx.async();
        Future<Object> future1 = Admin.INSTANCE.exec(JsonUtil.Companion.create("command:test"));
        future1.setHandler(result -> {
            Assert.assertTrue(result.failed());
            async1.complete();
        });

    }
}
