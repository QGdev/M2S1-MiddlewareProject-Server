package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Callable;

import static fr.univnantes.web.websocket.instruction.InstructionType.INSERT_CHAR;
import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;

/**
 * Represents a websocket insert character instruction.
 * <p>
 *     An insert character instruction is sent by a client when a user inserts a character in a document.
 *     It contains the line index, the column index, the character and the user identifier.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "INSERT_CHAR",
 *     "lineIdx": 0,
 *     "columnIdx": 0,
 *     "char": "a",
 *     "userId": "user1"
 *     }
 * </p>
 */
public class InsertCharInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = INSERT_CHAR;
    private final int lineIndex;
    private final int columnIndex;
    private final char character;
    private final String userIdentifier;

    /**
     * Creates a new insert character instruction
     *
     * @param message   The message containing the TextMessage
     */
    public InsertCharInstruction(TextMessage message) {
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

        //  Parse the payload columnIndex
        if (!json.has(JSONAttributes.COLUMN_IDX)) throw new IllegalArgumentException("Does not contain a columnIdx");
        int columnIdx = json.getInt(JSONAttributes.COLUMN_IDX);
        if (columnIdx < 0) throw new IllegalArgumentException("columnIdx is negative");
        this.columnIndex = columnIdx;

        //  Parse the payload character
        if (!json.has(JSONAttributes.CHAR)) throw new IllegalArgumentException("Does not contain a char");
        String chr = json.getString(JSONAttributes.CHAR);
        if (chr == null) throw new IllegalArgumentException("char is null");
        if (chr.length() != 1) throw new IllegalArgumentException("char is not a single character");
        this.character = chr.charAt(0);

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
     * Returns the column index of the instruction
     * @return The column index
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Returns the character of the instruction
     * @return The character
     */
    public char getCharacter() {
        return character;
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

            //  Insert the character
            return document.insert(lineIndex, columnIndex, character);
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
                .put(JSONAttributes.COLUMN_IDX, columnIndex)
                .put(JSONAttributes.CHAR, String.valueOf(character))
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
                .put(JSONAttributes.COLUMN_IDX, columnIndex)
                .put(JSONAttributes.CHAR, String.valueOf(character))
                .put(JSONAttributes.USER_ID, userIdentifier)
                .toString();
    }
}
