package editor.workspace;

import editor.editor.Editor;
import editor.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作区测试
 */
public class WorkspaceTest {
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
    }

    @Test
    void testLoadFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "Hello World".getBytes());

        workspace.loadFile(testFile.toString());
        Editor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertEquals("Hello World", editor.getContent());
    }

    @Test
    void testLoadNonExistentFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("new.txt");

        workspace.loadFile(testFile.toString());
        Editor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertTrue(editor.isModified());
    }

    @Test
    void testInitFile() throws IOException {
        workspace.initFile("test.txt", false);
        Editor editor = workspace.getEditor("test.txt");
        assertNotNull(editor);
        assertTrue(editor.isModified());
    }

    @Test
    void testInitFileWithLog() throws IOException {
        workspace.initFile("test.txt", true);
        Editor editor = workspace.getEditor("test.txt");
        assertNotNull(editor);
        assertEquals("# log", editor.getLines().get(0));
    }

    @Test
    void testSetActiveFile() throws IOException {
        workspace.initFile("file1.txt", false);
        workspace.initFile("file2.txt", false);
        
        workspace.setActiveFile("file1.txt");
        assertEquals("file1.txt", workspace.getActiveFilePath());
        
        workspace.setActiveFile("file2.txt");
        assertEquals("file2.txt", workspace.getActiveFilePath());
    }

    @Test
    void testSaveFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        workspace.initFile(testFile.toString(), false);
        
        Editor editor = workspace.getEditor(testFile.toString());
        editor.append("Hello");
        
        workspace.saveFile(testFile.toString());
        assertFalse(editor.isModified());
        
        String content = new String(Files.readAllBytes(testFile));
        assertEquals("Hello", content);
    }

    @Test
    void testCloseFile() throws IOException {
        workspace.initFile("test.txt", false);
        workspace.forceCloseFile("test.txt");
        
        assertNull(workspace.getEditor("test.txt"));
    }

    @Test
    void testFileList() throws IOException {
        workspace.initFile("file1.txt", false);
        workspace.initFile("file2.txt", false);
        
        var fileList = workspace.getFileList();
        assertEquals(2, fileList.size());
    }

    @Test
    void testAutoEnableLog(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "# log\nHello".getBytes());

        workspace.loadFile(testFile.toString());
        // 日志应该自动启用（通过Logger.shouldAutoEnableLog检查）
        assertTrue(Logger.shouldAutoEnableLog("# log\nHello"));
    }
}

