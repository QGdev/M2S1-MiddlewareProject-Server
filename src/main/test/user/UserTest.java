package user;

import fr.univnantes.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    /**
     * Test the creation of a User with the specified name.
     * <p>
     * This test creates a User with a given name and asserts that the user's
     * name matches the expected value, the UUID is not null, and the session is null.
     * </p>
     */
    @Test
    void testUserCreation() {
        String userName = "John";
        User user = new User(userName);

        assertEquals(userName, user.getName());
        assertNotNull(user.getUUID());
        assertNull(user.getSession());
    }

    /**
     * Test setting and getting the WebSocket session for a User.
     * <p>
     * This test creates a User, sets a mock WebSocket session using the helper method,
     * and asserts that the session can be retrieved correctly.
     * </p>
     */
    @Test
    void testSetAndGetSession() {
        User user = new User("Alice");

        WebSocketSession session = createMockSession();
        user.setSession(session);

        assertEquals(session, user.getSession());
    }

    /**
     * Helper method to create a mock WebSocketSession for testing.
     * <p>
     * This method is used in the test to create a mock WebSocket session,
     * allowing for the simulation of a WebSocket session in the testing environment.
     * </p>
     *
     * @return A mock WebSocketSession for testing purposes.
     *         Note: For simplicity, you can use a mocking framework like Mockito
     *         (Example: return Mockito.mock(WebSocketSession.class);)
     */
    private WebSocketSession createMockSession() {
        // Using Mockito to create a mock WebSocketSession
        return Mockito.mock(WebSocketSession.class);
    }
}
