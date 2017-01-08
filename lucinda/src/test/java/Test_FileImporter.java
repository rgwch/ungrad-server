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
        FileTool.deltree("target/fileImporterTest");
        indexManager = new IndexManager("target/fileImporterTest", "de");
        dispatcher = new Dispatcher(indexManager);
    }

    @After
    public void tearDown() {
        indexManager.shutDown();
        FileTool.deltree("target/fileImporterTest");
    }

    /**
     * Import a OpenOffice Text file
     * @param ctx
     */
    @Test
    public void test_Odt(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/testodt.odt"), new JsonObject());
        Assert.assertTrue(result.equals(""));
    }

    /**
     * Import a PDF with text content
     * @param ctx
     */
    @Test
    public void testPDF(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/testpdf.pdf"), new JsonObject());
        Assert.assertTrue(result.equals(""));

    }

    /**
     * Try to import an inexistent file
     * @param ctx
     */
    @Test
    public void testFailure(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result = fi.process(Paths.get("target/test-classes/inexistent.xx"), new JsonObject());
        Assert.assertTrue(result.endsWith("not found"));

    }

    /**
     * Import a PDF with image content which must be OCRed
     * Note: While testoct.pdf and testpdf.pdf look very similar to a human, they are structurally fundamentally different:
     * testpdf.pdf containts text content, which is easy to extract. Testocr.pdf contains image content, such as a pdf from a
     * scanner import. There is no text to extract, and the importer must check with OCR, if there is any text in the image.
     *
     * @param ctx
     */
    @Test
    public void testOCR(TestContext ctx) {
        FileImporter fi = new FileImporter(indexManager);
        String result=fi.process(Paths.get("target/test-classes/testocr.pdf"), new JsonObject());
        Assert.assertTrue(result.equals(""));
    }
}
