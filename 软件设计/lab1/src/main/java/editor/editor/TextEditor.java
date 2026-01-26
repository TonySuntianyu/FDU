package editor.editor;

import editor.editor.command.Command;
import editor.editor.command.AppendCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 文本编辑器实现
 * 使用行数组存储文本内容
 */
public class TextEditor implements Editor {
    private final List<String> lines;
    private boolean modified;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;

    public TextEditor() {
        this.lines = new ArrayList<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    @Override
    public void append(String text) {
        if (text == null) {
            text = "";
        }
        // 根据需求，append 应该在文件末尾追加一行文本
        // 使用命令模式以支持撤销/重做
        AppendCommand cmd = new AppendCommand(this, text, true);
        executeCommand(cmd);
    }

    @Override
    public void insert(int line, int col, String text) throws IllegalArgumentException {
        if (text == null) {
            text = "";
        }

        // 检查边界
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new IllegalArgumentException("空文件只能在 1:1 位置插入");
            }
            // 处理换行符
            if (text.contains("\n")) {
                String[] parts = text.split("\n", -1);
                for (String part : parts) {
                    lines.add(part);
                }
            } else {
                lines.add(text);
            }
            markModified();
            return;
        }

        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length() + 1) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        // 处理换行符
        if (text.contains("\n")) {
            String[] parts = text.split("\n", -1);
            String prefix = currentLine.substring(0, col - 1);
            String suffix = currentLine.substring(col - 1);
            
            // 第一行：当前行的前缀 + 插入文本的第一部分
            lines.set(line - 1, prefix + parts[0]);
            
            // 中间行：插入文本的中间部分（如果有）
            for (int i = 1; i < parts.length - 1; i++) {
                lines.add(line - 1 + i, parts[i]);
            }
            
            // 最后一行：插入文本的最后部分 + 当前行的后缀
            // 注意：如果 text.contains("\n") 为 true，parts.length 至少为 2
            lines.add(line - 1 + parts.length - 1, parts[parts.length - 1] + suffix);
        } else {
            String newLine = currentLine.substring(0, col - 1) + text + currentLine.substring(col - 1);
            lines.set(line - 1, newLine);
        }

        markModified();
    }

    @Override
    public void delete(int line, int col, int len) throws IllegalArgumentException {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("空文件无法删除");
        }

        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        String currentLine = lines.get(line - 1);
        if (col < 1 || col > currentLine.length()) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        if (len < 0) {
            throw new IllegalArgumentException("删除长度必须大于0");
        }

        if (col + len - 1 > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }

        String newLine = currentLine.substring(0, col - 1) + currentLine.substring(col - 1 + len);
        lines.set(line - 1, newLine);

        markModified();
    }

    @Override
    public void replace(int line, int col, int len, String text) throws IllegalArgumentException {
        if (text == null) {
            text = "";
        }

        // 先删除，再插入
        if (len > 0) {
            delete(line, col, len);
        }
        
        // 如果删除后行还存在，则插入
        if (!lines.isEmpty() && line <= lines.size()) {
            String currentLine = lines.get(line - 1);
            // 计算插入位置（删除后列号可能变化）
            int insertCol = Math.min(col, currentLine.length() + 1);
            insert(line, insertCol, text);
        } else if (len == 0) {
            // 如果只是插入，不删除
            insert(line, col, text);
        }
    }

    @Override
    public String show(int startLine, int endLine) {
        if (lines.isEmpty()) {
            return "";
        }

        int start = Math.max(1, startLine);
        int end = Math.min(lines.size(), endLine);

        if (start > end) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            sb.append(i).append(": ").append(lines.get(i - 1));
            if (i < end) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public void load(String content) {
        lines.clear();
        undoStack.clear();
        redoStack.clear();
        
        if (content == null || content.isEmpty()) {
            return;
        }

        // 按换行符分割，但保留空行
        String[] parts = content.split("\n", -1);
        for (String part : parts) {
            lines.add(part);
        }
        
        clearModified();
    }

    @Override
    public String getContent() {
        return String.join("\n", lines);
    }

    @Override
    public void markModified() {
        this.modified = true;
    }

    @Override
    public void clearModified() {
        this.modified = false;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        markModified();
    }

    @Override
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            markModified();
        }
    }

    @Override
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            markModified();
        }
    }

    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 移除最后一行（用于撤销append操作）
     */
    public void removeLastLine() {
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
        }
    }

    /**
     * 直接追加文本（不通过命令模式，用于命令内部调用）
     */
    public void appendDirectly(String text) {
        if (text == null) {
            text = "";
        }
        lines.add(text);
    }

    /**
     * 追加到最后一行的末尾（用于命令内部调用）
     */
    public void appendToLastLine(String text) {
        if (text == null) {
            text = "";
        }
        if (lines.isEmpty()) {
            lines.add(text);
            return;
        }
        int lastIndex = lines.size() - 1;
        lines.set(lastIndex, lines.get(lastIndex) + text);
    }

    /**
     * 恢复最后一行（用于撤销操作）
     */
    public void restoreLastLine(String originalLine) {
        if (!lines.isEmpty()) {
            int lastIndex = lines.size() - 1;
            lines.set(lastIndex, originalLine);
        }
    }

    /**
     * 获取当前行数（供命令内部使用）
     */
    public int getLineCountInternal() {
        return lines.size();
    }

    /**
     * 获取指定索引的行（供命令内部使用）
     */
    public String getLineInternal(int index) {
        return lines.get(index);
    }
}

