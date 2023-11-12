package fr.univnantes.web.rest;

import fr.univnantes.document.Document;
import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.User;
import fr.univnantes.user.UserManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final Logger logger = LoggerFactory.getLogger(RestApiController.class);
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
     *                      - The content of the document
     *                      - The user id
     *                      - The user name
     *                  In the form:
     *                  {
     *                      "document": {
     *                          "id": "documentId",
     *                          "name": "documentName"
     *                          "content": "documentContent"
     *                      },
     *                      "user": {
     *                          "id": "userId",
     *                          "name": "userName"
     *                      }
     *                  }
     *
     * @apiNote         If the document could not be created, returns an error as an HTTP 500 error code
     */
    //  TODO:   CROSS ORIGIN, NEED TO NARROW IT DOWN TO THE FRONTEND, NOW JUST ACCEPTS EVERYTHING FOR TESTING PURPOSES
    @CrossOrigin(origins = "*")
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestParam(name = "docName") String documentName, @RequestParam(name = "userName") String userName) {
        if (documentName == null) return ResponseEntity.badRequest().body("HTTP 400 - Document name is null");
        if (userName == null) return ResponseEntity.badRequest().body("HTTP 400 - User name is null");

        if (documentName.isBlank()) return ResponseEntity.badRequest().body("HTTP 400 - Document name is empty");
        if (userName.isBlank()) return ResponseEntity.badRequest().body("HTTP 400 - User name is empty");


        Document document = documentManager.createDocument(documentName);

        //  Check if the document exists
        if (document == null) {
            logger.error("Document could not be created");
            return ResponseEntity.internalServerError().body("HTTP 500 - The document could not be created");
        }

        User user = userManager.createUser(userName);
        document.addUser(user);

        logger.info("Document {} created by user {}", document.getUUID(), user.getUUID());

                //  Create a JSON object to return
        JSONObject returnedJSON = createJSONUserDocument(user, document);
        return ResponseEntity.accepted().body(returnedJSON.toString());
    }

    /**
     * Joins a document and returns it
     *
     * @param documentId    The id of the document
     * @param userName      The name of the user
-     * @return         The created document as a JSON object containing
     *                      - The document id
     *                      - The document name
     *                      - The content of the document
     *                      - The user id
     *                      - The user name
     *                  In the form:
     *                  {
     *                      "document": {
     *                          "id": "documentId",
     *                          "name": "documentName"
     *                          "content": "documentContent"
     *                      },
     *                      "user": {
     *                          "id": "userId",
     *                          "name": "userName"
     *                      }
     *                  }
     *
     * @apiNote         If the document could not be joined because it does not exist,
     *                  it will return an error as an HTTP 404 error code
     */
    //  TODO:   CROSS ORIGIN, NEED TO NARROW IT DOWN TO THE FRONTEND, NOW JUST ACCEPTS EVERYTHING FOR TESTING PURPOSES
    @CrossOrigin(origins = "*")
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestParam(name = "docId") String documentId, @RequestParam(name = "userName") String userName) {
        if (documentId == null) return ResponseEntity.badRequest().body("HTTP 400 - Document id is null");
        if (userName == null) return ResponseEntity.badRequest().body("HTTP 400 - User name is null");

        if (documentId.isBlank()) return ResponseEntity.badRequest().body("HTTP 400 - Document id is empty");
        if (userName.isBlank()) return ResponseEntity.badRequest().body("HTTP 400 - User name is empty");

        Document document = documentManager.getDocument(documentId);

        //  Check if the document exists
        if (document == null)   return ResponseEntity.notFound().build();

        User user = userManager.createUser(userName);
        document.addUser(user);

        logger.info("Document {} joined by user {}", document.getUUID(), user.getUUID());

        //  Create a JSON object to return
        JSONObject returnedJSON = createJSONUserDocument(user, document);
        return ResponseEntity.accepted().body(returnedJSON.toString());
    }
}
