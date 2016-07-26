import ch.rgw.lucinda.IndexManager;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by gerry on 26.07.16.
 */
@RunWith(VertxUnitRunner.class)

public class Test_IndexManager {

    @Test
    public void testAddAndRetrieve(){
        IndexManager indexManager=new IndexManager("target/indexMgrTest");

    }
}
