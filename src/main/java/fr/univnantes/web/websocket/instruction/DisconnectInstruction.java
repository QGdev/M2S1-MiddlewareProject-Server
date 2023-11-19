package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;
import java.util.concurrent.Callable;

import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;
import static fr.univnantes.web.websocket.instruction.Utils.generateInfoMessage;

/**
 * Represents a websocket user disconnection instruction.
 * <p>
 *     A user disconnection instruction is sent by a client when a user disconnects from a document.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "DISCONNECT
 *     "userId": "user1",
 *     }
 * </p>
 */
public class DisconnectInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = InstructionType.DISCONNECT;
    private final UUID userIdentifier;

    /**
     * Creates a new document name change instruction
     *
     * @param message The message containing the TextMessage
     */
    public DisconnectInstruction(TextMessage message) {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload.isBlank() || payload.isEmpty()) throw new IllegalArgumentException("Payload is empty or blank");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has(JSONAttributes.TYPE)) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString(JSONAttributes.TYPE);
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not " + TYPE.type);

        //  Parse the payload userIdentifier
        if (!json.has(JSONAttributes.USER_ID)) throw new IllegalArgumentException("Does not contain a userId");
        String userId = json.getString(JSONAttributes.USER_ID);
        if (userId == null) throw new IllegalArgumentException("userId is null");
        this.userIdentifier = UUID.fromString(userId);
    }

    /**
     * Returns the type of the instruction
     *
     * @return The type
     */
    @Override
    public InstructionType getType() {
        return TYPE;
    }

    /**
     * Returns the user identifier of the instruction
     *
     * @return The user identifier
     */
    @Override
    public UUID getUserId() {
        return userIdentifier;
    }

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
    @Override
    public Callable<Boolean> getCallable(WebSocketSessionManager sessionManager, WebSocketSession session, DocumentManager documentManager, UserManager userManager, Object... args) {
        return () -> {
            //  Check if the user is connected
            if (!sessionManager.isAlreadyConnected(session)) {
                session.sendMessage(new TextMessage(generateErrorMessage("User is not connected")));
                return false;
            }

            //  Check if the user is connected to a document
            UUID documentId = sessionManager.getDocumentId(session);
            UUID userId = sessionManager.getUserId(session);

            if (documentId == null || userId == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("User is not connected to a document")));
                return false;
            }

            //  Verify that the document exists
            Document document = documentManager.getDocument(documentId);
            //  If the document does not exist, unlink the user, close the session and return false
            if (document == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("Document does not exist")));
                session.close();

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);

                return false;
            }

            //  Unlink everything related to the user except the session, the session will be closed
            User user = userManager.getUser(userIdentifier);
            if (user != null)   document.removeUser(user);
            userManager.removeUser(userIdentifier);

            //  Send a validation message to the user
            session.sendMessage(new TextMessage(generateInfoMessage("Disconnected, closing session, bye bye !")));
            session.close();

            return true;
        };
    }

    /**
     * Returns broadcastable version of the instruction.
     * The one who will be sent to the other users.
     *
     * @return The broadcastable version of the instruction.
     */
    @Override
    public JSONObject getBroadcastVersion() {
        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.USER_ID, userIdentifier);
    }

    /**
     * Returns string representation of the instruction.
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .toString();
    }

    /**
     * Used to generate broadcasted message as a JSON String
     * @param userIdentifier    userIdentifier of the disconnected user
     * @return  The resulting JSON String
     */
    public static String generateBroadcastMessage(UUID userIdentifier) {
        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .toString();
    }
}