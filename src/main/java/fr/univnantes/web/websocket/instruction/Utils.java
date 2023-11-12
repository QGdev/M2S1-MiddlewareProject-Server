package fr.univnantes.web.websocket.instruction;

import org.json.JSONObject;

/**
 * Utility class of the websocket instructions package.
 * <p>
 *     This class is used to generate error messages.
 *     It is used by the websocket instructions.
 *     But might add other utility methods in the future.
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation.
     */
    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates an error message from a string.
     *
     * @param message The error message
     * @return The error message as a JSON string
     */
    public static String generateErrorMessage(String message) {
        return new JSONObject()
                .put("type", "ERROR")
                .put("message", message)
                .toString();
    }
}
