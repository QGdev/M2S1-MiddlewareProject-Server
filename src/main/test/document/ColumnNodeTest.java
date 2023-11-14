package document;

import fr.univnantes.document.ColumnNode;
import fr.univnantes.document.LineNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColumnNodeTest {

    @Test
    public void testClear() {
        ColumnNode columnNode = new ColumnNode();
        columnNode.clear();
        assertNull(columnNode.getNext());
        assertNull(columnNode.getPrevious());
        assertNull(columnNode.getParent());
        assertEquals('\0', columnNode.getCharacter());
    }

    @Test
    public void testSetNext() {
        ColumnNode columnNode1 = new ColumnNode();
        ColumnNode columnNode2 = new ColumnNode();
        columnNode1.setNext(columnNode2);
        assertEquals(columnNode2, columnNode1.getNext());
    }

    @Test
    public void testSetPrevious() {
        ColumnNode columnNode1 = new ColumnNode();
        ColumnNode columnNode2 = new ColumnNode();
        columnNode1.setPrevious(columnNode2);
        assertEquals(columnNode2, columnNode1.getPrevious());
    }

    @Test
    public void testSetParent() {
        ColumnNode columnNode = new ColumnNode();
        LineNode lineNode = new LineNode();
        columnNode.setParent(lineNode);
        assertEquals(lineNode, columnNode.getParent());
    }

    @Test
    public void testSetCharacter() {
        ColumnNode columnNode = new ColumnNode();
        columnNode.setCharacter('a');
        assertEquals('a', columnNode.getCharacter());
    }
}
