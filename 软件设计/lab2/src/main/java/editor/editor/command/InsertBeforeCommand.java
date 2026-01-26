package editor.editor.command;

import editor.editor.XmlEditor;

/**
 * 插入元素命令（在目标元素前）
 */
public class InsertBeforeCommand implements Command {
    private final XmlEditor editor;
    private final String tagName;
    private final String newId;
    private final String targetId;
    private final String text;

    public InsertBeforeCommand(XmlEditor editor, String tagName, String newId, String targetId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.targetId = targetId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insertBefore(tagName, newId, targetId, text);
    }

    @Override
    public void undo() {
        editor.deleteElement(newId);
    }
}

