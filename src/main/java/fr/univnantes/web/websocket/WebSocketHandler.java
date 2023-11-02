package fr.univnantes.web.websocket;

import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.document.Document;
import fr.univnantes.web.websocket.instruction.*;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * WebSocketHandler
 * <p>
 *     This class is the handler for the WebSocket.
 *     It handles the requests to the WebSocket gets WebSocketInstructions and executes them.
 *     It is used to insert and delete characters in a document.
 * </p>
 */
public class WebSocketHandler extends TextWebSocketHandler {

    private final DocumentManager documentManager = DocumentManager.getInstance();
    private final UserManager userManager = UserManager.getInstance();
    private final WebSocketSessionManager webSocketSessionManager = WebSocketSessionManager.getInstance();

    /**
     * Handles the TextMessage received from the WebSocket
     *
     * @param session   The WebSocket session
     * @param message   The message received
     * @throws IOException  If an I/O error occurs
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        System.out.println("Received message: " + message.getPayload());

        //  Parse the message into a WebSocketInstruction
        WebSocketInstruction parsedInstruction;
        InstructionType instructionType;
        try {
            parsedInstruction = InstructionType.getConstructedInstruction(message);
            instructionType = parsedInstruction.getType();
        }
        catch (IllegalArgumentException e) {
            session.sendMessage(new TextMessage("ERROR - " + e.getMessage()));
            session.close();
            return;
        }


        //  Search for the user in memory
        User user = userManager.getUser(parsedInstruction.getUserIdentifier());

        //  If the user is not found, close the session
        if (user == null) {
            session.sendMessage(new TextMessage("User not found"));
            session.close();
            return;
        }

        //  If it is a connect instruction, check if the user is already connected to the document
        if (instructionType == InstructionType.CONNECT) {
            if (webSocketSessionManager.isAlreadyConnected(session)) {
                session.sendMessage(new TextMessage("ERROR - Already connected"));
                return;
            }
        }
        //  If the user is already connected to the document, just send an error message
        else {
            if (!webSocketSessionManager.isAlreadyConnected(session)) {
                session.sendMessage(new TextMessage("ERROR - Not connected"));
                session.close();
                userManager.removeUser(user.getUUID());
                return;
            }
        }

        //  Execute the instruction depending on its type
        //  But first init the document
        Document document = null;
        switch (instructionType) {
            case INSERT:
                InsertInstruction insertInstruction = (InsertInstruction) parsedInstruction;

                document = documentManager.getDocument(webSocketSessionManager.getDocumentId(session));

                document.insert(insertInstruction.getLineIndex(),
                        insertInstruction.getColumnIndex(),
                        insertInstruction.getCharacter());
                break;
            case DELETE:
                DeleteInstruction deleteInstruction = (DeleteInstruction) parsedInstruction;

                document = documentManager.getDocument(webSocketSessionManager.getDocumentId(session));

                document.delete(deleteInstruction.getLineIndex(),
                        deleteInstruction.getColumnIndex());
                break;
            case CONNECT:
                ConnectInstruction connectInstruction = (ConnectInstruction) parsedInstruction;
                document = documentManager.getDocument(connectInstruction.getDocumentIdentifier());

                //  If the document is not found, close the session
                if (document == null) {
                    userManager.removeUser(user.getUUID());
                    session.sendMessage(new TextMessage("Document not found"));
                    session.close();
                    return;
                }
                webSocketSessionManager.addSession(session, document.getUUID(), user.getUUID());
                document.addUser(user);
                user.setSession(session);
                break;
        }

        //  Send an OK message to the user letting him know that the instruction was executed
        session.sendMessage(new TextMessage("OK"));

        //  Broadcast the message to all users but not the user who sent the message
        for (User u : document.getUsers().values()) {
            if (u != user && u.getSession() != null) {
                u.getSession().sendMessage(message);
            }
        }
    }

    /**
     * Handles the connection of a user to the WebSocket
     *
     * @param session   The WebSocket session
     * @throws IOException  If an I/O error occurs
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