package fr.univnantes.web.rest;

import org.json.JSONObject;

import fr.univnantes.document.Document;
import fr.univnantes.User;

/**
 * Utils
 * <p>
 *     This class contains utility methods for the REST API.
 *     It is used to create JSON objects.
 * </p>
 */
public class Utils {

    /**
     * Creates a JSON object representing a document
     *
     * @param documentId        The document id
     * @param documentName      The document name
     * @param documentContent   The document content
     * @return  A JSON object representing a document
     *          In the form:
     *          {
     *              "id": "documentId",
     *              "name": "documentName",
     *              "content": "documentContent"
     *          }
     */
    public static JSONObject createJSONDocument(String documentId, String documentName, String documentContent) {
        JSONObject documentJSON = new JSONObject();
        documentJSON.put("id", documentId);
        documentJSON.put("name", documentName);
        documentJSON.put("content", documentContent);
        return documentJSON;
    }

    /**
     * Creates a JSON object representing a document
     *
     * @param document        The document object
     * @return  A JSON object representing a document
     *          In the form:
     *          {
     *              "id": "documentId",
     *              "name": "documentName",
     *              "content": "documentContent"
     *          }
     */
    public static JSONObject createJSONDocument(Document document) {
        return createJSONDocument(document.getUUID(), document.getName(), document.toString());
    }

    /**
     * Creates a JSON object representing a user
     *
     * @param userId    The user id
     * @param username  The username
     * @return        A JSON object representing a user
     *          In the form:
     *          {
     *              "id": "userId",
     *              "name": "username"
     *          }
     */
    public static JSONObject createJSONUser(String userId, String username) {
        JSONObject userJSON = new JSONObject();
        userJSON.put("id", userId);
        userJSON.put("name", username);
        return userJSON;
    }

    /**
     * Creates a JSON object representing a user
     *
     * @param user  The user object
     * @return  A JSON object representing a user
     *          In the form:
     *          {
     *              "id": "userId",
     *              "name": "username"
     *          }
     */
    public static JSONObject createJSONUser(User user) {
        return createJSONUser(user.getUUID(), user.getName());
    }

    /**
     * Creates a JSON object representing a user and a document
     *
     * @param userId            The user id
     * @param username          The username
     * @param documentId        The document id
     * @param documentName      The document name
     * @param documentContent   The document content
     * @return          A JSON object representing a user and a document
     *                  In the form:
     *                  {
     *                      "document": {
     *                        "id": "documentId",
     *                        "name": "documentName",
     *                        "content": "documentContent"
     *                      },
     *                      "user": {
     *                          "id": "userId",
     *                          "name": "username"
     *                      }
     *                  }
     */
    public static JSONObject createJSONUserDocument(String userId, String username, String documentId, String documentName, String documentContent) {
        JSONObject returnedJSON = new JSONObject();
        returnedJSON.put("document", createJSONDocument(documentId, documentName, documentContent));
        returnedJSON.put("user", createJSONUser(userId, username));
        return returnedJSON;
    }

    /**
     * Creates a JSON object representing a user and a document
     *
     * @param user      The user object
     * @param document  The document object
     * @return          A JSON object representing a user and a document
     *                  In the form:
     *                  {
     *                      "document": {
     *                        "id": "documentId",
     *                        "name": "documentName",
     *                        "content": "documentContent"
     *                      },
     *                      "user": {
     *                          "id": "userId",
     *                          "name": "username"
     *                      }
     *                  }
     */
    public static JSONObject createJSONUserDocument(User user, Document document) {
        JSONObject returnedJSON = new JSONObject();
        returnedJSON.put("document", createJSONDocument(document));
        returnedJSON.put("user", createJSONUser(user));
        return returnedJSON;
    }
}
