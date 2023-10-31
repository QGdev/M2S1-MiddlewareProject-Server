package fr.univnantes.web.websocket.instruction;

public interface WebSocketInstruction {

    /**
     * Returns the type of the instruction.
     * @return The type of the instruction.
     */
    public InstructionType getType();

    /**
     * Returns the user identifier of the instruction.
     * @return The user identifier of the instruction.
     */
    public String getUserIdentifier();

    /**
     * Returns the document identifier of the instruction.
     * @return The document identifier of the instruction.
     */
    public String getDocumentIdentifier();
}
