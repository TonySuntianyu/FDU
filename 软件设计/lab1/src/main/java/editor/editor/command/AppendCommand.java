package editor.editor.command;

import editor.editor.TextEditor;

/**
 * 追加命令
 */
public class AppendCommand implements Command {
    private final TextEditor editor;
    private final String text;
    private final boolean asNewLine; // true: 添加新行, false: 追加到行尾
    private boolean appendedToLastLine = false;
    private String originalLastLine = null;

    public AppendCommand(TextEditor editor, String text) {
        this(editor, text, false); // 默认追加到行尾
    }

    public AppendCommand(TextEditor editor, String text, boolean asNewLine) {
        this.editor = editor;
        this.text = text;
        this.asNewLine = asNewLine;
    }

    @Override
    public void execute() {
        int lineCount = editor.getLineCountInternal();
        if (asNewLine || lineCount == 0) {
            editor.appendDirectly(text);
            appendedToLastLine = false;
        } else {
            originalLastLine = editor.getLineInternal(lineCount - 1);
            editor.appendToLastLine(text);
            appendedToLastLine = true;
        }
    }

    @Override
    public void undo() {
        if (appendedToLastLine && originalLastLine != null) {
            // 恢复最后一行
            editor.restoreLastLine(originalLastLine);
        } else {
            // 移除最后一行
            editor.removeLastLine();
        }
    }
}

