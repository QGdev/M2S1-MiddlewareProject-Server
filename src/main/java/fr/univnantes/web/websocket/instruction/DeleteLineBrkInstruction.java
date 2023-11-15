package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Callable;

import static fr.univnantes.web.websocket.instruction.InstructionType.DELETE_LINE_BRK;
import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;

/**
 * Represents a websocket line break delete instruction.
 * <p>
 *     A line break delete instruction is sent by a client when a user deletes a line break in a document.
 *     It contains the line index, the user identifier and the document identifier.
 *     The column index is not needed because the line break is always at the end of the line.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "DELETE_LINE_BRK",
 *     "lineIdx": 0,
 *     "userId": "user1"
 *     }
 * </p>
 */
public class DeleteLineBrkInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = DELETE_LINE_BRK;
    private final int lineIndex;
    private final String userIdentifier;

    /**
     * Creates a line break delete instruction
     *
     * @param message   The message containing the TextMessage
     */
    public DeleteLineBrkInstruction(TextMessage message) {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload.isBlank() || payload.isEmpty()) throw new IllegalArgumentException("Payload is empty or blank");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has(JSONAttributes.TYPE)) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString(JSONAttributes.TYPE);
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not " + TYPE.type);

        //  Parse the payload lineIndex
        if (!json.has(JSONAttributes.LINE_IDX)) throw new IllegalArgumentException("Does not contain a lineIdx");
        int lineIdx = json.getInt(JSONAttributes.LINE_IDX);
        if (lineIdx < 0) throw new IllegalArgumentException("lineIdx is negative");
        this.lineIndex = lineIdx;

        //  Parse the payload userIdentifier
        if (!json.has(JSONAttributes.USER_ID)) throw new IllegalArgumentException("Does not contain a userId");
        String userId = json.getString(JSONAttributes.USER_ID);
        if (userId == null) throw new IllegalArgumentException("userId is null");
        this.userIdentifier = userId;
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
     * Returns the line index of the instruction
     * @return The line index
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * Returns the user identifier of the instruction
     * @return The user identifier
     */
    @Override
    public String getUserId() {
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
            String documentIdentifier = sessionManager.getDocumentId(session);

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

            //  If everything is ok, delete the line break
            return document.deleteLineBreak(lineIndex);
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
                .put(JSONAttributes.LINE_IDX, lineIndex)
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
                .put(JSONAttributes.LINE_IDX, lineIndex)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .toString();
    }
}
