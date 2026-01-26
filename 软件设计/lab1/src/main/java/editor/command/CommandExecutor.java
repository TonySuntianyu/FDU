package editor.command;

import editor.editor.Editor;
import editor.editor.TextEditor;
import editor.editor.command.*;
import editor.event.CommandEvent;
import editor.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 命令执行器
 * 执行解析后的命令
 */
public class CommandExecutor {
    private final Workspace workspace;
    private final Scanner scanner;

    public CommandExecutor(Workspace workspace) {
        this.workspace = workspace;
        this.scanner = new Scanner(System.in);
    }

    /**
     * 执行命令
     * @param cmd 解析后的命令
     * @return 是否继续执行（false表示退出）
     */
    public boolean execute(CommandParser.ParsedCommand cmd) {
        if (cmd == null) {
            System.out.println("未知命令");
            return true;
        }

        String commandName = cmd.getCommandName();

        try {
            switch (commandName) {
                case "load":
                    executeLoad(cmd);
                    break;
                case "save":
                    executeSave(cmd);
                    break;
                case "init":
                    executeInit(cmd);
                    break;
                case "close":
                    executeClose(cmd);
                    break;
                case "edit":
                    executeEdit(cmd);
                    break;
                case "editor-list":
                    executeEditorList();
                    break;
                case "dir-tree":
                    executeDirTree(cmd);
                    break;
                case "undo":
                    executeUndo();
                    break;
                case "redo":
                    executeRedo();
                    break;
                case "exit":
                    return executeExit();
                case "append":
                    executeAppend(cmd);
                    break;
                case "insert":
                    executeInsert(cmd);
                    break;
                case "delete":
                    executeDelete(cmd);
                    break;
                case "replace":
                    executeReplace(cmd);
                    break;
                case "show":
                    executeShow(cmd);
                    break;
                case "log-on":
                    executeLogOn(cmd);
                    break;
                case "log-off":
                    executeLogOff(cmd);
                    break;
                case "log-show":
                    executeLogShow(cmd);
                    break;
                default:
                    System.out.println("未知命令: " + commandName);
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }

        return true;
    }

    /**
     * 发布命令事件
     */
    private void publishCommand(String commandStr, String filePath) {
        workspace.getEventPublisher().publish(new CommandEvent(commandStr, filePath));
    }

    // ========== 工作区命令 ==========

    private void executeLoad(CommandParser.ParsedCommand cmd) throws IOException {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            System.out.println("错误: load命令需要文件路径");
            return;
        }

        workspace.loadFile(filePath);
        publishCommand("load " + filePath, filePath);
        System.out.println("已加载文件: " + filePath);
    }

    private void executeSave(CommandParser.ParsedCommand cmd) throws IOException {
        String arg = cmd.getArg(0);
        
        if (arg == null) {
            // 保存当前活动文件
            String activeFile = workspace.getActiveFilePath();
            if (activeFile == null) {
                System.out.println("错误: 没有活动文件");
                return;
            }
            workspace.saveFile(activeFile);
            publishCommand("save", activeFile);
            System.out.println("已保存: " + activeFile);
        } else if ("all".equals(arg)) {
            // 保存所有文件
            workspace.saveAllFiles();
            publishCommand("save all", null);
            System.out.println("已保存所有文件");
        } else {
            // 保存指定文件
            workspace.saveFile(arg);
            publishCommand("save " + arg, arg);
            System.out.println("已保存: " + arg);
        }
    }

    private void executeInit(CommandParser.ParsedCommand cmd) throws IOException {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            System.out.println("错误: init命令需要文件路径");
            return;
        }

