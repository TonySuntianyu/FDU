package editor.editor.command;

import editor.editor.XmlEditor;

/**
 * 修改元素文本命令
 */
public class EditTextCommand implements Command {
    private final XmlEditor editor;
    private final String elementId;
    private final String newText;
    private String oldText;

    public EditTextCommand(XmlEditor editor, String elementId, String newText) {
        this.editor = editor;
        this.elementId = elementId;
        this.newText = newText;
    }

    @Override
    public void execute() {
        var element = editor.getElementById(elementId);
        if (element != null) {
            oldText = element.getText();
        }
        editor.editText(elementId, newText);
    }

    @Override
    public void undo() {
        if (oldText != null) {
            editor.editText(elementId, oldText);
        }
    }
}

