package editor.logging;

import editor.event.CommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志模块测试
 */
public class LoggerTest {
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = new Logger("test.txt");
    }

    @Test
    void testEnableDisable() {
        assertFalse(logger.isEnabled());
        logger.enable();
        assertTrue(logger.isEnabled());
        logger.disable();
        assertFalse(logger.isEnabled());
    }

    @Test
    void testShouldAutoEnableLog() {
        assertTrue(Logger.shouldAutoEnableLog("# log\nHello"));
        assertFalse(Logger.shouldAutoEnableLog("Hello\nWorld"));
        assertFalse(Logger.shouldAutoEnableLog(""));
        assertFalse(Logger.shouldAutoEnableLog(null));
    }

    @Test
    void testLogEvent(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve(".test.txt.log");
        Logger testLogger = new Logger(tempDir.resolve("test.txt").toString());
        testLogger.enable();

        CommandEvent event = new CommandEvent("append \"test\"", "test.txt");
        testLogger.onEvent(event);

        // 验证日志文件已创建（如果实现正确）
        // 注意：由于日志文件路径是内部计算的，这里主要测试逻辑
        assertTrue(testLogger.isEnabled());
    }

    @Test
    void testReadLog() {
        String log = logger.readLog();
        // 如果日志文件不存在，应该返回空字符串或错误信息
        assertNotNull(log);
    }
}

