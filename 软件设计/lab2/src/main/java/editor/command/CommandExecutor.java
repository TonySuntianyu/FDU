package editor.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import editor.editor.Editor;
import editor.editor.TextEditor;
import editor.editor.XmlEditor;
import editor.editor.command.AppendChildCommand;
import editor.editor.command.AppendCommand;
import editor.editor.command.DeleteCommand;
import editor.editor.command.DeleteElementCommand;
import editor.editor.command.EditIdCommand;
import editor.editor.command.EditTextCommand;
import editor.editor.command.InsertBeforeCommand;
import editor.editor.command.InsertCommand;
import editor.editor.command.ReplaceCommand;
import editor.event.CommandEvent;
import editor.spellcheck.LanguageToolSpellChecker;
import editor.spellcheck.SpellChecker;
import editor.spellcheck.SpellError;
import editor.workspace.Workspace;

/**
 * 命令执行器
 * 执行解析后的命令
 */
public class CommandExecutor {
    private final Workspace workspace;
    private final Scanner scanner;
    private final SpellChecker spellChecker;

    public CommandExecutor(Workspace workspace) {
        this.workspace = workspace;
        this.scanner = new Scanner(System.in);
        this.spellChecker = new LanguageToolSpellChecker();
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
                case "insert-before":
                    executeInsertBefore(cmd);
                    break;
                case "append-child":
                    executeAppendChild(cmd);
                    break;
                case "edit-id":
                    executeEditId(cmd);
                    break;
                case "edit-text":
                    executeEditText(cmd);
                    break;
                case "delete-element":
                    executeDeleteElement(cmd);
                    break;
                case "xml-tree":
                    executeXmlTree(cmd);
                    break;
                case "spell-check":
                    executeSpellCheck(cmd);
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
        String arg0 = cmd.getArg(0);
        String arg1 = cmd.getArg(1);

        // 兼容两种格式：
        // 1) Lab1: init <file> [with-log]
        // 2) Lab2: init <text|xml> <file> [with-log]
        if (arg0 == null) {
            System.out.println("错误: init命令需要参数");
            return;
        }

        if ("text".equalsIgnoreCase(arg0) || "xml".equalsIgnoreCase(arg0)) {
            // Lab2 新格式
            String fileType = arg0.toLowerCase();
            String filePath = arg1;
            if (filePath == null) {
                System.out.println("错误: init命令需要文件类型和文件路径");
                return;
            }
            boolean withLog = "with-log".equals(cmd.getArg(2));
            workspace.initFile(fileType, filePath, withLog);
            publishCommand("init " + fileType + " " + filePath + (withLog ? " with-log" : ""), filePath);
            System.out.println("已创建新缓冲区: " + filePath);
        } else {
            // Lab1 旧格式，默认为 text 类型
            String filePath = arg0;
            boolean withLog = "with-log".equals(arg1);
            workspace.initFile(filePath, withLog);
            publishCommand("init " + filePath + (withLog ? " with-log" : ""), filePath);
            System.out.println("已创建新缓冲区: " + filePath);
        }
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
            sb.append(" (").append(info.getEditTime()).append(")");
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

    // ========== XML 编辑命令 ==========

    private void executeInsertBefore(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 当前文件不是 XML 文件");
            return;
        }

        try {
            String tagName = cmd.getArg(0);
            String newId = cmd.getArg(1);
            String targetId = cmd.getArg(2);
            String text = cmd.getArg(3);

            if (tagName == null || newId == null || targetId == null) {
                System.out.println("错误: insert-before命令参数不足");
                return;
            }

            XmlEditor xmlEditor = (XmlEditor) editor;
            InsertBeforeCommand command = new InsertBeforeCommand(xmlEditor, tagName, newId, targetId, text);
            xmlEditor.executeCommand(command);
            publishCommand("insert-before " + tagName + " " + newId + " " + targetId + 
                (text != null ? " \"" + text + "\"" : ""), workspace.getActiveFilePath());
            System.out.println("已插入元素");
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeAppendChild(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 当前文件不是 XML 文件");
            return;
        }

        try {
            String tagName = cmd.getArg(0);
            String newId = cmd.getArg(1);
            String parentId = cmd.getArg(2);
            String text = cmd.getArg(3);

            if (tagName == null || newId == null || parentId == null) {
                System.out.println("错误: append-child命令参数不足");
                return;
            }

            XmlEditor xmlEditor = (XmlEditor) editor;
            AppendChildCommand command = new AppendChildCommand(xmlEditor, tagName, newId, parentId, text);
            xmlEditor.executeCommand(command);
            publishCommand("append-child " + tagName + " " + newId + " " + parentId + 
                (text != null ? " \"" + text + "\"" : ""), workspace.getActiveFilePath());
            System.out.println("已追加子元素");
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeEditId(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 当前文件不是 XML 文件");
            return;
        }

        try {
            String oldId = cmd.getArg(0);
            String newId = cmd.getArg(1);

            if (oldId == null || newId == null) {
                System.out.println("错误: edit-id命令参数不足");
                return;
            }

            XmlEditor xmlEditor = (XmlEditor) editor;
            EditIdCommand command = new EditIdCommand(xmlEditor, oldId, newId);
            xmlEditor.executeCommand(command);
            publishCommand("edit-id " + oldId + " " + newId, workspace.getActiveFilePath());
            System.out.println("已修改元素 ID");
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeEditText(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 当前文件不是 XML 文件");
            return;
        }

        try {
            String elementId = cmd.getArg(0);
            String text = cmd.getArg(1);

            if (elementId == null || text == null) {
                System.out.println("错误: edit-text命令参数不足");
                return;
            }

            XmlEditor xmlEditor = (XmlEditor) editor;
            EditTextCommand command = new EditTextCommand(xmlEditor, elementId, text);
            xmlEditor.executeCommand(command);
            publishCommand("edit-text " + elementId + " \"" + text + "\"", workspace.getActiveFilePath());
            System.out.println("已修改元素文本");
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeDeleteElement(CommandParser.ParsedCommand cmd) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("错误: 没有活动文件");
            return;
        }

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 当前文件不是 XML 文件");
            return;
        }

        try {
            String elementId = cmd.getArg(0);
            if (elementId == null) {
                System.out.println("错误: delete-element命令需要元素 ID");
                return;
            }

            XmlEditor xmlEditor = (XmlEditor) editor;
            DeleteElementCommand command = new DeleteElementCommand(xmlEditor, elementId);
            xmlEditor.executeCommand(command);
            publishCommand("delete-element " + elementId, workspace.getActiveFilePath());
            System.out.println("已删除元素");
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    private void executeXmlTree(CommandParser.ParsedCommand cmd) {
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

        if (!(editor instanceof XmlEditor)) {
            System.out.println("错误: 文件不是 XML 文件: " + filePath);
            return;
        }

        XmlEditor xmlEditor = (XmlEditor) editor;
        String tree = xmlEditor.xmlTreeToString();
        System.out.print(tree);
    }

    // ========== 拼写检查命令 ==========

    private void executeSpellCheck(CommandParser.ParsedCommand cmd) {
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

        try {
            List<SpellError> errors = new ArrayList<>();
            
            if (editor instanceof TextEditor) {
                // 文本文件：检查所有文本内容
                TextEditor textEditor = (TextEditor) editor;
                List<String> lines = textEditor.getLines();
                int lineNum = 1;
                int colOffset = 0;
                
                for (String line : lines) {
                    List<SpellError> lineErrors = spellChecker.check(line);
                    for (SpellError error : lineErrors) {
                        // 计算在文件中的位置
                        int col = error.getPosition() + 1;
                        errors.add(new TextSpellError(error.getWord(), error.getSuggestion(), 
                            lineNum, col, colOffset + error.getPosition()));
                    }
                    colOffset += line.length() + 1; // +1 for newline
                    lineNum++;
                }
            } else if (editor instanceof XmlEditor) {
                // XML 文件：仅检查元素的文本内容
                XmlEditor xmlEditor = (XmlEditor) editor;
                errors = checkXmlSpelling(xmlEditor);
            } else {
                System.out.println("错误: 不支持的文件类型");
                return;
            }

            // 输出结果
            if (errors.isEmpty()) {
                System.out.println("拼写检查结果：\n未发现拼写错误");
            } else {
                System.out.println("拼写检查结果：");
                for (SpellError error : errors) {
                    if (error instanceof TextSpellError) {
                        TextSpellError textError = (TextSpellError) error;
                        System.out.println("第" + textError.getLine() + "行，第" + textError.getCol() + "列：" +
                            "\"" + error.getWord() + "\" -> 建议：" + error.getSuggestion());
                    } else if (error instanceof XmlSpellError) {
                        XmlSpellError xmlError = (XmlSpellError) error;
                        System.out.println("元素 " + xmlError.getElementId() + ": \"" + error.getWord() + 
                            "\" -> 建议：" + error.getSuggestion());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("警告: 拼写检查失败: " + e.getMessage());
        }
    }

    /**
     * 检查 XML 文件的拼写
     */
    private List<SpellError> checkXmlSpelling(XmlEditor xmlEditor) {
        List<SpellError> errors = new ArrayList<>();
        checkElementSpelling(xmlEditor.getRoot(), xmlEditor, errors);
        return errors;
    }

    /**
     * 递归检查元素的拼写
     */
    private void checkElementSpelling(editor.editor.XmlElement element, XmlEditor xmlEditor, List<SpellError> errors) {
        // 检查元素的文本内容
        if (element.hasText()) {
            List<SpellError> textErrors = spellChecker.check(element.getText());
            for (SpellError error : textErrors) {
                errors.add(new XmlSpellError(error.getWord(), error.getSuggestion(), element.getId()));
            }
        }

        // 递归检查子元素
        for (editor.editor.XmlElement child : element.getChildren()) {
            checkElementSpelling(child, xmlEditor, errors);
        }
    }

    /**
     * 文本文件的拼写错误（带行列信息）
     */
    private static class TextSpellError extends SpellError {
        private final int line;
        private final int col;

        public TextSpellError(String word, String suggestion, int line, int col, int position) {
            super(word, suggestion, position);
            this.line = line;
            this.col = col;
        }

        public int getLine() {
            return line;
        }

        public int getCol() {
            return col;
        }
    }

    /**
     * XML 文件的拼写错误（带元素 ID）
     */
    private static class XmlSpellError extends SpellError {
        private final String elementId;

        public XmlSpellError(String word, String suggestion, String elementId) {
            super(word, suggestion, 0);
            this.elementId = elementId;
        }

        public String getElementId() {
            return elementId;
        }
    }
}

