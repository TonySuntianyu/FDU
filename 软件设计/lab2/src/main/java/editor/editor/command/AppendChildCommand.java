package editor.editor.command;

import editor.editor.XmlEditor;

/**
 * 追加子元素命令
 */
public class AppendChildCommand implements Command {
    private final XmlEditor editor;
    private final String tagName;
    private final String newId;
    private final String parentId;
    private final String text;

    public AppendChildCommand(XmlEditor editor, String tagName, String newId, String parentId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.appendChild(tagName, newId, parentId, text);
    }

    @Override
    public void undo() {
        editor.deleteElement(newId);
    }
}

