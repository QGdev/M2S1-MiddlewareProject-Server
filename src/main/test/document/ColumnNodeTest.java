package document;

import fr.univnantes.document.ColumnNode;
import fr.univnantes.document.LineNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColumnNodeTest {
    /**
     * Test the clear() method of ColumnNode by verifying that all attributes
     * are set to null or the default value after calling clear().
     * <p>
     * This test creates a ColumnNode, calls clear(), and asserts that the next,
     * previous, parent, and character attributes are all set to null or the default value.
     * </p>
     */
    @Test
    public void testClear() {
        ColumnNode columnNode = new ColumnNode();
        columnNode.clear();
        assertNull(columnNode.getNext());
        assertNull(columnNode.getPrevious());
        assertNull(columnNode.getParent());
        assertEquals('\0', columnNode.getCharacter());
    }

    /**
     * Test setting the next node for a ColumnNode.
     * <p>
     * This test creates two ColumnNode instances, sets the second node as the next
     * node for the first, and asserts that the next node is correctly set.
     * </p>
     */
    @Test
    public void testSetNext() {
        ColumnNode columnNode1 = new ColumnNode();
        ColumnNode columnNode2 = new ColumnNode();
        columnNode1.setNext(columnNode2);
        assertEquals(columnNode2, columnNode1.getNext());
    }

    /**
     * Test setting the previous node for a ColumnNode.
     * <p>
     * This test creates two ColumnNode instances, sets the second node as the previous
     * node for the first, and asserts that the previous node is correctly set.
     * </p>
     */
    @Test
    public void testSetPrevious() {
        ColumnNode columnNode1 = new ColumnNode();
        ColumnNode columnNode2 = new ColumnNode();
        columnNode1.setPrevious(columnNode2);
        assertEquals(columnNode2, columnNode1.getPrevious());
    }

    /**
     * Test setting the parent node for a ColumnNode.
     * <p>
     * This test creates a ColumnNode and a LineNode, sets the LineNode as the parent
     * for the ColumnNode, and asserts that the parent node is correctly set.
     * </p>
     */
    @Test
    public void testSetParent() {
        ColumnNode columnNode = new ColumnNode();
        LineNode lineNode = new LineNode();
        columnNode.setParent(lineNode);
        assertEquals(lineNode, columnNode.getParent());
    }

    /**
     * Test setting the character for a ColumnNode.
     * <p>
     * This test creates a ColumnNode, sets a character 'a', and asserts that
     * the character is correctly set.
     * </p>
     */
    @Test
    public void testSetCharacter() {
        ColumnNode columnNode = new ColumnNode();
        columnNode.setCharacter('a');
        assertEquals('a', columnNode.getCharacter());
    }

}
