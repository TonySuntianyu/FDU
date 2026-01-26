package editor.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 命令解析器测试
 */
public class CommandParserTest {
    @Test
    void testParseLoad() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("load test.txt");
        assertNotNull(cmd);
        assertEquals("load", cmd.getCommandName());
        assertEquals("test.txt", cmd.getArg(0));
    }

    @Test
    void testParseAppend() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("append \"Hello World\"");
        assertNotNull(cmd);
        assertEquals("append", cmd.getCommandName());
        assertEquals("Hello World", cmd.getArg(0));
    }

    @Test
    void testParseInsert() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("insert 1:5 \"test\"");
        assertNotNull(cmd);
        assertEquals("insert", cmd.getCommandName());
        assertEquals("1", cmd.getArg(0));
        assertEquals("5", cmd.getArg(1));
        assertEquals("test", cmd.getArg(2));
    }

    @Test
    void testParseDelete() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("delete 1:5 10");
        assertNotNull(cmd);
        assertEquals("delete", cmd.getCommandName());
        assertEquals("1", cmd.getArg(0));
        assertEquals("5", cmd.getArg(1));
        assertEquals("10", cmd.getArg(2));
    }

    @Test
    void testParseReplace() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("replace 1:1 4 \"slow\"");
        assertNotNull(cmd);
        assertEquals("replace", cmd.getCommandName());
        assertEquals("1", cmd.getArg(0));
        assertEquals("1", cmd.getArg(1));
        assertEquals("4", cmd.getArg(2));
        assertEquals("slow", cmd.getArg(3));
    }

    @Test
    void testParseShow() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("show 1:10");
        assertNotNull(cmd);
        assertEquals("show", cmd.getCommandName());
        assertEquals("1", cmd.getArg(0));
        assertEquals("10", cmd.getArg(1));
    }

    @Test
    void testParseInitWithLog() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("init test.txt with-log");
        assertNotNull(cmd);
        assertEquals("init", cmd.getCommandName());
        assertEquals("test.txt", cmd.getArg(0));
        assertEquals("with-log", cmd.getArg(1));
    }

    @Test
    void testParseSaveAll() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("save all");
        assertNotNull(cmd);
        assertEquals("save", cmd.getCommandName());
        assertEquals("all", cmd.getArg(0));
    }

    @Test
    void testParseUnknownCommand() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("unknown command");
        assertNull(cmd);
    }

    @Test
    void testParseEmptyCommand() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("");
        assertNull(cmd);
    }

    @Test
    void testParseEscapeSequences() {
        // 测试换行符转义
        CommandParser.ParsedCommand cmd1 = CommandParser.parse("append \"Line1\\nLine2\"");
        assertNotNull(cmd1);
        String text1 = cmd1.getArg(0);
        assertTrue(text1.contains("\n"), "应该包含换行符");
        assertEquals("Line1\nLine2", text1);

        // 测试制表符转义
        CommandParser.ParsedCommand cmd2 = CommandParser.parse("append \"a\\tb\"");
        assertNotNull(cmd2);
        String text2 = cmd2.getArg(0);
        assertTrue(text2.contains("\t"), "应该包含制表符");
        assertEquals("a\tb", text2);

        // 测试反斜杠转义
        CommandParser.ParsedCommand cmd3 = CommandParser.parse("append \"a\\\\b\"");
        assertNotNull(cmd3);
        String text3 = cmd3.getArg(0);
        assertEquals("a\\b", text3);

        // 测试引号转义
        CommandParser.ParsedCommand cmd4 = CommandParser.parse("append \"a\\\"b\"");
        assertNotNull(cmd4);
        String text4 = cmd4.getArg(0);
        assertEquals("a\"b", text4);
    }

    @Test
    void testParseInsertWithNewline() {
        CommandParser.ParsedCommand cmd = CommandParser.parse("insert 1:2 \"\\n\"");
        assertNotNull(cmd);
        assertEquals("insert", cmd.getCommandName());
        String text = cmd.getArg(2);
        assertEquals("\n", text, "应该解析为单个换行符");
        assertEquals(1, text.length(), "应该是单个字符");
    }
}

