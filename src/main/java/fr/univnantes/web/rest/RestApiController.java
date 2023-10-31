package fr.univnantes.web.rest;

import fr.univnantes.DocumentManager;
import fr.univnantes.User;
import fr.univnantes.UserManager;
import fr.univnantes.document.Document;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static fr.univnantes.web.rest.Utils.createJSONUserDocument;

/**
 * RestApiController
 * <p>
 *     This class is the controller for the REST API.
 *     It handles the requests to the API.
 *     It is used to create and join documents.
 * </p>
 */
@RestController
public class RestApiController {

    private final DocumentManager documentManager = DocumentManager.getInstance();
    private final UserManager userManager = UserManager.getInstance();

    /**
     * Returns a string containing the current date as a blank holder
     * @return  A string containing the current date as a blank holder
     */
    @GetMapping("/")
    public String index() {
        Date date = new Date();
        return "Hello, it's " + date.toString() + " !";
    }

    /**
     * Creates a new document and returns it
     *
     * @param documentName  The name of the document
     * @param userName      The name of the user
     * @return          The created document as a JSON object containing
     *                      - The document id
     *                      - The document name
     *                      - The user id
     *                      - The user id
     *
     * @apiNote         If the document could not be created, returns an error as an HTTP 500 error code
     */
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestParam(name = "documentName") String documentName, @RequestParam(name = "userName") String userName) {
        if (documentName == null) throw new IllegalArgumentException("Document name is null");
        if (userName == null) throw new IllegalArgumentException("User name is null");

        Document document = documentManager.createDocument(documentName);

        //  Check if the document exists
        if (document == null) {
            return ResponseEntity.internalServerError().body("HTTP 500 - The document could not be created");
        }

        User user = userManager.createUser(userName);

        //  Create a JSON object to return
        JSONObject returnedJSON = createJSONUserDocument(user, document);
        return ResponseEntity.accepted().body(returnedJSON.toString());
    }

    /**
     * Joins a document and returns it
     *
     * @param documentId    The id of the document
     * @param userName      The name of the user
     * @return          The joined document as a JSON object containing
     *                      - The document id
     *                      - The document name
     *                      - The user id
     *                      - The user id
     *
     * @apiNote         If the document could not be joined because it does not exist,
     *                  it will return an error as an HTTP 404 error code
     */
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestParam(name = "documentId") String documentId, @RequestParam(name = "userName") String userName) {
        if (documentId == null) throw new IllegalArgumentException("Document id is null");
        if (userName == null) throw new IllegalArgumentException("User name is null");

        Document document = documentManager.getDocument(documentId);

        //  Check if the document exists
        if (document == null) {
            return ResponseEntity.badRequest().body("HTTP 404 - Requested document not found");
        }

        User user = userManager.createUser(userName);

        //  Create a JSON object to return
        JSONObject returnedJSON = createJSONUserDocument(user, document);
        return ResponseEntity.accepted().body(returnedJSON.toString());
    }

}
