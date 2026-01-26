package editor.editor.command;

import editor.editor.XmlEditor;

/**
 * 修改元素 ID 命令
 */
public class EditIdCommand implements Command {
    private final XmlEditor editor;
    private final String oldId;
    private final String newId;

    public EditIdCommand(XmlEditor editor, String oldId, String newId) {
        this.editor = editor;
        this.oldId = oldId;
        this.newId = newId;
    }

    @Override
    public void execute() {
        editor.editId(oldId, newId);
    }

    @Override
    public void undo() {
        editor.editId(newId, oldId);
    }
}

