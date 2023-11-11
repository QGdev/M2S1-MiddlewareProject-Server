package fr.univnantes.document;

import fr.univnantes.user.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a document
 * <p>
 *     A document is a list of line nodes
 *     It contains a name, a UUID and a reference to the first line node
 *     It is used to represent a text document
 * </p>
 */
public class Document {

    private final String uuid;
    private String name;
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger lineCount = new AtomicInteger(1);

    private final LineNode content;

    /**
     * Create a new document
     *
     * @param name      The name of the document
     *                  Must not be null or empty
     */
    public Document(String name) {
        if (name == null) throw new IllegalArgumentException("Document name is null");
        if (name.isEmpty()) throw new IllegalArgumentException("Document name is empty");

        this.name = name;
        uuid = java.util.UUID.randomUUID().toString();
        content = new LineNode();
    }

    /**
     * Returns the line node at the given position
     * @param line  The position of the line node
     * @return      The line node at the given position
     */
    private LineNode getLineNode(int line) {
        if (line < 0) return null;

        int currentLine = 0;
        LineNode lineNode = content;
        while (currentLine < line) {
            lineNode = lineNode.getNext();
            if (lineNode == null) break;
            currentLine++;
        }
        if (currentLine < line) return null;

        return lineNode;
    }

    /**
     * Returns the last line node of the document
     * @return  The last line node of the document
     */
    private LineNode getLastLineNode() {
        LineNode lineNode = content;
        while (lineNode.getNext() != null) {
            lineNode = lineNode.getNext();
        }
        return lineNode;
    }

    /**
     * Create a new line node after the given line node
     * @param previousLineNode  The line node after which the new line node is created
     * @return                  True if the line node has been created, false otherwise
     */
    private synchronized boolean createLineNode(LineNode previousLineNode) {
        if (previousLineNode == null) return false;

        LineNode lineNode = new LineNode();
        lineNode.setPrevious(previousLineNode);
        lineNode.setNext(previousLineNode.getNextAcquire());
        previousLineNode.setNextRelease(lineNode);
        if (lineNode.getNext() != null) {
            lineNode.getNext().setPrevious(lineNode);
        }
        return true;
    }

    /**
     * Returns the UUID of the document
     * @return  The UUID of the document
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the name of the document
     * @return  The name of the document
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of the document
     * @param name  The new name of the document
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the users of the document
     * @return  The users of the document
     */
    public ConcurrentMap<String, User> getUsers() {
        return users;
    }

    /**
     * Add a user to the document
     * @param user  The user to add
     */
    public boolean addUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");
        if (users.contains(user.getUUID())) return false;

        return users.put(user.getUUID(), user) == null;
    }

    /**
     * Remove a user from the document
     * @param user  The user to remove
     */
    public boolean removeUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");

        return users.remove(user.getUUID()) != null;
    }

    /**
     * Check if a user is in the document
     * @param user  The user to check
     */
    public boolean isUserInDocument(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");

        return users.contains(user.getUUID());
    }

    /**
     * Returns the number of lines in the document
     * @return  The number of lines in the document
     */
    public int getLineCount() {
        return lineCount.get();
    }

    /**
     * Insert a character at the given position
     * If the position does not exist, it is created
     * If the position already exists, the character is inserted before the existing character
     *
     * @param line          Coordinate of the line, starts at 0
     * @param column        Coordinate of the column, starts at 0
     * @param character     Character to insert
     * @return          True if the character has been inserted, false otherwise
     */
    public synchronized boolean insert(int line, int column, char character) {
        if (line < 0 || column < 0) return false;

        LineNode lineNode = getLineNode(line);

        //  The requested line node does not exist
        //  Create it

        if (lineNode == null) {
            lineNode = getLastLineNode();
            int actualLine = lineCount.getAcquire() - 1;

            while (actualLine < line) {
                createLineNode(lineNode);
                lineNode = lineNode.getNext();
                actualLine++;
            }
            lineCount.setRelease(actualLine + 1);
        }
        return lineNode.insert(column, character);
    }

    /**
     * Modify a character at the given position
     *
     * @param line          Coordinate of the line, starts at 0
     * @param column        Coordinate of the column, starts at 0
     * @param character     Character to insert
     * @return          True if the character has been modified, false otherwise
     */
    public synchronized boolean modify(int line, int column, char character) {
        if (line < 0 || column < 0) return false;

        LineNode lineNode = getLineNode(line);

        //  The requested line node does not exist
        if (lineNode == null) {
            return false;
        }
        return lineNode.modify(column, character);
    }

    /**
     * Delete a character at the given position
     *
     * @param line          Coordinate of the line, starts at 0
     * @param column        Coordinate of the column, starts at 0
     * @return          True if the character has been deleted, false otherwise
     */
    public synchronized boolean delete(int line, int column) {
        if (line < 0 || column < 0) return false;

        LineNode lineNode = getLineNode(line);

        //  The requested line node does not exist
        //  Nothing to remove

        if (lineNode == null) return false;

        return lineNode.delete(column);
    }

    /**
     * Delete a line break between two lines, the selected line is merged with the next line
     *
     * @param line        Coordinate of the line, starts at 0
     * @return      True if the line has been removed, false otherwise
     */
    public synchronized boolean deleteLineBreak(int line) {
        if (line < 0) return false;

        LineNode lineNode = getLineNode(line);

        //  The requested line node does not exist
        //  Nothing to remove
        if (lineNode == null) return false;

        boolean result = lineNode.deleteLineBreak();
        if (result) lineCount.decrementAndGet();

        return result;
    }

    /**
     * Turns the document into a string
     * Each line is separated by a line break
     *
     * @return    The document as a string
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        LineNode lineNode = content;
        while (lineNode != null) {
            sb.append(lineNode);
            lineNode = lineNode.getNext();
        }

        // Remove the last line break
        if (!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
