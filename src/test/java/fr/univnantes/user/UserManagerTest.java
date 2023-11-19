package fr.univnantes.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = UserManager.getInstance();
    }
    /**
     * Test the user creation and retrieval functionality of UserManager.
     * <p>
     * This test sets up a UserManager, creates a user with a specified name,
     * and asserts that the created user can be retrieved by both name and UUID.
     * </p>
     */
    @Test
    void testUserCreationAndRetrieval() {
        String userName = "John";
        User user = userManager.createUser(userName);

        assertNotNull(user);
        assertEquals(userName, user.getName());

        User retrievedUser = userManager.getUser(user.getUUID());
        assertNotNull(retrievedUser);
        assertEquals(user, retrievedUser);
    }

    /**
     * Test the removal of a user from UserManager.
     * <p>
     * This test sets up a UserManager, creates a user with a specified name,
     * removes the user by UUID, and asserts that the user is successfully removed.
     * </p>
     */
    @Test
    void testRemoveUser() {
        String userName = "Alice";
        User user = userManager.createUser(userName);

        assertTrue(userManager.removeUser(user.getUUID()));
        assertNull(userManager.getUser(user.getUUID()));
    }

    /**
     * Test the singleton behavior of UserManager by ensuring that the same instance
     * is returned when calling getInstance() multiple times.
     * <p>
     * This test sets up a UserManager, retrieves two instances using getInstance(),
     * and asserts that they are the same, confirming the singleton pattern implementation.
     * </p>
     */
    @Test
    void testGetInstance() {
        UserManager instance1 = UserManager.getInstance();
        UserManager instance2 = UserManager.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

}
