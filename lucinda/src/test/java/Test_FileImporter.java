import ch.rgw.io.FileTool;
import ch.rgw.lucinda.Dispatcher;
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
    Dispatcher dispatcher;

    @Before
    public void setUp() {
        FileTool.deltree("target/indexMgrTest");
        indexManager = new IndexManager("target/indexMgrTest", "de");
        dispatcher = new Dispatcher(indexManager);
    }

    @After
    public void tearDown() {
        indexManager.shutDown();
        FileTool.deltree("target/indexMgrTest");
    }

    @Test
    public void test_Odt(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/testodt.odt"), new JsonObject());
        Assert.assertTrue(result.equals(""));
    }

    @Test
    public void testPDF(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/testpdf.pdf"), new JsonObject());
        Assert.assertTrue(result.equals(""));

    }

    @Test
    public void testFailure(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/inexistent.xx"), new JsonObject());
        Assert.assertTrue(result.endsWith("not found"));

    }

    @Test
    public void testOCR(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result=fi.process(Paths.get("target/test-classes/testocr.pdf"), new JsonObject());
        Assert.assertTrue(result.equals(""));
    }
}
