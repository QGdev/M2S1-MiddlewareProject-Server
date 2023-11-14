package document;

import fr.univnantes.document.Document;
import fr.univnantes.user.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentTest {

    @Test
    public void testGetUUID() {
        Document document = new Document("Test");
        assertNotNull(document.getUUID());
    }

    @Test
    public void testGetName() {
        Document document = new Document("Test");
        assertEquals("Test", document.getName());
    }

    @Test
    public void testSetName() {
        Document document = new Document("Test");
        document.setName("NewTest");
        assertEquals("NewTest", document.getName());
    }

    @Test
    public void testAddUser() {
        Document document = new Document("Test");
        User user = new User("User1");
        assertTrue(document.addUser(user));
    }

    @Test
    public void testRemoveUser() {
        Document document = new Document("Test");
        User user = new User("User1");
        document.addUser(user);
        assertTrue(document.removeUser(user));
    }

    @Test
    public void testIsUserInDocument() {
        Document document = new Document("Test");
        User user = new User("User1");
        document.addUser(user);
        assertTrue(document.isUserInDocument(user));
    }

    @Test
    public void testGetLineCount() {
        Document document = new Document("Test");
        assertEquals(1, document.getLineCount());
    }

    @Test
    public void testInsert() {
        Document document = new Document("Test");
        assertTrue(document.insert(0, 0, 'a'));
    }

    @Test
    public void testModify() {
        Document document = new Document("Test");
        document.insert(0, 0, 'a');
        assertTrue(document.modify(0, 0, 'b'));
    }
}
