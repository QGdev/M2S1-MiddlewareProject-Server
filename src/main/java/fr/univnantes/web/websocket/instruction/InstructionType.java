package fr.univnantes.web.websocket.instruction;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a websocket instruction.
 * <p>
 *     A websocket instruction is sent by a client when a user inserts or deletes a character in a document.
 *     It is used to manipulate a document.
 * </p>
 */
public enum InstructionType {
    INSERT_CHAR("INSERT_CHAR", true, true, InsertCharInstruction.class),
    INSERT_LINE_BRK("INSERT_LINE_BRK", true, true, InsertLineBrkInstruction.class),
    DELETE_CHAR("DELETE_CHAR", true, true, DeleteCharInstruction.class),
    DELETE_LINE_BRK("DELETE_LINE_BRK", true, true, DeleteLineBrkInstruction.class),
    CONNECT("CONNECT", false, true, ConnectInstruction.class),
    CHANGE_DOC_NAME("CHANGE_DOC_NAME", true, true, ChangeDocNameInstruction.class),
    DISCONNECT("DISCONNECT", true, false, DisconnectInstruction.class);

    public final String type;
    public final boolean requiresActionTargetCheck;
    public final boolean needsBroadcast;
    public final Class<? extends WebSocketInstruction> instructionClass;

    /**
     * Creates a new instruction type
     *
     * @param type              The type of the instruction
     * @param requiresActionTargetCheck Whether the instruction requires an action target check or not
     *                                  (The user action can only be performed on the user not on another user)
     * @param needsBroadcast            Whether the instruction needs to be broadcasted to all users or not after execution
     * @param instructionClass          The class of the instruction which extends WebSocketInstruction
     */
    InstructionType(String type, boolean requiresActionTargetCheck, boolean needsBroadcast, Class<? extends WebSocketInstruction> instructionClass) {
        this.type = type;
        this.requiresActionTargetCheck = requiresActionTargetCheck;
        this.needsBroadcast = needsBroadcast;
        this.instructionClass = instructionClass;
    }

    /**
     * Returns the instruction type from its string representation
     *
     * @param text  The string representation of the instruction type
     * @return      The instruction type
     */
    public static InstructionType fromString(String text) {
        for (InstructionType b : InstructionType.values()) {
            if (b.type.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Returns the type of the instruction.
     * @return The type.
     */
    @Override
    public String toString() {
        return type;
    }

    /**
     * Returns the instruction from the TextMessage
     *
     * @param message   The message containing the TextMessage
     * @return          The constructed instruction
     *
     * @throws NoSuchMethodException     If the constructor does not exist
     * @throws InvocationTargetException If the constructor throws an exception
     * @throws InstantiationException    If the constructor is not accessible
     * @throws IllegalAccessException    If the constructor is not accessible
     * @throws IllegalArgumentException  If the message is null
     */
    public static WebSocketInstruction getConstructedInstruction(TextMessage message) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload.isBlank() || payload.isEmpty()) throw new IllegalArgumentException("Payload is empty or blank");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has(WebSocketInstruction.JSONAttributes.TYPE)) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString(WebSocketInstruction.JSONAttributes.TYPE);
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Does not contain a type");

        InstructionType instructionType = InstructionType.fromString(type);
        if (instructionType == null) throw new IllegalArgumentException("Type is not INSERT, DELETE or CONNECT");

        return instructionType.instructionClass.getConstructor(TextMessage.class).newInstance(message);
    }
}
