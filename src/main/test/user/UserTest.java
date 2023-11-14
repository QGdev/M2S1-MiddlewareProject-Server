package user;

import fr.univnantes.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        String userName = "John";
        User user = new User(userName);

        assertEquals(userName, user.getName());
        assertNotNull(user.getUUID());
        assertNull(user.getSession());
    }

    @Test
    void testSetAndGetSession() {
        User user = new User("Alice");

        WebSocketSession session = createMockSession();
        user.setSession(session);

        assertEquals(session, user.getSession());
    }

    // Helper method to create a mock WebSocketSession for testing
    private WebSocketSession createMockSession() {
        // Implement your mock WebSocketSession here
        // For simplicity, you can use a mocking framework like Mockito
        // Example: return Mockito.mock(WebSocketSession.class);
        return null; // Replace with actual implementation
    }
}
