package fr.univnantes.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LineNodeTest {
    /**
     * Test the insert() method of LineNode by verifying the correct insertion
     * of characters at specified positions within the line.
     * <p>
     * This test creates a LineNode, inserts characters at different positions,
     * and asserts that the resulting string representation is as expected.
     * </p>
     */
    @Test
    public void testInsert() {
        LineNode lineNode = new LineNode();
        assertTrue(lineNode.insert(0, 'A'));
        assertEquals("A\n", lineNode.toString());

        assertTrue(lineNode.insert(1, 'B'));
        assertEquals("AB\n", lineNode.toString());

        assertTrue(lineNode.insert(0, 'C'));
        assertEquals("CAB\n", lineNode.toString());

        assertFalse(lineNode.insert(-1, 'D'));
    }

    /**
     * Test the modify() method of LineNode by verifying the correct modification
     * of characters at specified positions within the line.
     * <p>
     * This test creates a LineNode, inserts a character, modifies it,
     * and asserts that the resulting string representation is as expected.
     * </p>
     */
    @Test
    public void testModify() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.modify(0, 'B'));
        assertEquals("B\n", lineNode.toString());

        assertFalse(lineNode.modify(-1, 'C'));
        assertFalse(lineNode.modify(1, 'D'));
    }

    /**
     * Test the delete() method of LineNode by verifying the correct deletion
     * of characters at specified positions within the line.
     * <p>
     * This test creates a LineNode, inserts a character, deletes it,
     * and asserts that the resulting string representation is as expected.
     * </p>
     */
    @Test
    public void testDelete() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.delete(0));
        assertEquals("\n", lineNode.toString());

        assertFalse(lineNode.delete(-1));
        assertFalse(lineNode.delete(0));
    }

    /**
     * Test the deleteLineBreak() method of LineNode by verifying the correct deletion
     * of the line break between two LineNode instances.
     * <p>
     * This test creates two LineNode instances, inserts characters, sets one as
     * the next node of the other, deletes the line break, and asserts the result.
     * </p>
     */
    @Test
    public void testDeleteLineBreak() {
        LineNode lineNode1 = new LineNode();
        lineNode1.insert(0, 'A');
        LineNode lineNode2 = new LineNode();
        lineNode2.insert(0, 'B');
        lineNode1.setNext(lineNode2);
        lineNode2.setPrevious(lineNode1);

        assertTrue(lineNode2.deleteLineBreak());
        assertEquals("AB\n", lineNode1.toString());
    }

    /**
     * Test the insertLineBreak() method of LineNode by verifying the correct insertion
     * of a line break at a specified position within the line.
     * <p>
     * This test creates a LineNode, inserts a character, inserts a line break,
     * and asserts that the resulting string representation is as expected.
     * </p>
     */
    @Test
    public void testInsertLineBreak() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.insertLineBreak(0));

        assertFalse(lineNode.insertLineBreak(-1));
        assertFalse(lineNode.insertLineBreak(1));
    }


}
