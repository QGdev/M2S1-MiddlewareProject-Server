package fr.univnantes.document;

import fr.univnantes.user.User;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTest {
    /**
     * Test the generation of a unique UUID for a document.
     * <p>
     * This test creates a document and verifies that the UUID generated for it is not null.
     * </p>
     */
    @Test
    public void testGetUUID() {
        Document document = new Document("Test");
        assertNotNull(document.getUUID());
    }

    /**
     * Test retrieving the name of a document.
     * <p>
     * This test creates a document with the name "Test" and asserts that the retrieved
     * name matches the expected value.
     * </p>
     */
    @Test
    public void testGetName() {
        Document document = new Document("Test");
        assertEquals("Test", document.getName());
    }

    /**
     * Test setting a new name for a document.
     * <p>
     * This test creates a document with the name "Test," sets a new name "NewTest,"
     * and verifies that the name has been successfully updated.
     * </p>
     */
    @Test
    public void testSetName() {
        Document document = new Document("Test");
        document.setName("NewTest");
        assertEquals("NewTest", document.getName());
    }

    /**
     * Test adding a user to a document.
     * <p>
     * This test creates a document and a user, adds the user to the document,
     * and asserts that the addition was successful.
     * </p>
     */
    @Test
    public void testAddUser() {
        Document document = new Document("Test");
        User user = new User("User1");
        assertTrue(document.addUser(user));
    }

    /**
     * Test removing a user from a document.
     * <p>
     * This test creates a document, adds a user to it, removes the user,
     * and asserts that the removal was successful.
     * </p>
     */
    @Test
    public void testRemoveUser() {
        Document document = new Document("Test");
        User user = new User("User1");
        document.addUser(user);
        assertTrue(document.removeUser(user));
    }

    /**
     * Test checking if a user is in a document.
     * <p>
     * This test creates a document, adds a user to it, and verifies that
     * the document correctly recognizes the presence of the user.
     * </p>
     */
    @Test
    public void testIsUserInDocument() {
        Document document = new Document("Test");
        User user = new User("User1");
        document.addUser(user);
        assertTrue(document.isUserInDocument(user));
    }

    /**
     * Test getting the line count of a document.
     * <p>
     * This test creates a document and asserts that the initial line count is 1.
     * </p>
     */
    @Test
    public void testGetLineCount() {
        Document document = new Document("Test");
        assertEquals(1, document.getLineCount());
    }

    /**
     * Test inserting a character into a document.
     * <p>
     * This test creates a document and inserts a character at position (0, 0),
     * asserting that the insertion was successful.
     * </p>
     */
    @Test
    public void testInsert() {
        Document document = new Document("Test");
        assertTrue(document.insert(0, 0, 'a'));
    }

    /**
     * Test modifying a character in a document.
     * <p>
     * This test creates a document, inserts a character, modifies it to 'b',
     * and asserts that the modification was successful.
     * </p>
     */
    @Test
    public void testModify() {
        Document document = new Document("Test");
        document.insert(0, 0, 'a');
        assertTrue(document.modify(0, 0, 'b'));
    }


    /**
     * Test the concurrent insertion of characters into a document from multiple threads.
     * <p>
     * This test creates 10 threads that concurrently insert characters into a document.
     * Each thread inserts 100 characters at the beginning of the document.
     * The test ensures that all threads have completed their insertions before asserting
     * that the length of the document is 1000 characters.
     * </p>
     *
     * @throws InterruptedException if any thread is interrupted
     */
    @Test
    public void testMultipleInsert() throws InterruptedException {
        // AtomicBoolean to signal threads to start insertion
        AtomicBoolean avosmarques = new AtomicBoolean(false);

        // Create a document with an initial content "toto"
        Document d = new Document("toto");

        // Create an array of 10 threads
        Thread[] ts = new Thread[10];

        // Initialize and start each thread
        for(int i = 0; i < 10; i++) {
            int finalI = i;
            ts[i] = new Thread(() -> {
                // Wait until the signal is true to start insertion
                while(!avosmarques.get());

                // Insert 100 characters into the document
                for(int y = 0; y < 100; y++) {
                    d.insert(0, 0, (char)(97 + finalI));
                }
            });
            ts[i].start();
        }

        // Set the signal to true, allowing all threads to start insertion
        avosmarques.set(true);

        // Wait for all threads to complete their insertion
        for(int i = 0; i < 10; i++) {
            ts[i].join();
        }

        // Assert that the length of the document is 1000 characters
        assertEquals(1000, d.toString().length());
    }

}
