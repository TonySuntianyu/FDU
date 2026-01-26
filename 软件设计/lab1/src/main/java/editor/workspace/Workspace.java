package editor.workspace;

import editor.editor.Editor;
import editor.editor.TextEditor;
import editor.event.CommandEvent;
import editor.event.EventPublisher;
import editor.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 工作区
 * 管理多个编辑器实例和全局状态
 */
public class Workspace {
    private final Map<String, Editor> editors;
    private final Map<String, Logger> loggers;
    private String activeFilePath;
    private final EventPublisher eventPublisher;
    private final List<String> recentFiles; // 最近使用的文件列表

    public Workspace() {
        this.editors = new HashMap<>();
        this.loggers = new HashMap<>();
        this.activeFilePath = null;
        this.eventPublisher = new EventPublisher();
        this.recentFiles = new ArrayList<>();
    }

    /**
     * 获取事件发布者
     */
    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    /**
     * 加载文件
     */
    public void loadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        // 如果文件已打开，切换为活动文件
        if (editors.containsKey(filePath)) {
            setActiveFile(filePath);
            return;
        }

        // 创建编辑器
        Editor editor = new TextEditor();
        
        // 读取文件内容
        String content = "";
        if (Files.exists(path)) {
            content = new String(Files.readAllBytes(path));
        }
        
        // 加载内容到编辑器
        editor.load(content);
        
        // 如果文件不存在，标记为已修改
        if (!Files.exists(path)) {
            editor.markModified();
        }
        
        // 检查是否需要自动启用日志
        Logger logger = new Logger(filePath);
        if (Logger.shouldAutoEnableLog(content)) {
            logger.enable();
        }
        
        // 注册日志监听器
        eventPublisher.subscribe(logger);
        
        // 保存编辑器和日志器
        editors.put(filePath, editor);
        loggers.put(filePath, logger);
        
