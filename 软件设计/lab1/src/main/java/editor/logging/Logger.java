package editor.logging;

import editor.event.CommandEvent;
import editor.event.EventListener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志记录器（观察者模式）
 * 监听命令执行事件并记录到日志文件
 */
public class Logger implements EventListener {
    private final String filePath;
    private final String logFilePath;
    private boolean enabled;
    private boolean sessionStarted;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    public Logger(String filePath) {
        this.filePath = filePath;
        this.logFilePath = getLogFilePath(filePath);
        this.enabled = false;
        this.sessionStarted = false;
    }

    /**
     * 获取日志文件路径
     */
    private String getLogFilePath(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        Path parent = path.getParent();
        if (parent != null) {
            return parent.resolve("." + fileName + ".log").toString();
        } else {
            return "." + fileName + ".log";
        }
    }

    /**
     * 启用日志
     */
    public void enable() {
        this.enabled = true;
        this.sessionStarted = false;
    }

    /**
     * 禁用日志
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onEvent(CommandEvent event) {
        if (!enabled) {
            return;
        }

        try {
            // 记录会话开始
            if (!sessionStarted) {
                writeSessionStart();
                sessionStarted = true;
            }

            // 记录命令
            writeCommand(event);
        } catch (IOException e) {
            System.err.println("警告: 日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 写入会话开始标记
     */
    private void writeSessionStart() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(TIMESTAMP_FORMATTER);
        String line = "session start at " + timestamp + "\n";
        appendToFile(line);
    }

    /**
     * 写入命令记录
     */
    private void writeCommand(CommandEvent event) throws IOException {
        String timestamp = event.getFormattedTimestamp();
        String line = timestamp + " " + event.getCommand() + "\n";
        appendToFile(line);
    }

    /**
     * 追加内容到日志文件
     */
    private void appendToFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(content);
        }
    }

    /**
     * 读取日志内容
     */
    public String readLog() {
        try {
            Path path = Paths.get(logFilePath);
            if (!Files.exists(path)) {
                return "";
            }
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "读取日志失败: " + e.getMessage();
        }
    }

    /**
     * 检查文件首行是否为 # log
     */
    public static boolean shouldAutoEnableLog(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String firstLine = content.split("\n", 2)[0].trim();
        return "# log".equals(firstLine);
    }
}

