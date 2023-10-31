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


        for (int c = 0; c < 5; c++) {
            document.insert(0, 0, (char) (c + 97));
        }

        //  Insert 5 lines of 5 characters
        // abcde
        // fghij
        // klmno
        // pqrst
        // uvwxy

        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 5; c++) {
                document.insert(l + 1, c, (char) (l * 5 + c + 97));
            }
        }

        // Remove one letter on two
        // ace
        // gik
        // moq
        // suw
        // y

        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 5; c++) {
                if ((l + c) % 2 == 0) {
                    document.modify(1+l, c, ' ');
                }
            }
        }
        System.out.println(document);

        // Test deleteLineBreak
        System.out.println(document.getLineCount());
        document.deleteLineBreak(0);
        System.out.println(document.getLineCount());
        document.deleteLineBreak(0);
        System.out.println(document.getLineCount());
        document.deleteLineBreak(0);
        System.out.println(document.getLineCount());
        document.deleteLineBreak(0);
        System.out.println(document.getLineCount());
        document.deleteLineBreak(0);
        System.out.println(document.getLineCount());



        System.out.println(document);

        String test = document.toString();
        char[] test1 = test.toCharArray();

        for (char c : test1) {
            if (c != '\n') {
                System.out.print((byte)(c));
                System.out.print('|');
            }
            else {
                System.out.println();
            }
        }
        System.out.println(document.getLineCount());

    }
}