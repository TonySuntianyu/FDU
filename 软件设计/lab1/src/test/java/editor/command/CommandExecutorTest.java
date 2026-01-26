package editor.command;

import editor.editor.Editor;
import editor.workspace.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 命令执行器测试
 */
public class CommandExecutorTest {
    private Workspace workspace;
    private CommandExecutor executor;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        executor = new CommandExecutor(workspace);
    }

    @Test
    void testExecuteLoad(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "Hello World".getBytes());

        CommandParser.ParsedCommand cmd = CommandParser.parse("load " + testFile.toString());
        assertTrue(executor.execute(cmd));
        
        Editor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertEquals("Hello World", editor.getContent());
    }

    @Test
    void testExecuteSave(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        workspace.initFile(testFile.toString(), false);
        
        Editor editor = workspace.getEditor(testFile.toString());
        editor.append("Test content");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("save");
        executor.execute(cmd);
        
        assertFalse(editor.isModified());
        String content = new String(Files.readAllBytes(testFile));
        assertEquals("Test content", content);
    }

    @Test
    void testExecuteSaveAll(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        
        workspace.initFile(file1.toString(), false);
        workspace.initFile(file2.toString(), false);
        
        workspace.getEditor(file1.toString()).append("Content 1");
        workspace.getEditor(file2.toString()).append("Content 2");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("save all");
        executor.execute(cmd);
        
        assertEquals("Content 1", new String(Files.readAllBytes(file1)));
        assertEquals("Content 2", new String(Files.readAllBytes(file2)));
    }

    @Test
    void testExecuteInit() throws IOException {
        CommandParser.ParsedCommand cmd = CommandParser.parse("init test.txt");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertNotNull(editor);
        assertTrue(editor.isModified());
    }

    @Test
    void testExecuteInitWithLog() throws IOException {
        CommandParser.ParsedCommand cmd = CommandParser.parse("init test.txt with-log");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertNotNull(editor);
        assertEquals("# log", editor.getLines().get(0));
    }

    @Test
    void testExecuteAppend() throws IOException {
        workspace.initFile("test.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("append \"Hello\"");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertEquals("Hello", editor.getLines().get(0));
    }

    @Test
    void testExecuteInsert() throws IOException {
        workspace.initFile("test.txt", false);
        workspace.getEditor("test.txt").append("Hello");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("insert 1:6 \" World\"");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertEquals("Hello World", editor.getLines().get(0));
    }

    @Test
    void testExecuteDelete() throws IOException {
        workspace.initFile("test.txt", false);
        workspace.getEditor("test.txt").append("Hello World");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("delete 1:7 5");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertEquals("Hello ", editor.getLines().get(0));
    }

    @Test
    void testExecuteReplace() throws IOException {
        workspace.initFile("test.txt", false);
        workspace.getEditor("test.txt").append("fast fox");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("replace 1:1 4 \"slow\"");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertEquals("slow fox", editor.getLines().get(0));
    }

    @Test
    void testExecuteShow() throws IOException {
        workspace.initFile("test.txt", false);
        Editor editor = workspace.getEditor("test.txt");
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("show 1:2");
        executor.execute(cmd);
        
        // show命令会输出到控制台，这里主要验证命令能正常执行
        assertNotNull(editor);
    }

    @Test
    void testExecuteUndoRedo() throws IOException {
        workspace.initFile("test.txt", false);
        workspace.getEditor("test.txt").append("Line 1");
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("append \"Line 2\"");
        executor.execute(cmd);
        
        Editor editor = workspace.getEditor("test.txt");
        assertEquals(2, editor.getLines().size());
        
        CommandParser.ParsedCommand undoCmd = CommandParser.parse("undo");
        executor.execute(undoCmd);
        assertEquals(1, editor.getLines().size());
        
        CommandParser.ParsedCommand redoCmd = CommandParser.parse("redo");
        executor.execute(redoCmd);
        assertEquals(2, editor.getLines().size());
    }

    @Test
    void testExecuteLogOn() throws IOException {
        workspace.initFile("test.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("log-on");
        executor.execute(cmd);
        
        // 验证日志已启用（通过检查日志器状态）
        // 注意：这里主要验证命令能正常执行，具体日志功能由LoggerTest测试
    }

    @Test
    void testExecuteLogOff() throws IOException {
        workspace.initFile("test.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("log-off");
        executor.execute(cmd);
        
        // 验证日志已关闭
    }

    @Test
    void testExecuteLogShow() throws IOException {
        workspace.initFile("test.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("log-show");
        executor.execute(cmd);
        
        // 验证日志显示命令能正常执行
    }

    @Test
    void testExecuteEdit() throws IOException {
        workspace.initFile("file1.txt", false);
        workspace.initFile("file2.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("edit file2.txt");
        executor.execute(cmd);
        
        assertEquals("file2.txt", workspace.getActiveFilePath());
    }

    @Test
    void testExecuteEditorList() throws IOException {
        workspace.initFile("file1.txt", false);
        workspace.initFile("file2.txt", false);
        
        CommandParser.ParsedCommand cmd = CommandParser.parse("editor-list");
        executor.execute(cmd);
        
        // 验证命令能正常执行
        assertEquals(2, workspace.getFileList().size());
    }

    @Test
    void testErrorHandling() {
        // 测试未知命令
        CommandParser.ParsedCommand cmd = CommandParser.parse("unknown command");
        assertTrue(executor.execute(cmd));
        
        // 测试没有活动文件时的操作
        CommandParser.ParsedCommand appendCmd = CommandParser.parse("append \"test\"");
        executor.execute(appendCmd);
        // 应该输出错误信息，但不抛出异常
    }

    @Test
    void testExecuteUnknownCommand() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("invalid-command");
        // 未知命令应该返回null，executor应该处理
        assertTrue(executor.execute(cmd));
    }
}

