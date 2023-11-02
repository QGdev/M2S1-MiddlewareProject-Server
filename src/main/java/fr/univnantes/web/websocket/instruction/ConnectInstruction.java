package fr.univnantes.web.websocket.instruction;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

import static fr.univnantes.web.websocket.instruction.InstructionType.CONNECT;

public class ConnectInstruction implements WebSocketInstruction {

    private static final InstructionType TYPE = CONNECT;
    private final String userIdentifier;
    private final String documentIdentifier;

    /**
     * Creates a new insert instruction
     *
     * @param message   The message containing the TextMessage
     */
    public ConnectInstruction(TextMessage message) {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload == null) throw new IllegalArgumentException("Payload is null");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has("type")) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString("type");
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not INSERT");

        //  Parse the payload userIdentifier
        if (!json.has("userId")) throw new IllegalArgumentException("Does not contain a userId");
        String userIdentifier = json.getString("userId");
        if (userIdentifier == null) throw new IllegalArgumentException("userIdentifier is null");
        this.userIdentifier = userIdentifier;

        //  Parse the payload documentIdentifier
        if (!json.has("docId")) throw new IllegalArgumentException("Does not contain a docId");
        String documentIdentifier = json.getString("docId");
        if (documentIdentifier == null) throw new IllegalArgumentException("docIdentifier is null");
        this.documentIdentifier = documentIdentifier;
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
     * Returns the user identifier of the instruction
     * @return The user identifier of the instruction
     */
    @Override
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Returns the document identifier of the instruction
     * @return The document identifier of the instruction
     */
    public String getDocumentIdentifier() {
        return documentIdentifier;
    }
}
