package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;
import java.util.concurrent.Callable;

import static fr.univnantes.document.Document.isDocumentNameValid;
import static fr.univnantes.web.websocket.instruction.InstructionType.CHANGE_DOC_NAME;
import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;
import static fr.univnantes.web.websocket.instruction.Utils.generateWarnMessage;

/**
 * Represents a websocket document name change instruction.
 * <p>
 *     A document document name change instruction is sent by a client when a user changes the name of a document.
 *     It contains the new document name, the user identifier.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "CHANGE_DOC_NAME",
 *     "newName": "My new rillettes recipe",
 *     "userId": "user1",
 *     }
 * </p>
 */
public class ChangeDocNameInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = CHANGE_DOC_NAME;
    private final String newName;
    private final UUID userIdentifier;

    /**
     * Creates a new document name change instruction
     *
     * @param message   The message containing the TextMessage
     */
    public ChangeDocNameInstruction(TextMessage message) {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload.isBlank() || payload.isEmpty()) throw new IllegalArgumentException("Payload is empty or blank");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has(JSONAttributes.TYPE)) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString(JSONAttributes.TYPE);
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not " + TYPE.type);

        //  Parse the payload newName
        if (!json.has(JSONAttributes.NEW_DOC_NAME)) throw new IllegalArgumentException("Does not contain a newName");
        String newName = json.getString(JSONAttributes.NEW_DOC_NAME);
        if (newName == null) throw new IllegalArgumentException("newName is null");
        if (!isDocumentNameValid(newName)) throw new IllegalArgumentException("newName is not valid");
        this.newName = newName;

        //  Parse the payload userIdentifier
        if (!json.has(JSONAttributes.USER_ID)) throw new IllegalArgumentException("Does not contain a userId");
        String userId = json.getString(JSONAttributes.USER_ID);
        if (userId == null) throw new IllegalArgumentException("userId is null");
        this.userIdentifier = UUID.fromString(userId);
    }

    /**
     * Returns the type of the instruction
     * @return The type
     */
    @Override
    public InstructionType getType() {
        return TYPE;
    }

    /**
     * Returns the new document name of the instruction
     * @return The new document name
     */
    public int getNewName() {
        return getNewName();
    }

    /**
     * Returns the user identifier of the instruction
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
            UUID documentIdentifier = sessionManager.getDocumentId(session);

            if (documentIdentifier == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("User is not connected to a document")));
                return false;
            }

            //  Verify that the document exists
            Document document = documentManager.getDocument(documentIdentifier);
            //  If the document does not exist, unlink the user, close the session and return false
            if (document == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("Document does not exist")));
                session.close();

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);

                return false;
            }

            //  Check if the new document name is the same as the old one
            if (document.getName().equals(newName)) {
                session.sendMessage(new TextMessage(generateWarnMessage("New document name is the same as the old one, nothing to do")));
                return false;
            }

            //  If everything is ok, change the name of the document
            document.setName(newName);
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
                .put(JSONAttributes.NEW_DOC_NAME, newName)
                .put(JSONAttributes.USER_ID, userIdentifier);
    }

    /**
     * Returns string representation of the instruction.
     * @return A string representation
     */
    @Override
    public String toString() {
        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.NEW_DOC_NAME, newName)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .toString();
    }
}
