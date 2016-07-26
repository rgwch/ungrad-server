import ch.rgw.io.FileTool;
import ch.rgw.lucinda.IndexManager;
import ch.rgw.lucinda.LauncherKt;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gerry on 26.07.16.
 */
@RunWith(VertxUnitRunner.class)

public class Test_IndexManager {
    static IndexManager indexManager;
    static String id="893f8fbfe7b6483a0fa24c97ee18bca98d431e8e";


    @BeforeClass
    public static void setUp(){
        FileTool.deltree("target/indexMgrTest");
        LauncherKt.setIndexManager(new IndexManager("target/indexMgrTest"));
        indexManager= LauncherKt.getIndexManager();
    }
    @AfterClass
    public static void tearDown(){
        indexManager.shutDown();
        FileTool.deltree("target/indexMgrTest");
    }

    @Test
    public void indexDirect(){
        indexManager.removeDocument(id);
        assertEquals(0,indexManager.queryDocuments("BBB: Test*",100).size());

        Document doc=new Document();
        doc.add(new StringField("AAA","Test Feld aaa", Field.Store.YES));
        doc.add(new TextField("BBB","Test Field bbb", Field.Store.YES));
        doc.add(new StringField("_id",id,Field.Store.YES));
        indexManager.updateDocument(doc);

        assertEquals(1,indexManager.queryDocuments("BBB: Test*",100).size());

        assertNotNull(indexManager.getDocument(id));

    }
    @Test
    public void add() throws IOException {
        JsonArray x=indexManager.queryDocuments("_id: "+id,100);
        indexManager.removeDocument(id);
        JsonArray y=indexManager.queryDocuments("_id: "+id,100);
        assertEquals(0,indexManager.queryDocuments("BBB: Test*",100).size());

        FileInputStream fis=new FileInputStream("target/classes/default.cfg");
        indexManager.addDocument(fis,insert());
        fis.close();
        assertEquals(1,indexManager.queryDocuments("BBB: Test*",100).size());
        assertNotNull(indexManager.getDocument(id));

    }

    @Test
    public void update() throws IOException{
        indexManager.removeDocument(id);
        assertEquals(0,indexManager.queryDocuments("BBB: Test*",100).size());

        FileInputStream fis=new FileInputStream("target/classes/default.cfg");
        indexManager.addDocument(fis,insert());
        fis.close();
        assertEquals(1,indexManager.queryDocuments("BBB: Test*",100).size());
        Document doc=indexManager.getDocument(id);
        assertNotNull(doc);
        doc.add(new StringField("CCC", "test ccc", Field.Store.YES));
        indexManager.updateDocument(doc);
        assertEquals(1,indexManager.queryDocuments("CCC: Test*",100).size());
    }

    @Test
    public void delete()throws IOException{
        FileInputStream fis=new FileInputStream("target/classes/default.cfg");
        indexManager.addDocument(fis,insert());
        fis.close();
        assertEquals(1,indexManager.queryDocuments("BBB: Test*",100).size());
        Document doc=indexManager.getDocument(id);
        assertNotNull(doc);
        indexManager.removeDocument(id);
        assertEquals(0,indexManager.queryDocuments("BBB: Test*",100).size());
    }

    private JsonObject insert(){
        JsonObject jo=new JsonObject()
                .put("AAA", "Test aaa")
                .put("BBB", "Test bbb")
                .put("_id",id);
        return jo;
    }
}
