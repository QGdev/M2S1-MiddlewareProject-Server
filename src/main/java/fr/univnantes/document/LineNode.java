package fr.univnantes.document;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a line node
 * <p>
 * A line node is a node of a document
 * It contains a reference to the next and previous line node
 * It also contains a reference to the first column node of the line
 * It is used to represent a line in a document
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
     * @return the column node if it exists, null otherwise
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
     * @return true if the character has been inserted, false otherwise
     */
    public boolean insert(int index, char character) {
        if (index < 0) return false;

        synchronized (this) {
            ColumnNode firstColumnNode = getContent();
            ColumnNode referenceColumnNode = getColumnNodeAtIndex(index);
            ColumnNode newColumnNode = new ColumnNode();
            newColumnNode.setCharacter(character);
            newColumnNode.setParent(this);

            //  If the index is less than the number of column nodes
            if (referenceColumnNode != null) {
                ColumnNode previousNode = referenceColumnNode.getPrevious();

                //  If the column node is the first column node
                if (previousNode == null) {
                    //  If the line is empty
                    //  Set the content of the line to the new column node
                    if (firstColumnNode == null) {
                        setContent(newColumnNode);
                        return true;
                    }

                    //  If the line is not empty
                    //  Link the new column node to the first column node
                    firstColumnNode.setPrevious(newColumnNode);
                    newColumnNode.setNext(firstColumnNode);
                    setContent(newColumnNode);
                    return true;
                }

                //  If the column node is in the middle
                //  Link the previous column node to the new column node
                previousNode.setNext(newColumnNode);
                newColumnNode.setPrevious(previousNode);

                //  Link the new column node to the reference column node
                newColumnNode.setNext(referenceColumnNode);
                referenceColumnNode.setPrevious(newColumnNode);
                return true;
            }

            //  If the index is greater than the number of column nodes
            //  Create new blank column nodes to fill the gap
            int numberOfColumnNodes = getNumberOfColumnNodes();
            ColumnNode lastColumnNode = getLastColumnNode();

            //  If the line is empty
            if (firstColumnNode == null) {
                //  Fill the line with blank column nodes
                while (numberOfColumnNodes < index) {
                    ColumnNode blankColumnNode = new ColumnNode();
                    blankColumnNode.setParent(this);
                    blankColumnNode.setCharacter(' ');

                    //  If the line is empty
                    //  Set the content of the line to the blank column node
                    if (lastColumnNode == null) {
                        setContent(blankColumnNode);
                    }
                    //  If the line is not empty
                    //  Link the last column node to the blank column node
                    else {
                        lastColumnNode.setNext(blankColumnNode);
                        blankColumnNode.setPrevious(lastColumnNode);
                    }

                    lastColumnNode = blankColumnNode;
                    numberOfColumnNodes++;
                }

                //  If the line is empty
                //  Set the content of the line to the new column node
                if (lastColumnNode == null) {
                    setContent(newColumnNode);
                    return true;
                }

                //  If the line is not empty
                //  Link the last column node to the new column node
                lastColumnNode.setNext(newColumnNode);
                newColumnNode.setPrevious(lastColumnNode);
                return true;
            }

            //  If the line is not empty
            //  Fill the line with blank column nodes
            while (numberOfColumnNodes < index) {
                ColumnNode blankColumnNode = new ColumnNode();
                blankColumnNode.setParent(this);
                blankColumnNode.setCharacter(' ');

                //  Link the last column node to the blank column node
                lastColumnNode.setNext(blankColumnNode);
                blankColumnNode.setPrevious(lastColumnNode);

                lastColumnNode = blankColumnNode;
                numberOfColumnNodes++;
            }

            //  Link the last column node to the new column node
            lastColumnNode.setNext(newColumnNode);
            newColumnNode.setPrevious(lastColumnNode);
            return true;
        }
    }

    /**
     * Modifies the character at the given index in the line, the index starts at 0
     *
     * @param index     the index of the character to modify, the index starts at 0
     * @param character the new character
     * @return true if the character has been modified, false otherwise
     */
    public boolean modify(int index, char character) {
        if (index < 0) return false;

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
     *
     * @param index the index of the character to delete, the index starts at 0
     * @return true if the character has been deleted, false otherwise
     */
    public boolean delete(int index) {
        if (index < 0) return false;

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
     *
     * @return true if the line break has been deleted, false otherwise
     */
    public boolean deleteLineBreak() {
        synchronized (this) {
            LineNode previousLineNode = getPrevious();
            LineNode nextLineNode = getNext();
            ColumnNode actualContent = getContent();
            //  If the line is empty
            if (previousLineNode == null) return false;

            //  Lock on the previous line node
            synchronized (previousLineNode) {

                if (nextLineNode != null) {
                    //  Detach the current line node from the document
                    //  Link the previous line node to the next line node
                    if (actualContent == null) {
                        synchronized (nextLineNode) {
                            previousLineNode.setNext(nextLineNode);
                            nextLineNode.setPrevious(previousLineNode);

                            return true;
                        }
                    }

                    //  The content of the current line node will be moved to the previous line node
                    //  Link the previous line node to the next line node
                    synchronized (nextLineNode) {
                        previousLineNode.setNext(nextLineNode);
                        nextLineNode.setPrevious(previousLineNode);
                    }

                    //  Merge the content of the current line node with the content of the previous line node
                    ColumnNode lastColumnNode = previousLineNode.getLastColumnNode();

                    if (lastColumnNode == null) {
                        previousLineNode.setContent(actualContent);
                    } else {
                        lastColumnNode.setNext(actualContent);
                    }

                    //  Set parent of other column nodes
                    ColumnNode currentColumnNode = actualContent;
                    while (currentColumnNode != null) {
                        currentColumnNode.setParent(previousLineNode);
                        currentColumnNode = currentColumnNode.getNext();
                    }
                    return true;
                }

                //  If the line is the last line of the document
                //  Detach the current line node from the document
                previousLineNode.setNext(null);

                if (actualContent == null) return true;

                //  The content of the current line node will be moved to the previous line node
                //  Merge the content of the current line node with the content of the previous line node
                ColumnNode lastColumnNode = previousLineNode.getLastColumnNode();

                if (lastColumnNode == null) {
                    previousLineNode.setContent(actualContent);
                } else {
                    lastColumnNode.setNext(actualContent);
                }

                //  Set parent of other column nodes
                ColumnNode currentColumnNode = actualContent;
                while (currentColumnNode != null) {
                    currentColumnNode.setParent(previousLineNode);
                    currentColumnNode = currentColumnNode.getNext();
                }

                return true;
            }
        }
    }

    /**
     * Inserts a line break at the given index in the line, the index starts at 0
     * Will split the line in two, the first part will contain the characters before the line break
     * The second part will contain the characters after the line break
     *
     * @param column the index of the line break, the index starts at 0
     * @return true if the line break has been inserted, false otherwise
     */
    public boolean insertLineBreak(int column) {
        //  Check if the index is valid
        //  Cannot be negative
        if (column < 0) return false;

        synchronized (this) {
            //  Get the column node at the given index
            ColumnNode columnNode = getColumnNodeAtIndex(column);

            //  If the column node does not exist
            //  Just create a new line node
            if (columnNode == null) {
                LineNode newLineNode = new LineNode();
                LineNode nextLineNode = getNext();

                //  If the next line node does not exist
                //  Just link the new line node to the current line node
                if (nextLineNode == null) {
                    setNext(newLineNode);
                    newLineNode.setPrevious(this);
                    return true;
                }

                //  If the next line node exists
                //  Lock on the next line node
                synchronized (nextLineNode) {
                    //  Attach the new line node to the document
                    //  Link the current line node to the new line node to the next line node
                    setNext(newLineNode);
                    newLineNode.setPrevious(this);
                    newLineNode.setNext(nextLineNode);
                    nextLineNode.setPrevious(newLineNode);

                    return true;
                }
            }

            //  If the column node exists
            //  Split the line in two
            //  But first, check if the column node is the last column node
            LineNode nextLineNode = getNext();
            LineNode newLineNode = new LineNode();

            //  Set the second part of the line into the new line node
            newLineNode.setContent(columnNode);
            if (columnNode.getNext() != null)   columnNode.getNext().setPrevious(null);
            columnNode.setNext(null);

            //  Set parents of the column nodes
            ColumnNode currentColumnNode = newLineNode.getContent();
            while (currentColumnNode != null) {
                currentColumnNode.setParent(newLineNode);
                currentColumnNode = currentColumnNode.getNext();
            }

            //  If the column node is the last column node
            if (nextLineNode == null) {
                setNext(newLineNode);
                newLineNode.setPrevious(this);
                return true;
            }

            //  If the column node is not the last column node
            //  Lock on the next line node
            synchronized (nextLineNode) {
                //  Attach the new line node to the document
                //  Link the current line node to the new line node to the next line node
                setNext(newLineNode);
                newLineNode.setPrevious(this);
                newLineNode.setNext(nextLineNode);
                nextLineNode.setPrevious(newLineNode);

                return true;
            }
        }
    }

    /**
     * Returns the number of column nodes in the line
     *
     * @return The number of column nodes in the line
     */
    private int getNumberOfColumnNodes() {
        ColumnNode columnNode = content.get();
        int numberOfColumnNodes = 0;

        while (columnNode != null) {
            numberOfColumnNodes++;
            columnNode = columnNode.getNext();
        }

        return numberOfColumnNodes;
    }

    /**
     * Converts the line node to a string representation
     *
     * @return the string representation of the line node
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
