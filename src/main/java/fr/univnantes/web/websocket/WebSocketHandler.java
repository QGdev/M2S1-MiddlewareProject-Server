package fr.univnantes.web.websocket;

import fr.univnantes.DocumentManager;
import fr.univnantes.User;
import fr.univnantes.UserManager;
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
import java.util.concurrent.CopyOnWriteArrayList;


public class WebSocketHandler extends TextWebSocketHandler {

    private final DocumentManager documentManager = DocumentManager.getInstance();
    private final UserManager userManager = UserManager.getInstance();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        WebSocketInstruction parsedInstruction = InstructionType.getConstructedInstruction(message);
        InstructionType type = parsedInstruction.getType();

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
                if (document.isUserInDocument(user)) {
                    session.sendMessage(new TextMessage("ERROR - User already connected"));
                    return;
                }
                document.addUser(user);
                user.setSession(session);
                break;
        }

        session.sendMessage(new TextMessage("OK"));

        //  Broadcast the message to all users
        for (User u : document.getUsers().values()) {
            if (u != user && u.getSession() != null) {
                u.getSession().sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}