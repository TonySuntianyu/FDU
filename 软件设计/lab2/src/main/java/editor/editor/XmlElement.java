package editor.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML 元素节点
 * 使用组合模式表示 XML 树形结构
 */
public class XmlElement {
    private String tagName;
    private String id;
    private String text;
    private Map<String, String> attributes;
    private List<XmlElement> children;
    private XmlElement parent;

    public XmlElement(String tagName, String id) {
        this.tagName = tagName;
        this.id = id;
        this.text = null;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.parent = null;
        // id 是必需的属性
        this.attributes.put("id", id);
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.attributes.put("id", id);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean hasText() {
        return text != null && !text.isEmpty();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean isMixedContent() {
        return hasText() && hasChildren();
    }

    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    public void setAttribute(String name, String value) {
        if ("id".equals(name)) {
            this.id = value;
        }
        attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public List<XmlElement> getChildren() {
        return new ArrayList<>(children);
    }

    public void addChild(XmlElement child) {
        if (child != null) {
            child.parent = this;
            children.add(child);
        }
    }

    public void removeChild(XmlElement child) {
        if (child != null) {
            child.parent = null;
            children.remove(child);
        }
    }

    public void insertChild(int index, XmlElement child) {
        if (child != null) {
            child.parent = this;
            children.add(index, child);
        }
    }

    public XmlElement getParent() {
        return parent;
    }

    public void setParent(XmlElement parent) {
        this.parent = parent;
    }

    public int getChildIndex(XmlElement child) {
        return children.indexOf(child);
    }

    /**
     * 获取所有子元素的 ID（递归）
     */
    public void getAllIds(List<String> ids) {
        ids.add(this.id);
        for (XmlElement child : children) {
            child.getAllIds(ids);
        }
    }
}

