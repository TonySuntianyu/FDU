package editor.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 换行符处理测试
 */
public class TextEditorNewlineTest {
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor();
    }

    @Test
    void testInsertNewlineAtEnd() {
        editor.append("Hello");
        editor.insert(1, 6, "\nWorld");
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("World", editor.getLines().get(1));
    }

    @Test
    void testInsertNewlineInMiddle() {
        editor.append("HelloWorld");
        editor.insert(1, 6, "\n");
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("World", editor.getLines().get(1));
    }

    @Test
    void testInsertMultipleNewlines() {
        editor.append("abc");
        editor.insert(1, 2, "\n1\n2\n");
        assertEquals(4, editor.getLines().size());
        assertEquals("a", editor.getLines().get(0));
        assertEquals("1", editor.getLines().get(1));
        assertEquals("2", editor.getLines().get(2));
        assertEquals("bc", editor.getLines().get(3));
    }

    @Test
    void testInsertNewlineAtStart() {
        editor.append("World");
        editor.insert(1, 1, "Hello\n");
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("World", editor.getLines().get(1));
    }

    @Test
    void testInsertNewlineInEmptyFile() {
        editor.insert(1, 1, "Line1\nLine2");
        assertEquals(2, editor.getLines().size());
        assertEquals("Line1", editor.getLines().get(0));
        assertEquals("Line2", editor.getLines().get(1));
    }

    @Test
    void testInsertNewlineOnly() {
        editor.append("ab");
        editor.insert(1, 2, "\n");
        assertEquals(2, editor.getLines().size());
        assertEquals("a", editor.getLines().get(0));
        assertEquals("b", editor.getLines().get(1));
    }

    @Test
    void testInsertTextWithNewlineAtEnd() {
        editor.append("Hello");
        editor.insert(1, 6, " World\nNext Line");
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello World", editor.getLines().get(0));
        assertEquals("Next Line", editor.getLines().get(1));
    }

    @Test
    void testInsertNewlineWithEmptyParts() {
        editor.append("ab");
        editor.insert(1, 2, "\n\n");
        assertEquals(3, editor.getLines().size());
        assertEquals("a", editor.getLines().get(0));
        assertEquals("", editor.getLines().get(1));
        assertEquals("b", editor.getLines().get(2));
    }
}

