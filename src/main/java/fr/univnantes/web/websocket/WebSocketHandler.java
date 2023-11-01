package fr.univnantes.web.websocket;

import fr.univnantes.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.document.Document;
import fr.univnantes.web.websocket.instruction.DeleteInstruction;
import fr.univnantes.web.websocket.instruction.InsertInstruction;
import fr.univnantes.web.websocket.instruction.InstructionType;
import fr.univnantes.web.websocket.instruction.WebSocketInstruction;
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

    /**
     * Handles the TextMessage received from the WebSocket
     *
     * @param session   The WebSocket session
     * @param message   The message received
     * @throws IOException  If an I/O error occurs
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        //  Parse the message into a WebSocketInstruction
        WebSocketInstruction parsedInstruction = InstructionType.getConstructedInstruction(message);
        InstructionType type = parsedInstruction.getType();

        //  Search for the user and the document in memory
        User user = userManager.getUser(parsedInstruction.getUserIdentifier());
        Document document = documentManager.getDocument(parsedInstruction.getDocumentIdentifier());

        //  If the user is not found, close the session
        if (user == null) {
            session.sendMessage(new TextMessage("User not found"));
            session.close();
            return;
        }
        //  If the document is not found, close the session
        if (document == null) {
            userManager.removeUser(user.getUUID());
            session.sendMessage(new TextMessage("Document not found"));
            session.close();
            return;
        }

        //  Execute the instruction depending on its type
        switch (type) {
            case INSERT:
                InsertInstruction insertInstruction = (InsertInstruction) parsedInstruction;
                document.insert(insertInstruction.getLineIndex(),
                        insertInstruction.getColumnIndex(),
                        insertInstruction.getCharacter());
                break;
            case DELETE:
                DeleteInstruction deleteInstruction = (DeleteInstruction) parsedInstruction;
                document.delete(deleteInstruction.getLineIndex(),
                        deleteInstruction.getColumnIndex());
                break;
            case CONNECT:
                //  Check if the user is already in the document
                //  If so, send an error and don't add the user to the document
                if (document.isUserInDocument(user)) {
                    session.sendMessage(new TextMessage("ERROR - User already connected"));
                    return;
                }
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
        //  TODO: Handle the disconnection of a user
        //  Might need to remove to be able to have a map with session
        //  as key and a record of the user and the document as value
    }
}