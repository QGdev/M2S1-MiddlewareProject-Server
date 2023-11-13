package fr.univnantes;

import fr.univnantes.document.Document;

/**
 * Main class of the application.
 * <p>
 *     This class is used to start the application.
 *     It is also used to initialize the document manager and the user manager.
 *
 *     But it is also used to test the document class
 * </p>
 */
public class Main {

    /**
     * Main method of the application.
     * @param args  The arguments of the application passed by the command line (not used)
     */
    public static void main(String[] args) {
        Document document = new Document("test");

        for (int i = 0; i < 5; i++) {
            document.insert(0, 0, (char) (i + 97));
        }

        System.out.println(document);
        document.insertLineBreak(0, 0);
        System.out.println("Insert line break");
        System.out.println(document);
        document.deleteLineBreak(0);
        System.out.println("Delete line break");
        System.out.println(document);
    }
}