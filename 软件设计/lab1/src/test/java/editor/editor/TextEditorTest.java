package editor.editor;

import editor.editor.command.AppendCommand;
import editor.editor.command.DeleteCommand;
import editor.editor.command.InsertCommand;
import editor.editor.command.ReplaceCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本编辑器测试
 */
public class TextEditorTest {
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor();
    }

    @Test
    void testAppend() {
        editor.append("Hello");
        assertEquals(1, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertTrue(editor.isModified());
    }

    @Test
    void testInsert() {
        editor.append("Hello");
        editor.insert(1, 6, " World");
        assertEquals("Hello World", editor.getLines().get(0));
    }

    @Test
    void testInsertWithNewline() {
        editor.append("Hello");
        editor.insert(1, 6, "\nWorld");
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("World", editor.getLines().get(1));
    }

    @Test
    void testDelete() {
        editor.append("Hello World");
        editor.delete(1, 7, 5);
        assertEquals("Hello ", editor.getLines().get(0));
    }

    @Test
    void testReplace() {
        editor.append("fast fox");
        editor.replace(1, 1, 4, "slow");
        assertEquals("slow fox", editor.getLines().get(0));
    }

    @Test
    void testShow() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        
        String result = editor.show(1, 2);
        assertTrue(result.contains("1: Line 1"));
        assertTrue(result.contains("2: Line 2"));
    }

    @Test
    void testLoad() {
        String content = "Line 1\nLine 2\nLine 3";
        editor.load(content);
        assertEquals(3, editor.getLines().size());
        assertFalse(editor.isModified());
    }

    @Test
    void testUndoRedo() {
        editor.append("Line 1");
        editor.append("Line 2");
        
        assertTrue(editor.canUndo());
        editor.undo();
        assertEquals(1, editor.getLines().size());
        
        assertTrue(editor.canRedo());
        editor.redo();
        assertEquals(2, editor.getLines().size());
    }

    @Test
    void testCommandPattern() {
        editor.append("Hello");
        editor.clearModified();
        
        AppendCommand cmd = new AppendCommand(editor, " World");
        editor.executeCommand(cmd);
        assertEquals("Hello World", editor.getLines().get(0));
        
        editor.undo();
        assertEquals("Hello", editor.getLines().get(0));
    }

    @Test
    void testInsertBoundary() {
        editor.append("Hello");
        
        // 测试边界情况
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insert(2, 1, "test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insert(1, 10, "test");
        });
    }

    @Test
    void testDeleteBoundary() {
        editor.append("Hello");
        
        assertThrows(IllegalArgumentException.class, () -> {
            editor.delete(1, 1, 10);
        });
    }
}

