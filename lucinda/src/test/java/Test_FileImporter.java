import ch.rgw.io.FileTool;
import ch.rgw.lucinda.Communicator;
import ch.rgw.lucinda.FileImporter;
import ch.rgw.lucinda.IndexManager;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Paths;

/**
 * Created by gerry on 28.09.16.
 */
@RunWith(VertxUnitRunner.class)
public class Test_FileImporter {
    IndexManager indexManager;

    @Before
    public void setUp() {
        FileTool.deltree("target/indexMgrTest");

        Communicator.indexManager = new IndexManager("target/indexMgrTest", "de");
        indexManager = Communicator.indexManager;
    }

    @After
    public void tearDown() {
        indexManager.shutDown();
        FileTool.deltree("target/indexMgrTest");
    }

    @Test
    public void test_Odt(TestContext ctx) {
        FileImporter fi = new FileImporter(Paths.get("target/test-classes/testodt.odt"), new JsonObject());
        Future<Integer> future = Future.future();
        Async async = ctx.async();
        fi.handle(future.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async.complete();
        }));

    }

    @Test
    public void testPDF(TestContext ctx) {
        FileImporter fi = new FileImporter(Paths.get("target/test-classes/testpdf.pdf"), new JsonObject());
        Future<Integer> future = Future.future();
        Async async = ctx.async();
        fi.handle(future.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async.complete();
        }));

    }

    @Test
    public void testFailure(TestContext ctx) {
        FileImporter fi = new FileImporter(Paths.get("target/test-classes/inexistent.xx"), new JsonObject());
        Future<Integer> future = Future.future();
        Async async = ctx.async();
        fi.handle(future.setHandler(result -> {
            Assert.assertTrue(result.failed());
            Assert.assertTrue(result.cause().getMessage().endsWith("not found"));
            async.complete();
        }));

    }

    @Test
    public void testOCR(TestContext ctx) {
        FileImporter fi = new FileImporter(Paths.get("target/test-classes/testocr.pdf"), new JsonObject());
        Future<Integer> future = Future.future();
        Async async = ctx.async();
        fi.handle(future.setHandler(result -> {
            Assert.assertTrue(result.succeeded());
            async.complete();
        }));

    }
}