        // 设置为活动文件
        setActiveFile(filePath);
    }

    /**
     * 创建新缓冲区
     */
    public void initFile(String filePath, boolean withLog) throws IOException {
        if (editors.containsKey(filePath)) {
            throw new IOException("文件已存在: " + filePath);
        }

        Editor editor = new TextEditor();
        
        if (withLog) {
            editor.append("# log");
        }
        
        editor.markModified();
        
        // 创建日志器
        Logger logger = new Logger(filePath);
        if (withLog) {
            logger.enable();
        }
        eventPublisher.subscribe(logger);
        
        editors.put(filePath, editor);
        loggers.put(filePath, logger);
        
        setActiveFile(filePath);
    }

    /**
     * 保存文件
     */
    public void saveFile(String filePath) throws IOException {
        Editor editor = editors.get(filePath);
        if (editor == null) {
            throw new IOException("文件未打开: " + filePath);
        }

        String content = editor.getContent();
        Files.write(Paths.get(filePath), content.getBytes("UTF-8"));
        editor.clearModified();
        
        // 发布保存事件
        eventPublisher.publish(new CommandEvent("save", filePath));
    }

    /**
     * 保存所有文件
     */
    public void saveAllFiles() throws IOException {
        List<String> errors = new ArrayList<>();
        for (String filePath : editors.keySet()) {
            try {
                Editor editor = editors.get(filePath);
                if (editor.isModified()) {
                    saveFile(filePath);
                }
            } catch (IOException e) {
                errors.add(filePath + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new IOException("部分文件保存失败:\n" + String.join("\n", errors));
        }
    }

    /**
     * 关闭文件
     */
    public boolean closeFile(String filePath) {
        if (!editors.containsKey(filePath)) {
            return false;
        }

        Editor editor = editors.get(filePath);
        
        // 如果文件已修改，需要用户确认
        if (editor.isModified()) {
            // 这里应该由命令处理器处理用户交互
            // 暂时返回false表示需要确认
            return false;
        }

        // 取消订阅日志
        Logger logger = loggers.get(filePath);
        if (logger != null) {
            eventPublisher.unsubscribe(logger);
        }

        editors.remove(filePath);
        loggers.remove(filePath);
        
        // 如果关闭的是活动文件，切换到最近使用的文件
        if (filePath.equals(activeFilePath)) {
            activeFilePath = null;
            if (!recentFiles.isEmpty()) {
                String recent = recentFiles.get(recentFiles.size() - 1);
                if (editors.containsKey(recent)) {
                    setActiveFile(recent);
                }
            }
        }
        
        recentFiles.remove(filePath);
        
        return true;
    }

    /**
     * 强制关闭文件（不保存）
     */
    public void forceCloseFile(String filePath) {
        if (!editors.containsKey(filePath)) {
            return;
        }

        Logger logger = loggers.get(filePath);
        if (logger != null) {
            eventPublisher.unsubscribe(logger);
        }

        editors.remove(filePath);
        loggers.remove(filePath);
        
        if (filePath.equals(activeFilePath)) {
            activeFilePath = null;
            if (!recentFiles.isEmpty()) {
                String recent = recentFiles.get(recentFiles.size() - 1);
                if (editors.containsKey(recent)) {
                    setActiveFile(recent);
                }
            }
        }
        
        recentFiles.remove(filePath);
    }

    /**
     * 设置活动文件
     */
    public void setActiveFile(String filePath) {
        if (!editors.containsKey(filePath)) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }
        
        // 更新最近使用列表
        recentFiles.remove(filePath);
        recentFiles.add(filePath);
        
        activeFilePath = filePath;
    }

    /**
     * 获取活动文件
     */
    public Editor getActiveEditor() {
        if (activeFilePath == null) {
            return null;
        }
        return editors.get(activeFilePath);
    }

    /**
     * 获取活动文件路径
     */
    public String getActiveFilePath() {
        return activeFilePath;
    }

    /**
     * 获取编辑器
     */
    public Editor getEditor(String filePath) {
        return editors.get(filePath);
    }

    /**
     * 获取所有打开的文件
     */
    public Set<String> getOpenFiles() {
        return new HashSet<>(editors.keySet());
    }

    /**
     * 获取文件列表信息
     */
    public List<FileInfo> getFileList() {
        List<FileInfo> list = new ArrayList<>();
        for (String filePath : editors.keySet()) {
            Editor editor = editors.get(filePath);
            boolean isActive = filePath.equals(activeFilePath);
            boolean isModified = editor.isModified();
            list.add(new FileInfo(filePath, isActive, isModified));
        }
        return list;
    }

    /**
     * 启用日志
     */
    public void enableLog(String filePath) {
        Logger logger = loggers.get(filePath);
        if (logger != null) {
            logger.enable();
        }
    }

    /**
     * 禁用日志
     */
    public void disableLog(String filePath) {
        Logger logger = loggers.get(filePath);
        if (logger != null) {
            logger.disable();
        }
    }

    /**
     * 获取日志内容
     */
    public String getLog(String filePath) {
        Logger logger = loggers.get(filePath);
        if (logger != null) {
            return logger.readLog();
        }
        return "";
    }

    /**
     * 保存工作区状态
     */
    public void saveState() {
        List<WorkspaceState.FileState> files = new ArrayList<>();
        for (String filePath : editors.keySet()) {
            Editor editor = editors.get(filePath);
            files.add(new WorkspaceState.FileState(filePath, editor.isModified()));
        }
        
        WorkspaceState state = new WorkspaceState(files, activeFilePath, false);
        WorkspaceMemento.save(state);
    }

    /**
     * 加载工作区状态
     */
    public WorkspaceState loadState() {
        return WorkspaceMemento.load();
    }

    /**
     * 文件信息
     */
    public static class FileInfo {
        private final String filePath;
        private final boolean isActive;
        private final boolean isModified;

        public FileInfo(String filePath, boolean isActive, boolean isModified) {
            this.filePath = filePath;
            this.isActive = isActive;
            this.isModified = isModified;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isActive() {
            return isActive;
        }

        public boolean isModified() {
            return isModified;
        }
    }
}

