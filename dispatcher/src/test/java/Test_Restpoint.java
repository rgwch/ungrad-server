import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by gerry on 04.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_Restpoint {
    static Vertx vertx;

    @BeforeClass
    public static void init(){
        vertx=Vertx.vertx();
    }
    @Test
    public void launch(TestContext ctx){

    }
}
