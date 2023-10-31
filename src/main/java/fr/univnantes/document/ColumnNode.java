package fr.univnantes.document;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a column node
 * <p>
 *     A column node is a node of a line node
 *     It contains a character and a reference to the next and previous column node
 *     It also contains a reference to the parent line node
 *     It is used to represent a character in a document
 * </p>
 */
public class ColumnNode {
    private final AtomicReference<ColumnNode> next;
    private final AtomicReference<ColumnNode> previous;
    private final AtomicReference<LineNode> parent;
    private final AtomicReference<Character> character;

    /**
     * Create a new column node
     */
    public ColumnNode() {
        next = new AtomicReference<>(null);
        previous = new AtomicReference<>(null);
        parent = new AtomicReference<>(null);
        character = new AtomicReference<>(' ');
    }

    /**
     * Empties the column node
     */
    public void clear() {
        synchronized (this) {
            next.set(null);
            previous.set(null);
            parent.set(null);
            character.set('\0');
        }
    }

    /**
     * Returns the next column node
     *
     * @return the next column node
     */
    public ColumnNode getNext() {
        synchronized (this) {
            return next.get();
        }
    }

    /**
     * Returns the next column node and acquires the lock
     *
     * @return the next column node
     */
    public ColumnNode getNextAcquire() {
        synchronized (this) {
            return next.getAcquire();
        }
    }

    /**
     * Sets the next column node
     *
     * @param next the next column node
     */
    public void setNext(ColumnNode next) {
        synchronized (this) {
            this.next.set(next);
        }
    }

    /**
     * Sets the next column node and releases the lock
     *
     * @param next the next column node
     */
    public void setNextRelease(ColumnNode next) {
        synchronized (this) {
            this.next.setRelease(next);
        }
    }

    /**
     * Returns the previous column node
     *
     * @return the previous column node
     */
    public ColumnNode getPrevious() {
        synchronized (this) {
            return previous.get();
        }
    }

    /**
     * Returns the previous column node and acquires the lock
     *
     * @return the previous column node
     */
    public ColumnNode getPreviousAcquire() {
        synchronized (this) {
            return this.previous.getAcquire();
        }
    }

    /**
     * Sets the previous column node
     *
     * @param previous the previous column node
     */
    public void setPrevious(ColumnNode previous) {
        synchronized (this) {
            this.previous.set(previous);
        }
    }

    /**
     * Sets the previous column node and releases the lock
     *
     * @param previous the previous column node
     */
    public void setPreviousRelease(ColumnNode previous) {
        synchronized (this) {
            this.previous.setRelease(previous);
        }
    }

    /**
     * Returns the parent line node
     *
     * @return the parent line node
     */
    public LineNode getParent() {
        synchronized (this) {
            return parent.get();
        }
    }

    /**
     * Returns the parent line node and acquires the lock
     *
     * @return the parent line node
     */
    public LineNode getParentAcquire() {
        synchronized (this) {
            return this.parent.getAcquire();
        }
    }

    /**
     * Sets the parent line node
     *
     * @param parent the parent line node
     */
    public void setParent(LineNode parent) {
        synchronized (this) {
            this.parent.set(parent);
        }
    }

    /**
     * Sets the parent line node and releases the lock
     *
     * @param parent the parent line node
     */
    public void setParentRelease(LineNode parent) {
        synchronized (this) {
            this.parent.setRelease(parent);
        }
    }

    /**
     * Returns the character
     *
     * @return the character
     */
    public char getCharacter() {
        synchronized (this) {
            return character.get();
        }
    }

    /**
     * Returns the character and acquires the lock
     *
     * @return the character
     */
    public void setCharacter(char character) {
        synchronized (this) {
            this.character.set(character);
        }
    }
}
