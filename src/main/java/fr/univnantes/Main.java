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

        /*
        String testString = "Ceci est un test";
        char[] charArray = testString.toCharArray();

        for (int i = 5; i > 0; i--) {
            for (int j = charArray.length - 1 ; j >= 0; j--) {
            //for (int j = 0 ; j < charArray.length; j++) {
                document.insert(i, j, charArray[j]);
                System.out.println("i = " + i + " j = " + j + " char = " + charArray[j] + " document = " + document);
            }
        }*/


        /*for (int c = 0; c < 5; c++) {
            document.insert(0, 0, (char) (c + 97));
        }*/

        //  Insert 5 lines of 5 characters
        // abcde
        // fghij
        // klmno
        // pqrst
        // uvwxy

        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 5; c++) {
                document.insert(l + 10, c, (char) (l * 5 + c + 97));
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
                    document.modify(10+l, c, ' ');
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