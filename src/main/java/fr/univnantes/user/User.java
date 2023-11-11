package fr.univnantes.user;

import org.springframework.web.socket.WebSocketSession;

/**
 * Class representing a user.
 * <p>
 *     A user is identified by a UUID and a name.
 * </p>
 */
public class User {

    private final String uuid;
    private final String name;

    private WebSocketSession session;

    /**
     * Creates a new user
     *
     * @param name  The name of the user
     */
    public User(String name) {
        this.uuid = java.util.UUID.randomUUID().toString();
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
    public String getUUID() {
        return uuid;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }
}
