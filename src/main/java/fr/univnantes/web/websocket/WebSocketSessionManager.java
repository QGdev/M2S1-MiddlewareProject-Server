package fr.univnantes.web.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is used to manage the WebSocket sessions.
 * It is used to store the document id and the user id of a session.
 * It is also used to check if a session is already connected.
 */
public class WebSocketSessionManager {

    private static final  AtomicReference<WebSocketSessionManager> instance = new AtomicReference<>(null);
    private final ConcurrentHashMap<WebSocketSession, String> documentsSessions;
    private final ConcurrentHashMap<WebSocketSession, String> sessionToUser;
    private final ConcurrentHashMap<String, WebSocketSession> userToSession;

    /**
     * Creates a new WebSocketSessionManager
     */
    private WebSocketSessionManager() {
        documentsSessions = new ConcurrentHashMap<>();
        sessionToUser = new ConcurrentHashMap<>();
        userToSession = new ConcurrentHashMap<>();
    }

    /**
     * Returns the instance of the WebSocketSessionManager and creates it if it does not exist
     *
     * @return The instance of the WebSocketSessionManager
     */
    public static WebSocketSessionManager getInstance() {
        if (instance.get() == null) {
            synchronized (WebSocketSessionManager.class) {
                instance.compareAndSet(null, new WebSocketSessionManager());
            }
        }
        return instance.get();
    }

    /**
     * Adds a session to the WebSocketSessionManager
     *
     * @param session       The session to add
     * @param documentId    The document id of the session
     * @param userId        The user id of the session
     * @return          True if the session was added, false otherwise
     */
    public boolean addSession(WebSocketSession session, String documentId, String userId) {
        synchronized (WebSocketSessionManager.class) {
            if (documentsSessions.containsKey(session) || sessionToUser.containsKey(session)) {
                return false;
            }
            documentsSessions.put(session, documentId);
            sessionToUser.put(session, userId);
            userToSession.put(userId, session);

            return true;
        }
    }

    /**
     * Removes a session from the WebSocketSessionManager
     *
     * @param session   The session to remove
     * @return          True if the session was removed, false otherwise
     */
    public boolean removeSession(WebSocketSession session) {
        synchronized (WebSocketSessionManager.class) {
            if (!documentsSessions.containsKey(session) || !sessionToUser.containsKey(session)) {
                return false;
            }
            documentsSessions.remove(session);

            if (sessionToUser.containsKey(session)) {
                String userId = sessionToUser.remove(session);
                if (userId != null) {
                    userToSession.remove(userId);
                }
            }

            return true;
        }
    }

    /**
     * Returns the document id of a session
     *
     * @param session   The session to get the document id from
     * @return          The document id of the session, null if the session is not in the WebSocketSessionManager
     */
    public String getDocumentId(WebSocketSession session) {
        return documentsSessions.get(session);
    }

    /**
     * Returns the user id of a session
     *
     * @param session   The session to get the user id from
     * @return          The user id of the session, null if the session is not in the WebSocketSessionManager
     */
    public String getUserId(WebSocketSession session) {
        return sessionToUser.get(session);
    }

    /**
     * Returns the session of a user
     *
     * @param userId   The user id to get the session from
     * @return          The session of the user, null if the user is not in the WebSocketSessionManager
     */
    public WebSocketSession getSession(String userId) {
        return userToSession.get(userId);
    }

    /**
     * Returns the document id of a user
     *
     * @param userId    The user id to get the document id from
     * @return          The document id of the user, null if the user is not in the WebSocketSessionManager
     */
    public String getDocumentId(String userId) {
        WebSocketSession session = getSession(userId);
        if (session == null)    return null;

        return getDocumentId(session);
    }

    /**
     * Checks if a session is already connected
     *
     * @param session   The session to check
     * @return          True if the session is already connected, false otherwise
     */
    public boolean isAlreadyConnected(WebSocketSession session) {
        return documentsSessions.containsKey(session) || sessionToUser.containsKey(session);
    }
}
