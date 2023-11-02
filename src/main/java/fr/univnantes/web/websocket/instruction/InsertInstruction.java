package fr.univnantes.web.websocket.instruction;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

import static fr.univnantes.web.websocket.instruction.InstructionType.INSERT;

/**
 * Represents a websocket insert instruction.
 * <p>
 *     An insert instruction is sent by a client when a user inserts a character in a document.
 *     It contains the line index, the column index, the character, the user identifier and the document identifier.
 * </p>
 */
public class InsertInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = INSERT;
    private final int lineIndex;
    private final int columnIndex;
    private final char character;
    private final String userIdentifier;

    /**
     * Creates a new insert instruction
     *
     * @param message   The message containing the TextMessage
     */
    public InsertInstruction(TextMessage message) {
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

        //  Parse the payload character
        if (!json.has("char")) throw new IllegalArgumentException("Does not contain a char");
        String character = json.getString("char");
        if (character == null) throw new IllegalArgumentException("char is null");
        if (character.length() != 1) throw new IllegalArgumentException("char is not a single character");
        this.character = character.charAt(0);

        //  Parse the payload userIdentifier
        if (!json.has("userId")) throw new IllegalArgumentException("Does not contain a userId");
        String userIdentifier = json.getString("userId");
        if (userIdentifier == null) throw new IllegalArgumentException("userId is null");
        this.userIdentifier = userIdentifier;
    }

    /**
     * Returns the type of the instruction
     * @return The type of the instruction
     */
    @Override
    public InstructionType getType() {
        return TYPE;
    }

    /**
     * Returns the line index of the instruction
     * @return The line index of the instruction
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * Returns the column index of the instruction
     * @return The column index of the instruction
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Returns the character of the instruction
     * @return The character of the instruction
     */
    public char getCharacter() {
        return character;
    }

    /**
     * Returns the user identifier of the instruction
     * @return The user identifier of the instruction
     */
    @Override
    public String getUserIdentifier() {
        return userIdentifier;
    }
}
