package document;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {
    /**
     * Test the singleton behavior of the DocumentManager by ensuring that the
     * same instance is returned when calling getInstance() multiple times.
     * <p>
     * This test creates two instances of DocumentManager using getInstance() and asserts
     * that they are the same, confirming the singleton pattern implementation.
     * </p>
     */
    @Test
    public void testGetInstance() {
        DocumentManager documentManager1 = DocumentManager.getInstance();
        DocumentManager documentManager2 = DocumentManager.getInstance();
        assertSame(documentManager1, documentManager2);
    }

    /**
     * Test retrieving a document from the DocumentManager by its UUID.
     * <p>
     * This test creates a DocumentManager, creates a document with a specified name,
     * and asserts that retrieving the document using its UUID yields the expected result.
     * </p>
     */
    @Test
    public void testGetDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertEquals(document, documentManager.getDocument(document.getUUID()));
    }

    /**
     * Test creating a document using the DocumentManager.
     * <p>
     * This test creates a DocumentManager, uses it to create a document with a specified name,
     * and asserts that the created document is not null and has the expected name.
     * </p>
     */
    @Test
    public void testCreateDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertNotNull(document);
        assertEquals("Test", document.getName());
    }

    /**
     * Test removing a document from the DocumentManager.
     * <p>
     * This test creates a DocumentManager, creates a document with a specified name,
     * removes the document using its UUID, and asserts that the removal was successful.
     * </p>
     */
    @Test
    public void testRemoveDocument() {
        DocumentManager documentManager = DocumentManager.getInstance();
        Document document = documentManager.createDocument("Test");
        assertTrue(documentManager.removeDocument(document.getUUID()));
    }

}
