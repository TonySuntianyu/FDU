package editor.editor.command;

import editor.editor.TextEditor;

/**
 * 替换命令
 */
public class ReplaceCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final int len;
    private final String newText;
    private String originalContent;

    public ReplaceCommand(TextEditor editor, int line, int col, int len, String newText) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
        this.newText = newText;
    }

    @Override
    public void execute() {
        // 保存原始内容用于撤销
        originalContent = editor.getContent();
        editor.replace(line, col, len, newText);
    }

    @Override
    public void undo() {
        if (originalContent != null) {
            editor.load(originalContent);
        }
    }
}

