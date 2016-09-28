import ch.rgw.io.FileTool;
import ch.rgw.lucinda.Communicator;
import ch.rgw.lucinda.IndexManager;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by gerry on 26.07.16.
 */
@RunWith(VertxUnitRunner.class)

public class Test_IndexManager {
    private static IndexManager indexManager;
    private static final String id="893f8fbfe7b6483a0fa24c97ee18bca98d431e8e";


    @BeforeClass
    public static void clean(){
        FileTool.deltree("target/indexMgrTest");

    }

    @Before
    public void setUp(){
        FileTool.deltree("target/indexMgrTest");

        Communicator.indexManager=new IndexManager("target/indexMgrTest","de");
        indexManager= Communicator.indexManager;
    }
    @After
    public void tearDown(){
        indexManager.shutDown();
        FileTool.deltree("target/indexMgrTest");
    }


    @Test
    public void add() throws IOException {

        FileInputStream fis=new FileInputStream("target/classes/default.cfg");
        indexManager.addDocument(fis,insert());
        fis.close();
        assertEquals(1,indexManager.queryDocuments("BBB: Test*",100).size());
        assertNotNull(indexManager.getDocument(id));

    }

    @Test
    public void update() throws IOException{

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
        return new JsonObject()
                .put("AAA", "Test aaa")
                .put("BBB", "Test bbb")
                .put("_id",id);
    }
}
