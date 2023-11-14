package document;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    @Test
    public void testGetInstance() {
        DocumentManager documentManager1 = DocumentManager.getInstance();
        DocumentManager documentManager2 = DocumentManager.getInstance();
        assertSame(documentManager1, documentManager2);
    }

    @Test
    public void testGetDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertEquals(document, documentManager.getDocument(document.getUUID()));
    }

    @Test
    public void testCreateDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertNotNull(document);
        assertEquals("Test", document.getName());
    }

    @Test
    public void testRemoveDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertTrue(documentManager.removeDocument(document.getUUID()));
    }
}
