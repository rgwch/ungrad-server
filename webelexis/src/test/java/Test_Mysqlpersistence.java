import ch.rgw.tools.json.JsonUtil;
import ch.rgw.tools.json.JsonUtilKt;
import ch.webelexis.model.AsyncPersistentObject;
import ch.webelexis.model.DummyPersistentObject;
import ch.webelexis.model.MysqlPersistence;
import ch.webelexis.model.Patient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gerry on 29.10.16.
 */
@RunWith(VertxUnitRunner.class)

public class Test_Mysqlpersistence {
    JsonObject cfg;
    Vertx vertx;
    MysqlPersistence mp;

    @Before
    public void setUp(TestContext ctx) throws IOException{
        Async async=ctx.async();
        cfg= JsonUtil.Companion.createFromFile(new File("target/test-classes/mysql.cfg"),false);
        vertx=Vertx.vertx();
        mp=new MysqlPersistence(cfg,vertx);
        AsyncPersistentObject.Companion.setDefaultPersistence(mp);
        ArrayList<String> cmd=new ArrayList();
        cmd.add("DROP TABLE IF EXISTS DUMMY");
        cmd.add("CREATE TABLE DUMMY (ID VARCHAR(100), deleted CHAR(1), lastupdate BIGINT, p_1 VARCHAR(100),p_2 VARCHAR(100),p_3 VARCHAR(100));");
        mp.exec(cmd).setHandler(ret -> {
            if(!ret.succeeded()){
                throw new Error("failed database access");
            }
            async.complete();
        });

    }

    @After
    public void tearDown(){
        vertx.close();
    }


    @Test
    public void test_dummy(TestContext ctx) {
        Async async1 = ctx.async();
        DummyPersistentObject dpo = new DummyPersistentObject("1");
        Future<Boolean> res1 = dpo.set("p_1", "val_1");
        res1.setHandler(result1 -> {
            ctx.assertTrue(result1.succeeded());
            ctx.assertTrue(result1.result());
            async1.complete();
            Async async2 = ctx.async();
            Future<Object> res2 = dpo.get("p_1");
            res2.setHandler(result2 -> {
                ctx.assertTrue(result2.succeeded());
                ctx.assertEquals("val_1", result2.result());
                async2.complete();
            });

        });
    }

    @Test
    public void test_createSQL(){
        Patient pat=new Patient();
        //JsonUtilKt.add(pat,"firstname:Dideldum","lastname:Humptydoo","birthdate:01.05.1827");
        pat.set("firstname:Dideldum","lastname:Humptydoo","birthdate:01.05.1827");
        String sql=mp.apo2sql(pat);
        Assert.assertFalse(sql.startsWith("INSERT INTO KONTAKT (id,Bezeichnung1,Bezeichnung2,Geburtsdatum"));
    }

}
