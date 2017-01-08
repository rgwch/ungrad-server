import ch.rgw.lucinda.Hub;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.Async;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by gerry on 08.01.17.
 */
@RunWith(VertxUnitRunner.class)
public class Test_Hub {

    @Test
    public void Test_Launch(TestContext ctx){
        Vertx vertx=Vertx.vertx();
        Async async=ctx.async();
        vertx.deployVerticle(new Hub(), result ->{
            Assert.assertTrue(result.succeeded());
            async.complete();
        });
    }
}
