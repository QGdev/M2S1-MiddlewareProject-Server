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
