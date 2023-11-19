package fr.univnantes.web.websocket;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.instruction.DisconnectInstruction;
import fr.univnantes.web.websocket.instruction.InstructionType;
import fr.univnantes.web.websocket.instruction.WebSocketInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            logger.error("An error occurred while parsing the message {}, {}", message.getPayload(), e.getMessage());
            return;
        }

        //  Get instruction type
        InstructionType instructionType = parsedInstruction.getType();

        //  Before executing the instruction, check if the user
        if (instructionType.requiresActionTargetCheck) {
            UUID instructionUserId = parsedInstruction.getUserId();
            UUID sessionUserId = webSocketSessionManager.getUserId(session);

            //  If the instruction does not contain a user identifier, send an error message to the user and close the session
            if (instructionUserId == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("Instruction does not contain a user identifier")));
                session.close();
                return;
            }

            //  If the user is not connected, send an error message to the user and close the session
            if (sessionUserId == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("User is not connected")));
                session.close();
                return;
            }

            //  Check if the provided user identifier is the same as the one registered for the session
            if (!instructionUserId.equals(sessionUserId)) {
                session.sendMessage(new TextMessage(generateErrorMessage("User identifier does not match the one registered for the session")));
                session.close();
                logger.warn("User {}, tried to execute {} with user identifier {}", sessionUserId, instructionType.type, instructionUserId);
                return;
            }

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
            session.sendMessage(new TextMessage(generateErrorMessage("An error occurred while executing the instruction. Cause: " + e.getMessage())));
            logger.error("An error occurred while executing the instruction. Cause: {}", e.getMessage());
            return;
        }

        //  If the operation failed, do not broadcast the message
        if (!didOperationSucceeded) return;

        //  If the instruction is a broadcast instruction, broadcast the message to all users
        if (!instructionType.needsBroadcast)  return;

        //  Retrieve the document and broadcast the message to all users
        UUID documentId = webSocketSessionManager.getDocumentId(session);
        Document document = documentManager.getDocument(documentId);

        TextMessage broadcastMessage = new TextMessage(parsedInstruction.getBroadcastVersion().toString());

        //  Broadcast the message to all users but not the user who sent the message
        for (User u : document.getUsers().values()) {
            if (u.getSession() != null && u.getSession().isOpen()) {
                    u.getSession().sendMessage(broadcastMessage);
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

        //  Search for the document in memory
        UUID documentId = webSocketSessionManager.getDocumentId(session);
        UUID userIdentifier = webSocketSessionManager.getUserId(session);

        //  If the user is not connected, there is nothing we can do
        if (userIdentifier == null) return;

        //  If there is no document, remove the user from the user manager and remove the session from the session manager
        if (documentId == null) {
            userManager.removeUser(userIdentifier);
            webSocketSessionManager.removeSession(session);
            return;
        }

        //  Fetch the document and the user
        Document document = documentManager.getDocument(documentId);
        User user = userManager.getUser(userIdentifier);

        //  If the document does not exist, remove the user from the user manager and remove the session from the session manager
        if (document == null) {
            userManager.removeUser(userIdentifier);
            webSocketSessionManager.removeSession(session);
            return;
        }

        //  If the user does exist, remove it from the document
        if (user != null)   document.removeUser(user);

        //  Remove the session from the session manager and user from the user manager
        webSocketSessionManager.removeSession(session);
        userManager.removeUser(userIdentifier);

        //  Get the list of users in the document and broadcast the message to all users
        Map<UUID, User> usersMap = document.getUsers();
        List<User> users = List.copyOf(usersMap.values());

        //  TODO:   NEED TO DECIDE WHAT TO DO WHEN A USER LEAVES AND THERE ARE NO USERS LEFT IN THE DOCUMENT
        //  If there are no users left in the document, remove the document from the document manager
        if (users.isEmpty()) {
            //documentManager.removeDocument(documentId);
            return;
        }

        //  Broadcast the message to all users but not the user who sent the message
        String message = DisconnectInstruction.generateBroadcastMessage(userIdentifier);

        //  Broadcast the message to all users that are still connected
        for (User u : users) {
            if (u.getSession() != null && u.getSession().isOpen()) {
                u.getSession().sendMessage(new TextMessage(message));
            }
        }
        logger.info("User {} disconnected from document {}", userIdentifier, documentId);
    }
}