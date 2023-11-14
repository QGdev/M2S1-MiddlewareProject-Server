package document;

import static org.junit.jupiter.api.Assertions.*;

import fr.univnantes.document.LineNode;
import org.junit.jupiter.api.Test;

public class LineNodeTest {

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

    @Test
    public void testModify() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.modify(0, 'B'));
        assertEquals("B\n", lineNode.toString());

        assertFalse(lineNode.modify(-1, 'C'));
        assertFalse(lineNode.modify(1, 'D'));
    }

    @Test
    public void testDelete() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.delete(0));
        assertEquals("\n", lineNode.toString());

        assertFalse(lineNode.delete(-1));
        assertFalse(lineNode.delete(0));
    }

    @Test
    public void testDeleteLineBreak() {
        LineNode lineNode1 = new LineNode();
        lineNode1.insert(0, 'A');
        LineNode lineNode2 = new LineNode();
        lineNode2.insert(0, 'B');
        lineNode1.setNext(lineNode2);

        assertTrue(lineNode1.deleteLineBreak());
        assertEquals("AB\n", lineNode1.toString());
    }

    @Test
    public void testInsertLineBreak() {
        LineNode lineNode = new LineNode();
        lineNode.insert(0, 'A');
        assertTrue(lineNode.insertLineBreak(0));

        assertFalse(lineNode.insertLineBreak(-1));
        assertFalse(lineNode.insertLineBreak(1));
    }


}
