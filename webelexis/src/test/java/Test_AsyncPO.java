import ch.webelexis.model.Contact;
import ch.webelexis.model.DummyPersistentObject;
import ch.webelexis.model.Patient;
import ch.webelexis.model.Person;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by gerry on 24.10.16.
 */
@RunWith(VertxUnitRunner.class)

public class Test_AsyncPO {

    @Test @Ignore
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

    @Test @Ignore
    public void test_badParameter(TestContext ctx) {
        Async async = ctx.async();
        DummyPersistentObject dpo = new DummyPersistentObject("2");
        Future<Boolean> res = dpo.set("bad_fieldname", "val_1");
        res.setHandler(result -> {
            ctx.assertFalse(result.succeeded());
            async.complete();
        });
    }

    @Test @Ignore
    public void test_emptyField(TestContext ctx) {
        Async async = ctx.async();
        DummyPersistentObject dpo = new DummyPersistentObject("3");
        Future<Object> result = dpo.get("p_1");
        result.setHandler(result2 -> {
            ctx.assertTrue(result2.succeeded());
            ctx.assertTrue(result2.result().toString().isEmpty());
            async.complete();
        });

    }

    @Test @Ignore
    public void test_contact(TestContext ctx) {
        Contact contact = new Contact();
        Async async = ctx.async();
        contact.set("Bezeichnung1:Testperson", "Bezeichnung2:Armeswesen", "phone1:555-555 55 55").setHandler(result -> {
            ctx.assertTrue(result.succeeded());
            ctx.assertTrue(result.result());
            Contact query = new Contact();
            query.put("phone1", "555*");
            query.find().setHandler(result2 -> {
                ctx.assertTrue(result2.succeeded());
                List<JsonObject> found = result2.result();
                ctx.assertEquals(found.size(), 1);
                JsonObject jo = found.get(0);
                ctx.assertEquals("Testperson", jo.getString("Bezeichnung1"));
                async.complete();
            });
        });

    }

    @Test @Ignore
    public void test_patient(TestContext ctx) {
        Patient pat = new Patient();
        Async async = ctx.async();
        pat.set("lastname:Testperson", "firstname:Armeswesen", "phone1:550-555 55 55").setHandler(result -> {
            ctx.assertTrue(result.succeeded());
            ctx.assertTrue(result.result());
            Patient query = new Patient();
            query.put("phone1", "550*");
            query.find().setHandler(result2 -> {
                ctx.assertTrue(result2.succeeded());
                List<JsonObject> found = result2.result();
                ctx.assertEquals(found.size(), 1);
                JsonObject jo = found.get(0);
                ctx.assertEquals("Testperson", jo.getString("lastname"));
                async.complete();
            });
        });

    }

    @Test @Ignore
    public void test_badfield(TestContext ctx) {
        Async async = ctx.async();
        Person pers = new Person();
        pers.set("namez:Testperson", "firstname:Armeswesen").setHandler(result -> {
            ctx.assertTrue(result.failed());
            ctx.assertTrue(result.cause().getMessage().contains("illegal field name"));
            async.complete();
        });
    }
}
