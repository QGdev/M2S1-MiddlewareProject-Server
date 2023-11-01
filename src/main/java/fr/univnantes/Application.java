package fr.univnantes;

import fr.univnantes.document.DocumentManager;
import fr.univnantes.user.UserManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class of the application.
 * <p>
 *     This class is used to start the application.
 *     It is also used to initialize the document manager and the user manager.
 * </p>
 */
@SpringBootApplication
public class Application {

    /**
     * Main method of the application.
     * @param args  The arguments of the application passed by the command line (only used for SpringBoot)
     */
    public static void main(String[] args) {
        DocumentManager documentManager = DocumentManager.getInstance();
        UserManager userManager = UserManager.getInstance();

        SpringApplication.run(Application.class, args);
    }
}