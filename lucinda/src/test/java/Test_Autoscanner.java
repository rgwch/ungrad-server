import ch.rgw.lucinda.Autoscanner;
import ch.rgw.tools.json.JsonUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * Created by gerry on 29.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_Autoscanner {

    @Test
    public void test_scanner() throws IOException{
        Autoscanner testee=new Autoscanner();
        testee.setRunning(true);
        Vertx vertx=Vertx.vertx();
        vertx.deployVerticle(testee);
        File watchdir=new File("target/store/watch");
        watchdir.mkdirs();
        testee.register(new JsonArray().add(watchdir.getAbsolutePath()));
        testee.loop();
    }
}
