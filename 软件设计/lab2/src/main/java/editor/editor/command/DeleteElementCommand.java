package editor.editor.command;

import editor.editor.XmlEditor;
import editor.editor.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除元素命令
 */
public class DeleteElementCommand implements Command {
    private final XmlEditor editor;
    private final String elementId;
    private XmlElement deletedElement;
    private XmlElement parent;
    private int childIndex;

    public DeleteElementCommand(XmlEditor editor, String elementId) {
        this.editor = editor;
        this.elementId = elementId;
    }

    @Override
    public void execute() {
        var element = editor.getElementById(elementId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + elementId);
        }

        parent = element.getParent();
        if (parent != null) {
            childIndex = parent.getChildIndex(element);
        }

        // 保存被删除的元素（深拷贝）
        deletedElement = deepCopy(element);

        editor.deleteElement(elementId);
    }

    @Override
    public void undo() {
        if (deletedElement == null) {
            return;
        }

        // 恢复元素到 ID 映射
        restoreElement(deletedElement);

        // 重新插入到父元素
        if (parent != null) {
            parent.insertChild(childIndex, deletedElement);
        }
    }

    /**
     * 深拷贝元素
     */
    private XmlElement deepCopy(XmlElement element) {
        XmlElement copy = new XmlElement(element.getTagName(), element.getId());
        copy.setText(element.getText());
        
        // 复制属性
        for (var entry : element.getAttributes().entrySet()) {
            if (!"id".equals(entry.getKey())) {
                copy.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        // 递归复制子元素
        for (XmlElement child : element.getChildren()) {
            XmlElement childCopy = deepCopy(child);
            copy.addChild(childCopy);
        }

        return copy;
    }

    /**
     * 恢复元素到 ID 映射
     */
    private void restoreElement(XmlElement element) {
        // 这里需要访问 XmlEditor 的内部方法，暂时通过反射或添加方法
        // 为了简化，我们在 XmlEditor 中添加一个方法
        editor.restoreElementToIdMap(element);
        
        for (XmlElement child : element.getChildren()) {
            restoreElement(child);
        }
    }
}

