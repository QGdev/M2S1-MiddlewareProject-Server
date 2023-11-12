package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Callable;

/**
 * Represents a websocket instruction.
 * <p>
 *     A websocket instruction is sent by a client when a user inserts or deletes a character in a document.
 *     It is used to manipulate a document.
 * </p>
 */
public interface WebSocketInstruction {

    /**
     * Represents the attributes of a websocket instruction in JSON format.
     */
    final class JSONAttributes {
        public static final String TYPE = "type";
        public static final String USER_ID = "userId";
        public static final String DOC_ID = "docId";
        public static final String CONTENT = "content";
        public static final String USERS_LIST = "users";
        public static final String USER_NAME = "userName";
        public static final String MESSAGE = "message";
        public static final String LINE_IDX = "lineIdx";
        public static final String COLUMN_IDX = "columnIdx";
        public static final String CHAR = "char";

        /**
         * Private constructor to prevent instantiation.
         */
        private JSONAttributes() {
            throw new IllegalStateException("Utility class");
        }
    }

    /**
     * Returns the type of the instruction.
     * @return The type.
     */
    InstructionType getType();

    /**
     * Returns the user identifier of the instruction.
     * @return The user identifier.
     */
    String getUserIdentifier();

    /**
     * Returns a callable that will execute the instruction.
     *
     * @param sessionManager  The session manager.
     * @param session         The session.
     * @param documentManager The document manager.
     * @param userManager     The user manager.
     * @param args            The other arguments.
     * @return The callable.
     */
    Callable<Boolean> getCallable(WebSocketSessionManager sessionManager, WebSocketSession session, DocumentManager documentManager, UserManager userManager, Object... args);

    /**
     * Returns broadcastable version of the instruction.
     * The one who will be sent to the other users.
     *
     * @return The broadcastable version of the instruction.
     */
    JSONObject getBroadcastVersion();

    /**
     * Returns string representation of the instruction.
     * @return A string representation
     */
    String toString();
}
