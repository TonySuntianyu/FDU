package editor.editor.command;

import editor.editor.TextEditor;

import java.util.List;

/**
 * 插入命令
 */
public class InsertCommand implements Command {
    private final TextEditor editor;
    private final int line;
    private final int col;
    private final String text;
    private String originalContent;

    public InsertCommand(TextEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
    }

    @Override
    public void execute() {
        // 保存原始内容用于撤销
        originalContent = editor.getContent();
        editor.insert(line, col, text);
    }

    @Override
    public void undo() {
        if (originalContent != null) {
            editor.load(originalContent);
        }
    }
}

