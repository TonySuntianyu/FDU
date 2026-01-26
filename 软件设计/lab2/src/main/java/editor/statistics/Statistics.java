package editor.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计模块
 * 记录每个文件在当前会话中的编辑时长
 */
public class Statistics {
    private final Map<String, FileStatistics> fileStats;
    private String activeFilePath;
    private long sessionStartTime;

    public Statistics() {
        this.fileStats = new HashMap<>();
        this.activeFilePath = null;
        this.sessionStartTime = 0;
    }

    /**
     * 文件切换时调用
     */
    public void onFileActivated(String filePath) {
        // 停止当前文件的计时
        if (activeFilePath != null) {
            stopTiming(activeFilePath);
        }

        // 开始新文件的计时
        activeFilePath = filePath;
        if (filePath != null) {
            startTiming(filePath);
        }
    }

    /**
     * 文件关闭时调用
     */
    public void onFileClosed(String filePath) {
        if (filePath.equals(activeFilePath)) {
            stopTiming(filePath);
            activeFilePath = null;
        }
        // 重置时长（文件关闭后再次打开时从0开始）
        fileStats.remove(filePath);
    }

    /**
     * 开始计时
     */
    private void startTiming(String filePath) {
        FileStatistics stats = fileStats.get(filePath);
        if (stats == null) {
            stats = new FileStatistics();
            fileStats.put(filePath, stats);
        }
        stats.startTiming();
        sessionStartTime = System.currentTimeMillis();
    }

    /**
     * 停止计时
     */
    private void stopTiming(String filePath) {
        FileStatistics stats = fileStats.get(filePath);
        if (stats != null && sessionStartTime > 0) {
            long elapsed = System.currentTimeMillis() - sessionStartTime;
            stats.addTime(elapsed);
            sessionStartTime = 0;
        }
    }

    /**
     * 获取文件的编辑时长（毫秒）
     */
    public long getEditTime(String filePath) {
        FileStatistics stats = fileStats.get(filePath);
        if (stats == null) {
            return 0;
        }

        // 如果文件正在编辑，加上当前会话的时长
        long currentTime = 0;
        if (filePath.equals(activeFilePath) && sessionStartTime > 0) {
            currentTime = System.currentTimeMillis() - sessionStartTime;
        }

        return stats.getTotalTime() + currentTime;
    }

    /**
     * 格式化时长显示
     */
    public String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days >= 1) {
            hours = hours % 24;
            return days + " 天 " + hours + " 小时";
        } else if (hours >= 1) {
            minutes = minutes % 60;
            return hours + " 小时 " + minutes + " 分钟";
        } else if (minutes >= 1) {
            return minutes + " 分钟";
        } else {
            return seconds + " 秒";
        }
    }

    /**
     * 文件统计信息
     */
    private static class FileStatistics {
        private long totalTime; // 总时长（毫秒）

        public FileStatistics() {
            this.totalTime = 0;
        }

        public void addTime(long milliseconds) {
            this.totalTime += milliseconds;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void startTiming() {
            // 开始计时（由 Statistics 类管理开始时间）
        }
    }
}

