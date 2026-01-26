package editor.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 命令执行事件
 */
public class CommandEvent {
    private final String command;
    private final LocalDateTime timestamp;
    private final String filePath;

    public CommandEvent(String command, String filePath) {
        this.command = command;
        this.filePath = filePath;
        this.timestamp = LocalDateTime.now();
    }

    public String getCommand() {
        return command;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * 获取格式化的时间戳
     * @return 格式化的时间戳字符串
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
    }
}

