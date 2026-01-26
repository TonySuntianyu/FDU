package editor.workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作区状态（备忘录模式）
 * 用于保存和恢复工作区状态
 */
public class WorkspaceState {
    private final List<FileState> files;
    private final String activeFilePath;
    private final boolean logEnabled;

    public WorkspaceState(List<FileState> files, String activeFilePath, boolean logEnabled) {
        this.files = new ArrayList<>(files);
        this.activeFilePath = activeFilePath;
        this.logEnabled = logEnabled;
    }

    public List<FileState> getFiles() {
        return new ArrayList<>(files);
    }

    public String getActiveFilePath() {
        return activeFilePath;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    /**
     * 文件状态
     */
    public static class FileState {
        private final String filePath;
        private final boolean modified;

        public FileState(String filePath, boolean modified) {
            this.filePath = filePath;
            this.modified = modified;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isModified() {
            return modified;
        }
    }
}

