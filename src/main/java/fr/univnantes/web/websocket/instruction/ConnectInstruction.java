package fr.univnantes.web.websocket.instruction;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import fr.univnantes.web.websocket.WebSocketSessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.Callable;

import static fr.univnantes.web.websocket.instruction.InstructionType.CONNECT;
import static fr.univnantes.web.websocket.instruction.Utils.generateErrorMessage;

/**
 * Represents a websocket connect instruction.
 * <p>
 *     A connect instruction is sent by a client when a user connects to a document.
 *     It contains the user identifier and the document identifier.
 *     The user identifier is used to retrieve the user from the user manager.
 *     The document identifier is used to retrieve the document from the document manager.
 *     If the user or the document does not exist, the connection is refused.
 *
 *     The instruction in JSON format is as follows:
 *     {
 *     "type": "CONNECT",
 *     "userId": "user1",
 *     "docId": "doc1"
 *     }
 * </p>
 */
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
        if (payload.isBlank() || payload.isEmpty()) throw new IllegalArgumentException("Payload is empty or blank");

        //  Parse the payload type
        JSONObject json = new JSONObject(payload);
        if (!json.has(JSONAttributes.TYPE)) throw new IllegalArgumentException("Does not contain a type");

        String type = json.getString(JSONAttributes.TYPE);
        if (type == null) throw new IllegalArgumentException("Does not contain a type");

        if (!type.equals(TYPE.type)) throw new IllegalArgumentException("Type is not " + TYPE.type);

        //  Parse the payload userIdentifier
        if (!json.has(JSONAttributes.USER_ID)) throw new IllegalArgumentException("Does not contain a userId");
        String userId = json.getString(JSONAttributes.USER_ID);
        if (userId == null) throw new IllegalArgumentException("userIdentifier is null");
        this.userIdentifier = userId;

        //  Parse the payload documentIdentifier
        if (!json.has(JSONAttributes.DOC_ID)) throw new IllegalArgumentException("Does not contain a docId");
        String documentId = json.getString(JSONAttributes.DOC_ID);
        if (documentId == null) throw new IllegalArgumentException("docIdentifier is null");
        this.documentIdentifier = documentId;
    }

    /**
     * Returns the type of the instruction
     * @return The type
     */
    @Override
    public InstructionType getType() {
        return TYPE;
    }

    /**
     * Returns the user identifier of the instruction
     * @return The user identifier
     */
    @Override
    public String getUserId() {
        return userIdentifier;
    }

    /**
     * Returns a callable that will execute the instruction.
     *
     * @param sessionManager  The session manager.
     * @param session         The session.
     * @param documentManager The document manager.
     * @param userManager     The user manager.
     * @param args            The other arguments.
     * @return The callable.
     */
    @Override
    public Callable<Boolean> getCallable(WebSocketSessionManager sessionManager, WebSocketSession session, DocumentManager documentManager, UserManager userManager, Object... args) {
        return () -> {
            //  Verify that the session is not already connected
            if (sessionManager.isAlreadyConnected(session)) {
                session.sendMessage(new TextMessage(generateErrorMessage("Already connected")));
                return false;
            }

            //  Verify that the user
            User user = userManager.getUser(userIdentifier);
            //  If the document does not exist, unlink the user, close the session and return false
            if (user == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("User does not exist")));
                session.close();

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);
                return false;
            }

            //  Verify that the document exists
            Document document = documentManager.getDocument(documentIdentifier);

            if (document == null) {
                session.sendMessage(new TextMessage(generateErrorMessage("Document does not exist")));
                session.close();

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);

                return false;
            }

            //  Check if the user is registered to the document
            if (!document.isJoiningUserInDocument(user)) {
                session.sendMessage(new TextMessage(generateErrorMessage("User is not registered to the document")));
                session.close();

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);

                return false;
            }

            //  Add the session to the session manager
            boolean success = sessionManager.addSession(session, documentIdentifier, userIdentifier);
            user.setSession(session);

            if (!success) {
                session.sendMessage(new TextMessage(generateErrorMessage("Could not connect to the document")));
                session.close();

                //  Remove user from joining list
                document.removeJoiningUser(user);

                //  Remove the session from the session manager
                sessionManager.removeSession(session);
                userManager.removeUser(userIdentifier);

                return false;
            }
            //  If everything went well, move the user from the joining list to the user list
            document.removeJoiningUser(user);
            document.addUser(user);

            //  And send the document to the user
            session.sendMessage(new TextMessage(new JSONObject()
                    .put(JSONAttributes.TYPE, CONNECT.type)
                    .put(JSONAttributes.MESSAGE, "Connected")
                    .put(JSONAttributes.USER_ID, userIdentifier)
                    .put(JSONAttributes.CONTENT, document.toString())
                    .toString()));
            return true;

        };
    }

    /**
     * Returns broadcastable version of the instruction.
     * The one who will be sent to the other users.
     *
     * @return The broadcastable version of the instruction.
     * @throws IllegalStateException If the user manager is null
     * @throws IllegalStateException If the username is null
     */
    @Override
    public JSONObject getBroadcastVersion() {
        //  Get the user manager
        UserManager userManager = UserManager.getInstance();
        if (userManager == null) throw new IllegalStateException("UserManager is null");

        //  Get the document manager
        DocumentManager documentManager = DocumentManager.getInstance();
        if (documentManager == null) throw new IllegalStateException("DocumentManager is null");

        //  Get the user name
        String userName = userManager.getUser(userIdentifier).getName();
        if (userName == null) throw new IllegalStateException("User name is null");

        //  Get the document name
        Document document = documentManager.getDocument(documentIdentifier);
        if (document == null) throw new IllegalStateException("Document is null");

        //  Get the user map
        Map<String, User> userMap = document.getUsers();
        if (userMap == null) throw new IllegalStateException("User map is null");

        // Generate the user list
        JSONArray userList = new JSONArray();
        for (User u : userMap.values()) {
            userList.put(new JSONObject()
                    .put(JSONAttributes.USER_ID, u.getUUID())
                    .put(JSONAttributes.USER_NAME, u.getName()));
        }


        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .put(JSONAttributes.USER_NAME, userName)
                .put(JSONAttributes.USERS_LIST, userList);
    }

    /**
     * Returns string representation of the instruction.
     * @return A string representation
     */
    @Override
    public String toString() {
        return new JSONObject()
                .put(JSONAttributes.TYPE, TYPE.type)
                .put(JSONAttributes.USER_ID, userIdentifier)
                .put(JSONAttributes.DOC_ID, documentIdentifier)
                .toString();
    }
}