        boolean withLog = "with-log".equals(cmd.getArg(1));
        workspace.initFile(filePath, withLog);
        publishCommand("init " + filePath + (withLog ? " with-log" : ""), filePath);
        System.out.println("已创建新缓冲区: " + filePath);
    }

    private void executeClose(CommandParser.ParsedCommand cmd) {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            filePath = workspace.getActiveFilePath();
            if (filePath == null) {
                System.out.println("错误: 没有活动文件");
                return;
            }
        }

        Editor editor = workspace.getEditor(filePath);
        if (editor == null) {
            System.out.println("错误: 文件未打开: " + filePath);
            return;
        }

        if (editor.isModified()) {
            System.out.print("文件已修改，是否保存？(y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(answer)) {
                try {
                    workspace.saveFile(filePath);
                    publishCommand("save", filePath);
                } catch (IOException e) {
                    System.out.println("保存失败: " + e.getMessage());
                    return;
                }
            }
        }

        workspace.forceCloseFile(filePath);
        publishCommand("close " + filePath, filePath);
        System.out.println("已关闭: " + filePath);
    }

    private void executeEdit(CommandParser.ParsedCommand cmd) {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            System.out.println("错误: edit命令需要文件路径");
            return;
        }

        try {
            workspace.setActiveFile(filePath);
            publishCommand("edit " + filePath, filePath);
            System.out.println("已切换到: " + filePath);
        } catch (IllegalArgumentException e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeEditorList() {
        List<Workspace.FileInfo> files = workspace.getFileList();
        if (files.isEmpty()) {
            System.out.println("没有打开的文件");
            return;
        }

        for (Workspace.FileInfo info : files) {
            StringBuilder sb = new StringBuilder();
            if (info.isActive()) {
                sb.append("* ");
            } else {
                sb.append("  ");
            }
            sb.append(info.getFilePath());
            if (info.isModified()) {
                sb.append(" [modified]");
            }
            System.out.println(sb.toString());
        }
    }

    private void executeDirTree(CommandParser.ParsedCommand cmd) {
        String pathStr = cmd.getArg(0);
        Path path = pathStr != null ? Paths.get(pathStr) : Paths.get(".");
        
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            System.out.println("错误: 目录不存在: " + path);
            return;
        }

        printDirTree(path, "", true);
    }

    private void printDirTree(Path path, String prefix, boolean isLast) {
        try {
            String name = path.getFileName() != null ? path.getFileName().toString() : path.toString();
            System.out.println(prefix + (isLast ? "└── " : "├── ") + name);

            if (Files.isDirectory(path)) {
                File[] files = path.toFile().listFiles();
                if (files != null) {
                    // 过滤掉隐藏文件（以.开头的文件）
                    List<File> visibleFiles = new java.util.ArrayList<>();
                    for (File f : files) {
                        if (!f.getName().startsWith(".")) {
                            visibleFiles.add(f);
                        }
                    }

                    for (int i = 0; i < visibleFiles.size(); i++) {
                        boolean isLastItem = (i == visibleFiles.size() - 1);
                        String newPrefix = prefix + (isLast ? "    " : "│   ");
                        printDirTree(visibleFiles.get(i).toPath(), newPrefix, isLastItem);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略权限错误等
        }
    }

    private void executeUndo() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (editor.canUndo()) {
            editor.undo();
            publishCommand("undo", workspace.getActiveFilePath());
            System.out.println("已撤销");
        } else {
            System.out.println("无法撤销");
        }
    }

    private void executeRedo() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (editor.canRedo()) {
            editor.redo();
            publishCommand("redo", workspace.getActiveFilePath());
            System.out.println("已重做");
        } else {
            System.out.println("无法重做");
        }
    }

    private boolean executeExit() {
        // 检查未保存的文件
        Set<String> openFiles = workspace.getOpenFiles();
        for (String filePath : openFiles) {
            Editor editor = workspace.getEditor(filePath);
            if (editor != null && editor.isModified()) {
                System.out.print("文件 " + filePath + " 已修改，是否保存？(y/n): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                if ("y".equals(answer)) {
                    try {
                        workspace.saveFile(filePath);
                    } catch (IOException e) {
                        System.out.println("保存失败: " + e.getMessage());
                    }
                }
            }
        }

        // 保存工作区状态
        workspace.saveState();
        publishCommand("exit", null);
        System.out.println("再见！");
        return false;
    }

    // ========== 文本编辑命令 ==========

    private void executeAppend(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        String text = cmd.getArg(0);
        if (text == null) {
            System.out.println("错误: append命令需要文本参数");
            return;
        }

        if (editor instanceof TextEditor) {
            AppendCommand command = new AppendCommand((TextEditor) editor, text, true);
            editor.executeCommand(command);
            publishCommand("append \"" + text + "\"", workspace.getActiveFilePath());
            System.out.println("已追加文本");
        }
    }

    private void executeInsert(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        try {
            int line = Integer.parseInt(cmd.getArg(0));
            int col = Integer.parseInt(cmd.getArg(1));
            String text = cmd.getArg(2);
            if (text == null) {
                System.out.println("错误: insert命令需要文本参数");
                return;
            }

            if (editor instanceof TextEditor) {
                InsertCommand command = new InsertCommand((TextEditor) editor, line, col, text);
                editor.executeCommand(command);
                publishCommand("insert " + line + ":" + col + " \"" + text + "\"", workspace.getActiveFilePath());
                System.out.println("已插入文本");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeDelete(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        try {
            int line = Integer.parseInt(cmd.getArg(0));
            int col = Integer.parseInt(cmd.getArg(1));
            int len = Integer.parseInt(cmd.getArg(2));

            if (editor instanceof TextEditor) {
                DeleteCommand command = new DeleteCommand((TextEditor) editor, line, col, len);
                editor.executeCommand(command);
                publishCommand("delete " + line + ":" + col + " " + len, workspace.getActiveFilePath());
                System.out.println("已删除字符");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeReplace(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        try {
            int line = Integer.parseInt(cmd.getArg(0));
            int col = Integer.parseInt(cmd.getArg(1));
            int len = Integer.parseInt(cmd.getArg(2));
            String text = cmd.getArg(3);
            if (text == null) {
                text = "";
            }

            if (editor instanceof TextEditor) {
                ReplaceCommand command = new ReplaceCommand((TextEditor) editor, line, col, len, text);
                editor.executeCommand(command);
                publishCommand("replace " + line + ":" + col + " " + len + " \"" + text + "\"", workspace.getActiveFilePath());
                System.out.println("已替换文本");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeShow(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        int startLine = 1;
        int endLine = editor.getLines().size();

        if (cmd.getArgCount() >= 2) {
            try {
                startLine = Integer.parseInt(cmd.getArg(0));
                endLine = Integer.parseInt(cmd.getArg(1));
            } catch (NumberFormatException e) {
                System.out.println("错误: 无效的行号");
                return;
            }
        }

        String content = editor.show(startLine, endLine);
        System.out.println(content);
    }

    // ========== 日志命令 ==========

    private void executeLogOn(CommandParser.ParsedCommand cmd) {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            filePath = workspace.getActiveFilePath();
            if (filePath == null) {
                System.out.println("错误: 没有活动文件");
                return;
            }
        }

        workspace.enableLog(filePath);
        publishCommand("log-on " + filePath, filePath);
        System.out.println("已启用日志: " + filePath);
    }

    private void executeLogOff(CommandParser.ParsedCommand cmd) {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            filePath = workspace.getActiveFilePath();
            if (filePath == null) {
                System.out.println("错误: 没有活动文件");
                return;
            }
        }

        workspace.disableLog(filePath);
        publishCommand("log-off " + filePath, filePath);
        System.out.println("已关闭日志: " + filePath);
    }

    private void executeLogShow(CommandParser.ParsedCommand cmd) {
        String filePath = cmd.getArg(0);
        if (filePath == null) {
            filePath = workspace.getActiveFilePath();
            if (filePath == null) {
                System.out.println("错误: 没有活动文件");
                return;
            }
        }

        String log = workspace.getLog(filePath);
        if (log.isEmpty()) {
            System.out.println("没有日志记录");
        } else {
            System.out.print(log);
        }
    }
}

