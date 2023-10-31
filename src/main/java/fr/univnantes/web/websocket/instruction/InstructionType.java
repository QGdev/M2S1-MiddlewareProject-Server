package fr.univnantes.web.websocket.instruction;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;

import java.lang.reflect.InvocationTargetException;

public enum InstructionType {
    INSERT("INSERT", InsertInstruction.class),
    DELETE("DELETE", DeleteInstruction.class),
    CONNECT("CONNECT", ConnectInstruction.class);

    public final String type;
    public final Class<? extends WebSocketInstruction> instructionClass;

    InstructionType(String type, Class<? extends WebSocketInstruction> instructionClass) {
        this.type = type;
        this.instructionClass = instructionClass;
    }

    public static InstructionType fromString(String text) {
        for (InstructionType b : InstructionType.values()) {
            if (b.type.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static WebSocketInstruction getConstructedInstruction(TextMessage message) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (message == null) throw new IllegalArgumentException("Message is null");

        String payload = message.getPayload();
        if (payload == null) throw new IllegalArgumentException("Payload is null");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has("type")) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString("type");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Does not contain a type");

        InstructionType instructionType = InstructionType.fromString(type);
        if (instructionType == null) throw new IllegalArgumentException("Type is not INSERT, DELETE or CONNECT");

        return instructionType.instructionClass.getConstructor(TextMessage.class).newInstance(message);
    }
}
