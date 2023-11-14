package user;

import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = UserManager.getInstance();
    }

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

    @Test
    void testRemoveUser() {
        String userName = "Alice";
        User user = userManager.createUser(userName);

        assertTrue(userManager.removeUser(user.getUUID()));
        assertNull(userManager.getUser(user.getUUID()));
    }

    @Test
    void testGetInstance() {
        UserManager instance1 = UserManager.getInstance();
        UserManager instance2 = UserManager.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }
}
