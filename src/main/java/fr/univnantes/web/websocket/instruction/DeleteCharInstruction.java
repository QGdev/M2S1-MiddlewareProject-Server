package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Callable;

import static fr.univnantes.web.websocket.instruction.InstructionType.DELETE_CHAR;

/**
 * Represents a websocket char delete instruction.
 * <p>
 *     A char delete instruction is sent by a client when a user deletes a character in a document.
 *     It contains the line index, the column index, the user identifier and the document identifier.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "DELETE_CHAR",
 *     "lineIdx": 0,
 *     "columnIdx": 0,
 *     "userId": "user1"
 *     }
 * </p>
 */
public class DeleteCharInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = DELETE_CHAR;
    private final int lineIndex;
    private final int columnIndex;
    private final String userIdentifier;

    /**
     * Creates a new char delete instruction
     *
     * @param message   The message containing the TextMessage
     */
    public DeleteCharInstruction(TextMessage message) {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload == null) throw new IllegalArgumentException("Payload is null");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has("type")) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString("type");
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not INSERT");

        //  Parse the payload lineIndex
        if (!json.has("lineIdx")) throw new IllegalArgumentException("Does not contain a lineIdx");
        int lineIndex = json.getInt("lineIdx");
        if (lineIndex < 0) throw new IllegalArgumentException("lineIdx is negative");
        this.lineIndex = lineIndex;

        //  Parse the payload columnIndex
        if (!json.has("columnIdx")) throw new IllegalArgumentException("Does not contain a columnIdx");
        int columnIndex = json.getInt("columnIdx");
        if (columnIndex < 0) throw new IllegalArgumentException("columnIdx is negative");
        this.columnIndex = columnIndex;

        //  Parse the payload userIdentifier
        if (!json.has("userId")) throw new IllegalArgumentException("Does not contain a userId");
        String userIdentifier = json.getString("userId");
        if (userIdentifier == null) throw new IllegalArgumentException("userId is null");
        this.userIdentifier = userIdentifier;
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
     * Returns the column index of the instruction
     * @return The column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Returns the user identifier of the instruction
     * @return The user identifier
     */
    @Override
    public String getUserIdentifier() {
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
                session.sendMessage(new TextMessage(new JSONObject()
                        .put("type", "ERROR")
                        .put("message", "Not connected")
                        .toString()));
                return false;
            }

            //  Check if the user is connected to a document
            String docId = sessionManager.getDocumentId(session);

            if (docId == null) {
                session.sendMessage(new TextMessage(new JSONObject()
                        .put("type", "ERROR")
                        .put("message", "Not connected to a document")
                        .toString()));
                return false;
            }


            //  Check if the document exists
            Document document = documentManager.getDocument(docId);

            if (document == null) {
                session.sendMessage(new TextMessage(new JSONObject()
                        .put("type", "ERROR")
                        .put("message", "Document does not exist")
                        .toString()));
                return false;
            }

            //  If everything is ok, delete the char
            return document.delete(lineIndex, columnIndex);
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
                .put("type", TYPE.type)
                .put("lineIdx", lineIndex)
                .put("columnIdx", columnIndex)
                .put("userId", userIdentifier);
    }

    /**
     * Returns string representation of the instruction.
     * @return A string representation
     */
    @Override
    public String toString() {
        return getBroadcastVersion().toString();
    }
}
