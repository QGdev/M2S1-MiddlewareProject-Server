package fr.univnantes.document;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a line node
 * <p>
 *     A line node is a node of a document
 *     It contains a reference to the next and previous line node
 *     It also contains a reference to the first column node of the line
 *     It is used to represent a line in a document
 * </p>
 */
public class LineNode {
    private final AtomicReference<LineNode> next;
    private final AtomicReference<LineNode> previous;
    private final AtomicReference<ColumnNode> content;


    /**
     * Create a new line node
     */
    public LineNode() {
        next = new AtomicReference<>(null);
        previous = new AtomicReference<>(null);
        content = new AtomicReference<>(null);
    }

    /**
     * Empties the line node
     */
    public void clear() {
        synchronized (this) {
            next.set(null);
            previous.set(null);
            content.set(null);
        }
    }

    /**
     * Returns the next line node
     *
     * @return the next line node
     */
    public LineNode getNext() {
        return next.get();
    }

    /**
     * Returns the next line node and acquires the lock
     *
     * @return the next line node
     */
    public LineNode getNextAcquire() {
        return this.next.getAcquire();
    }

    /**
     * Sets the next line node
     *
     * @param next the next line node
     */
    public void setNext(LineNode next) {
        this.next.set(next);
    }

    /**
     * Sets the next line node and releases the lock
     *
     * @param next the next line node
     */
    public void setNextRelease(LineNode next) {
        this.next.setRelease(next);
    }

    /**
     * Returns the previous line node
     *
     * @return the previous line node
     */
    public LineNode getPrevious() {
        return previous.get();
    }

    /**
     * Returns the previous line node and acquires the lock
     *
     * @return the previous line node
     */
    public LineNode getPreviousAcquire() {
        return this.previous.getAcquire();
    }

    /**
     * Sets the previous line node
     *
     * @param previous the previous line node
     */
    public void setPrevious(LineNode previous) {
        this.previous.set(previous);
    }

    /**
     * Sets the previous line node and releases the lock
     *
     * @param previous the previous line node
     */
    public void setPreviousRelease(LineNode previous) {
        this.previous.setRelease(previous);
    }

    /**
     * Returns the first column node of the line
     *
     * @return the first column node of the line
     */
    public ColumnNode getContent() {
        return content.get();
    }

    /**
     * Returns the first column node of the line and acquires the lock
     *
     * @return the first column node of the line
     */
    public ColumnNode getContentAcquire() {
        return this.content.getAcquire();
    }

    /**
     * Sets the first column node of the line
     *
     * @param content the first column node of the line
     */
    public void setContent(ColumnNode content) {
        this.content.set(content);
    }

    /**
     * Sets the first column node of the line and releases the lock
     *
     * @param content the first column node of the line
     */
    public void setContentRelease(ColumnNode content) {
        this.content.setRelease(content);
    }

    /**
     * Returns the last column node of the line
     *
     * @return the last column node of the line
     */
    private ColumnNode getLastColumnNode() {
        ColumnNode columnNode = content.get();
        if (columnNode == null) return null;

        while (columnNode.getNext() != null) {
            columnNode = columnNode.getNext();
        }
        return columnNode;
    }

    /**
     * Returns the column node at the given index in the line, the index starts at 0
     *
     * @param index the index of the column node to return, the index starts at 0
     * @return    the column node if it exists, null otherwise
     */
    private ColumnNode getColumnNodeAtIndex(int index) {
        ColumnNode columnNode = content.get();
        int currentIndex = 0;

        if (columnNode == null) return null;

        while (currentIndex < index && columnNode.getNext() != null) {
            columnNode = columnNode.getNext();
            currentIndex++;
        }

        if (currentIndex == index) {
            return columnNode;
        }
        return null;
    }

    /**
     * Inserts a new character at the given index in the line, the index starts at 0
     *
     * @param index     the index of the character to insert, the index starts at 0
     *                  If the index is greater than the number of column nodes,
     *                  new column nodes will be created to fill the gap
     * @param character the character to insert
     * @return      true if the character has been inserted, false otherwise
     */
    public boolean insert(int index, char character) {
        if (index < 0)  return false;

        synchronized (this) {
            ColumnNode columnNode = content.get();

            //  If the line is empty
            if (columnNode == null) {
                columnNode = new ColumnNode();
                columnNode.setParent(this);
                content.set(columnNode);

                //  If the index is 0, we insert the character in the new column node
                if (index == 0) {
                    columnNode.setCharacter(character);
                    return true;
                }
            }

            //  If the line is not empty

            //  We will insert new nodes if needed
            int currentIndex = 0;
            while (currentIndex < index && columnNode.getNext() != null) {
                columnNode = columnNode.getNext();
                currentIndex++;
            }

            //  If the index is equal to the number of column nodes
            //  We will insert a new column node before the existing column node
            if (currentIndex == index) {
                ColumnNode newColumnNode = new ColumnNode();
                newColumnNode.setParent(this);
                newColumnNode.setNext(columnNode);
                newColumnNode.setPrevious(columnNode.getPrevious());
                columnNode.setPrevious(newColumnNode);

                //  If the column node is the first column node
                if (newColumnNode.getPrevious() == null) {
                    content.set(newColumnNode);
                }
                else {
                    newColumnNode.getPrevious().setNext(newColumnNode);
                }

                newColumnNode.setCharacter(character);
                return true;
            }

            //  If the index is greater than the number of column nodes
            //  We create new column nodes to fill the gap
            while (currentIndex < index) {
                ColumnNode newColumnNode = new ColumnNode();
                newColumnNode.setParent(this);
                columnNode.setNext(newColumnNode);
                newColumnNode.setPrevious(columnNode);
                columnNode = newColumnNode;
                currentIndex++;
            }

            //  If the index is equal to the number of column nodes
            if (currentIndex == index) {
                columnNode.setCharacter(character);
                return true;
            }

            return false;
        }
    }

