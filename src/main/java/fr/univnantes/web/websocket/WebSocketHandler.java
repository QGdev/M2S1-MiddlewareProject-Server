package fr.univnantes.web.websocket;

import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.document.Document;
import fr.univnantes.web.websocket.instruction.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;


/**
 * WebSocketHandler
 * <p>
 * This class is the handler for the WebSocket.
 * It handles the requests to the WebSocket gets WebSocketInstructions and executes them.
 * It is used to insert and delete characters in a document.
 * </p>
 */
public class WebSocketHandler extends TextWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final DocumentManager documentManager = DocumentManager.getInstance();
    private final UserManager userManager = UserManager.getInstance();
    private final WebSocketSessionManager webSocketSessionManager = WebSocketSessionManager.getInstance();

    /**
     * Handles the TextMessage received from the WebSocket
     *
     * @param session The WebSocket session
     * @param message The message received
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        //  Parse the message into a WebSocketInstruction
        WebSocketInstruction parsedInstruction;
        try {
            parsedInstruction = InstructionType.getConstructedInstruction(message);
        } catch (IllegalArgumentException e) {
            session.sendMessage(new TextMessage(generateErrorMessage(e.getMessage())));
            session.close();
            return;
        }

        //  Execute the instruction depending on its type
        //  Store operation execution result
        boolean didOperationSucceeded = false;

        //  Execute the instruction
        //  If it fails, send an error message to the user and close the session
        try {
            didOperationSucceeded = parsedInstruction
                    .getCallable(webSocketSessionManager,
                            session,
                            documentManager,
                            userManager)
                    .call();
        } catch (Exception e) {
            session.sendMessage(new TextMessage(new JSONObject()
                    .put("type", "ERROR")
                    .put("message", "An error occurred while executing the instruction")
                    .put("instruction", parsedInstruction)
                    .toString()));
            logger.error("An error occurred while executing the instruction", e);
            return;
        }

        //  Send an OK message to the user letting him know that the instruction was executed
        if (!didOperationSucceeded) {
            session.sendMessage(new TextMessage(new JSONObject()
                    .put("type", "ERROR")
                    .put("message", "Operation failed")
                    .put("instruction", parsedInstruction)
                    .toString()));
            return;
        }

        //  Retrieve the document and broadcast the message to all users
        String documentId = webSocketSessionManager.getDocumentId(session);
        Document document = documentManager.getDocument(documentId);

        TextMessage broadcastMessage = new TextMessage(parsedInstruction.getBroadcastVersion().toString());

        //  Broadcast the message to all users but not the user who sent the message
        for (User u : document.getUsers().values()) {
            if (u.getSession() != null) {
                if (u.getSession().isOpen()) {
                    u.getSession().sendMessage(broadcastMessage);
                }
            }

        }
    }

    /**
     * Handles the connection of a user to the WebSocket
     *
     * @param session The WebSocket session
     * @throws IOException If an I/O error occurs
     * @apiNote This method is not implemented for now
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        //  Search for the user and the document in memory
        String userId = webSocketSessionManager.getUserId(session);
        String documentId = webSocketSessionManager.getDocumentId(session);

        if (userId != null) {
            User user = userManager.getUser(userId);
            userManager.removeUser(userId);
            if (documentId != null) {
                Document document = documentManager.getDocument(documentId);
                document.removeUser(user);
                //  TODO: NEED TO SETTLE ON THE BEHAVIOR OF THE DOCUMENT WHEN ALL USERS ARE DISCONNECTED
                //if (document.getUsers().isEmpty()) {
                //    documentManager.removeDocument(documentId);
                //}
                webSocketSessionManager.removeSession(session);
            }
        }
    }
}