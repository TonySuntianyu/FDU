package editor.editor.command;

import editor.editor.TextEditor;

import java.util.List;

/**
 * 删除命令
 */
public class DeleteCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final int len;
    private String deletedText;

    public DeleteCommand(TextEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
    }

    @Override
    public void execute() {
        List<String> lines = editor.getLines();
        if (line >= 1 && line <= lines.size()) {
            String currentLine = lines.get(line - 1);
            if (col >= 1 && col <= currentLine.length()) {
                int endCol = Math.min(col + len - 1, currentLine.length());
                deletedText = currentLine.substring(col - 1, endCol);
            }
        }
        editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        if (deletedText != null) {
            editor.insert(line, col, deletedText);
        }
    }
}

