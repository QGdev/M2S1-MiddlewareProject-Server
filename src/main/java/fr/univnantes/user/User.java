package fr.univnantes.user;

import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

/**
 * Class representing a user.
 * <p>
 *     A user is identified by a UUID and a name.
 * </p>
 */
public class User {

    private final UUID uuid;
    private final String name;

    private WebSocketSession session;

    /**
     * Creates a new user
     *
     * @param name  The name of the user
     */
    public User(String name) {
        this.uuid = java.util.UUID.randomUUID();
        this.name = name;
    }

    /**
     * Used to get the username
     * @return  The username
     */
    public String getName() {
        return name;
    }

    /**
     * Used to get the UUID
     * @return  The UUID of the user
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Used to get the WebSocket session
     * @return  The WebSocket session
     */
    public WebSocketSession getSession() {
        return session;
    }

    /**
     * Used to set the WebSocket session
     * @param session   The WebSocket session
     */
    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