    /**
     * Modifies the character at the given index in the line, the index starts at 0
     *
     * @param index     the index of the character to modify, the index starts at 0
     * @param character the new character
     * @return      true if the character has been modified, false otherwise
     */
    public boolean modify(int index, char character) {
        if (index < 0)  return false;

        synchronized (this) {
            ColumnNode columnNode = getColumnNodeAtIndex(index);

            if (columnNode != null) {
                columnNode.setCharacter(character);
                return true;
            }

            return false;
        }
    }

    /**
     * Deletes the character at the given index in the line, the index starts at 0
     * @param index    the index of the character to delete, the index starts at 0
     * @return    true if the character has been deleted, false otherwise
     */
    public boolean delete(int index) {
        if (index < 0)  return false;

        synchronized (this) {
            ColumnNode columnNode = getColumnNodeAtIndex(index);

            if (columnNode != null) {
                ColumnNode previousNode = columnNode.getPrevious();
                ColumnNode nextNode = columnNode.getNext();

                //  If the column node is the first column node
                if (previousNode == null) {
                    this.content.set(nextNode);
                    if (nextNode != null) {
                        nextNode.setPrevious(null);
                    }
                    return true;
                }

                //  If the column node is the last column node
                if (nextNode == null) {
                    previousNode.setNext(null);
                    return true;
                }

                //  If the column node is in the middle
                previousNode.setNext(nextNode);
                nextNode.setPrevious(previousNode);
                return true;
            }

            return false;
        }
    }

    /**
     * Deletes the line break at the end of the line
     * @return  true if the line break has been deleted, false otherwise
     */
    public boolean deleteLineBreak() {
        synchronized (this) {
            LineNode nextLineNode = this.getNext();

            if (nextLineNode == null) return false;

            LineNode newNextLineNode = nextLineNode.getNext();

            if (newNextLineNode != null) {
                newNextLineNode.setPrevious(this);
                setNext(newNextLineNode);
            }
            else {
                setNext(null);
            }

            //  Copy the content of the next line node at the end of the current line node
            ColumnNode lastCurrentColumnNode = getLastColumnNode();
            ColumnNode firstNextColumnNode = nextLineNode.getContent();

            ////    Set the parent of the next column nodes to the current line node
            ColumnNode currentColumnNode = firstNextColumnNode;
            while (currentColumnNode != null) {
                currentColumnNode.setParent(this);
                currentColumnNode = currentColumnNode.getNext();
            }

            ////    Set the previous and next column nodes of the first and last column nodes
            /////   The current line node is empty
            if (lastCurrentColumnNode == null) {
                content.set(firstNextColumnNode);
                return true;
            }
            //////  If the next line node is empty, we don't need to copy the content
            if (firstNextColumnNode == null || firstNextColumnNode.getCharacter() == '\0') {
                return true;
            }

            lastCurrentColumnNode.setNext(firstNextColumnNode);
            firstNextColumnNode.setPrevious(lastCurrentColumnNode);

            return true;
        }
    }

    /**
     * Inserts a line break at the given index in the line, the index starts at 0
     * Will split the line in two, the first part will contain the characters before the line break
     * The second part will contain the characters after the line break
     *
     * @param column    the index of the line break, the index starts at 0
     * @return      true if the line break has been inserted, false otherwise
     */
    public boolean insertLineBreak(int column) {
        synchronized (this) {

            //  Check if the index is valid
            //  Cannot be negative
            if (column < 0) return false;
            //  The index should correspond to an existing column node
            ColumnNode columnNode = getColumnNodeAtIndex(column);
            if (columnNode == null) return false;

            //  Split the line in two
            //  The first part will contain the characters before the line break and remain in the current line node
            //  The second part will contain the characters after the line break and will be moved to a new line node

            ColumnNode secondPart = columnNode;
            if (columnNode.getPrevious() != null) {
                columnNode.getPrevious().setNext(null);
                secondPart.setPrevious(null);
            }
            else {
                content.set(null);
            }

            //  Create a new line node after the current one and link it in the document
            LineNode newLineNode = new LineNode();
            newLineNode.setContent(secondPart);
            //  Link every column node to the new line node
            ColumnNode currentColumnNode = secondPart;
            while (currentColumnNode != null) {
                currentColumnNode.setParent(newLineNode);
                currentColumnNode = currentColumnNode.getNext();
            }

            //  Link the new line node to the document
            newLineNode.setNext(this.getNext());
            newLineNode.setPrevious(this);

            //  Set link of the other nodes
            if (this.getNext() != null) {
                this.getNext().setPrevious(newLineNode);
            }
            this.setNext(newLineNode);

            return true;
        }
    }

    /**
     * Converts the line node to a string representation
     *
     * @return  the string representation of the line node
     */
    public String toString() {
        synchronized (this) {
            StringBuilder sb = new StringBuilder();
            ColumnNode columnNode = content.get();
            while (columnNode != null) {
                sb.append(columnNode.getCharacter());
                columnNode = columnNode.getNext();
            }
            sb.append('\n');

            return sb.toString();
        }
    }
}
