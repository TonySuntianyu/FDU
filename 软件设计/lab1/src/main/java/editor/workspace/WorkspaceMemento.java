package editor.workspace;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作区备忘录（备忘录模式）
 * 负责工作区状态的持久化
 */
public class WorkspaceMemento {
    private static final String WORKSPACE_FILE = ".editor_workspace";

    /**
     * 保存工作区状态
     */
    public static void save(WorkspaceState state) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(WORKSPACE_FILE))) {
            // 保存日志开关状态
            writer.println("log_enabled=" + state.isLogEnabled());
            
            // 保存活动文件
            if (state.getActiveFilePath() != null) {
                writer.println("active_file=" + state.getActiveFilePath());
            }
            
            // 保存文件列表
            writer.println("files_count=" + state.getFiles().size());
            for (WorkspaceState.FileState fileState : state.getFiles()) {
                writer.println("file=" + fileState.getFilePath() + "|" + fileState.isModified());
            }
        } catch (IOException e) {
            System.err.println("警告: 保存工作区状态失败: " + e.getMessage());
        }
    }

    /**
     * 加载工作区状态
     */
    public static WorkspaceState load() {
        Path path = Paths.get(WORKSPACE_FILE);
        if (!Files.exists(path)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            boolean logEnabled = false;
            String activeFilePath = null;
            List<WorkspaceState.FileState> files = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("log_enabled=")) {
                    logEnabled = Boolean.parseBoolean(line.substring("log_enabled=".length()));
                } else if (line.startsWith("active_file=")) {
                    activeFilePath = line.substring("active_file=".length());
                } else if (line.startsWith("files_count=")) {
                    // 文件数量，用于验证
                } else if (line.startsWith("file=")) {
                    String fileInfo = line.substring("file=".length());
                    String[] parts = fileInfo.split("\\|");
                    if (parts.length == 2) {
                        String filePath = parts[0];
                        boolean modified = Boolean.parseBoolean(parts[1]);
                        files.add(new WorkspaceState.FileState(filePath, modified));
                    }
                }
            }

            return new WorkspaceState(files, activeFilePath, logEnabled);
        } catch (IOException e) {
            System.err.println("警告: 加载工作区状态失败: " + e.getMessage());
            return null;
        }
    }
}

