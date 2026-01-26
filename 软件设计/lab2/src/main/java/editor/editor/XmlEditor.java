package editor.editor;

import editor.editor.command.Command;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * XML 编辑器实现
 * 支持 XML 文件的编辑操作
 */
public class XmlEditor implements Editor {
    private XmlElement root;
    private Map<String, XmlElement> idMap; // id -> element 映射
    private boolean modified;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;

    public XmlEditor() {
        this.idMap = new HashMap<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    @Override
    public void append(String text) {
        // XML 编辑器不支持 append 操作
        throw new UnsupportedOperationException("XML 编辑器不支持 append 操作");
    }

    @Override
    public void insert(int line, int col, String text) {
        // XML 编辑器不支持基于行列的插入
        throw new UnsupportedOperationException("XML 编辑器不支持基于行列的插入操作");
    }

    @Override
    public void delete(int line, int col, int len) {
        // XML 编辑器不支持基于行列的删除
        throw new UnsupportedOperationException("XML 编辑器不支持基于行列的删除操作");
    }

    @Override
    public void replace(int line, int col, int len, String text) {
        // XML 编辑器不支持基于行列的替换
        throw new UnsupportedOperationException("XML 编辑器不支持基于行列的替换操作");
    }

    @Override
    public String show(int startLine, int endLine) {
        // XML 编辑器使用 xml-tree 命令显示
        return xmlTreeToString();
    }

    @Override
    public List<String> getLines() {
        // 将 XML 内容转换为行列表
        String content = getContent();
        if (content.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(content.split("\n", -1));
    }

    @Override
    public void load(String content) {
        root = null;
        idMap.clear();
        undoStack.clear();
        redoStack.clear();

        if (content == null || content.trim().isEmpty()) {
            // 创建空的根元素
            root = new XmlElement("root", "root");
            idMap.put("root", root);
            clearModified();
            return;
        }

        // 解析 XML 内容
        try {
            root = parseXml(content);
            buildIdMap(root);
            clearModified();
        } catch (Exception e) {
            throw new IllegalArgumentException("XML 解析失败: " + e.getMessage());
        }
    }

    @Override
    public String getContent() {
        if (root == null) {
            return "";
        }
        return serializeXml(root);
    }

    @Override
    public void markModified() {
        this.modified = true;
    }

    @Override
    public void clearModified() {
        this.modified = false;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        markModified();
    }

    @Override
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            markModified();
        }
    }

    @Override
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            markModified();
        }
    }

    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    // ========== XML 特定方法 ==========

    public XmlElement getRoot() {
        return root;
    }

    public XmlElement getElementById(String id) {
        return idMap.get(id);
    }

    public boolean hasElement(String id) {
        return idMap.containsKey(id);
    }

    /**
     * 插入元素到目标元素前
     */
    public void insertBefore(String tagName, String newId, String targetId, String text) {
        if (idMap.containsKey(newId)) {
            throw new IllegalArgumentException("元素 ID 已存在: " + newId);
        }

        XmlElement target = idMap.get(targetId);
        if (target == null) {
            throw new IllegalArgumentException("目标元素不存在: " + targetId);
        }

        if (target == root) {
            throw new IllegalArgumentException("不能在根元素前插入元素");
        }

        XmlElement parent = target.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("目标元素没有父元素");
        }

        XmlElement newElement = new XmlElement(tagName, newId);
        if (text != null && !text.isEmpty()) {
            newElement.setText(text);
        }

        int index = parent.getChildIndex(target);
        parent.insertChild(index, newElement);
        idMap.put(newId, newElement);
    }

    /**
     * 追加子元素
     */
    public void appendChild(String tagName, String newId, String parentId, String text) {
        if (idMap.containsKey(newId)) {
            throw new IllegalArgumentException("元素 ID 已存在: " + newId);
        }

        XmlElement parent = idMap.get(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父元素不存在: " + parentId);
        }

        if (parent.hasText()) {
            throw new IllegalArgumentException("该元素已有文本内容，不支持混合内容");
        }

        XmlElement newElement = new XmlElement(tagName, newId);
        if (text != null && !text.isEmpty()) {
            newElement.setText(text);
        }

        parent.addChild(newElement);
        idMap.put(newId, newElement);
    }

    /**
     * 修改元素 ID
     */
    public void editId(String oldId, String newId) {
        if (oldId.equals("root")) {
            throw new IllegalArgumentException("不允许修改根元素 ID");
        }

        XmlElement element = idMap.get(oldId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + oldId);
        }

        if (idMap.containsKey(newId)) {
            throw new IllegalArgumentException("目标 ID 已存在: " + newId);
        }

        idMap.remove(oldId);
        element.setId(newId);
        idMap.put(newId, element);
    }

    /**
     * 修改元素文本
     */
    public void editText(String elementId, String text) {
        XmlElement element = idMap.get(elementId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + elementId);
        }

        if (element.hasChildren()) {
            throw new IllegalArgumentException("该元素有子元素，不支持混合内容");
        }

        element.setText(text);
    }

    /**
     * 删除元素
     */
    public void deleteElement(String elementId) {
        if (elementId.equals("root")) {
            throw new IllegalArgumentException("不能删除根元素");
        }

        XmlElement element = idMap.get(elementId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + elementId);
        }

        XmlElement parent = element.getParent();
        if (parent != null) {
            parent.removeChild(element);
        }

        // 递归删除所有子元素的 ID
        removeElementIds(element);
    }

    /**
     * 递归删除元素及其子元素的 ID
     */
    private void removeElementIds(XmlElement element) {
        idMap.remove(element.getId());
        for (XmlElement child : element.getChildren()) {
            removeElementIds(child);
        }
    }

    /**
     * 构建 ID 映射
     */
    private void buildIdMap(XmlElement element) {
        idMap.put(element.getId(), element);
        for (XmlElement child : element.getChildren()) {
            buildIdMap(child);
        }
    }

    /**
     * 恢复元素到 ID 映射（用于撤销操作）
     */
    public void restoreElementToIdMap(XmlElement element) {
        idMap.put(element.getId(), element);
    }

    /**
     * 显示 XML 树形结构
     */
    public String xmlTreeToString() {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        printTree(root, sb, "", true);
        return sb.toString();
    }

    private void printTree(XmlElement element, StringBuilder sb, String prefix, boolean isLast) {
        // 打印元素标签和属性
        String connector = isLast ? "└── " : "├── ";
        sb.append(prefix).append(connector).append(element.getTagName());
        Map<String, String> attrs = element.getAttributes();
        if (!attrs.isEmpty()) {
            List<String> attrList = new ArrayList<>();
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                attrList.add(entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
            sb.append(" [").append(String.join(", ", attrList)).append("]");
        }
        sb.append("\n");

        // 打印文本内容
        if (element.hasText()) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            sb.append(newPrefix).append("└── \"").append(element.getText()).append("\"\n");
        }

        // 打印子元素
        List<XmlElement> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean isLastChild = (i == children.size() - 1) && !element.hasText();
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            printTree(children.get(i), sb, newPrefix, isLastChild);
        }
    }

    /**
     * 解析 XML 内容（简化版，仅支持基本语法）
     */
    private XmlElement parseXml(String content) {
        content = content.trim();
        
        // 移除 XML 声明
        if (content.startsWith("<?xml")) {
            int end = content.indexOf("?>");
            if (end != -1) {
                content = content.substring(end + 2).trim();
            }
        }

        // 解析根元素
        return parseElement(content);
    }

    /**
     * 解析 XML 元素
     */
    private XmlElement parseElement(String xml) {
        xml = xml.trim();
        if (!xml.startsWith("<") || !xml.contains(">")) {
            throw new IllegalArgumentException("无效的 XML 格式");
        }

        // 提取开始标签
        int tagEnd = xml.indexOf(">");
        String startTag = xml.substring(1, tagEnd);
        
        // 解析标签名和属性
        String[] parts = startTag.split("\\s+", 2);
        String tagName = parts[0];
        
        // 解析属性
        Map<String, String> attrs = new HashMap<>();
        if (parts.length > 1) {
            attrs = parseAttributes(parts[1]);
        }

        String id = attrs.get("id");
        if (id == null) {
            throw new IllegalArgumentException("元素缺少必需的 id 属性");
        }

        XmlElement element = new XmlElement(tagName, id);
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (!"id".equals(entry.getKey())) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        // 查找结束标签
        String endTag = "</" + tagName + ">";
        int endTagPos = xml.lastIndexOf(endTag);
        if (endTagPos == -1) {
            throw new IllegalArgumentException("缺少结束标签: " + endTag);
        }

        // 提取内容
        String content = xml.substring(tagEnd + 1, endTagPos).trim();

        // 判断是文本内容还是子元素
        if (content.isEmpty()) {
            // 空元素
        } else if (content.startsWith("<")) {
            // 子元素
            parseChildren(element, content);
        } else {
            // 文本内容
            element.setText(content);
        }

        return element;
    }

    /**
     * 解析属性
     */
    private Map<String, String> parseAttributes(String attrStr) {
        Map<String, String> attrs = new HashMap<>();
        if (attrStr == null || attrStr.trim().isEmpty()) {
            return attrs;
        }

        // 简单的属性解析：key="value"
        Matcher matcher = attrPattern.matcher(attrStr);
        while (matcher.find()) {
            attrs.put(matcher.group(1), matcher.group(2));
        }

        return attrs;
    }

    /**
     * 解析子元素
     */
    private void parseChildren(XmlElement parent, String content) {
        String remaining = content.trim();
        while (!remaining.isEmpty()) {
            if (!remaining.startsWith("<")) {
                throw new IllegalArgumentException("无效的 XML 格式");
            }

            // 找到开始标签
            int tagEnd = remaining.indexOf(">");
            if (tagEnd == -1) {
                throw new IllegalArgumentException("无效的 XML 格式");
            }
            
            String startTag = remaining.substring(1, tagEnd);
            String[] parts = startTag.split("\\s+", 2);
            String tagName = parts[0];

            // 找到对应的结束标签
            String endTag = "</" + tagName + ">";
            int depth = 1;
            int searchPos = tagEnd + 1;
            int endTagPos = -1;

            while (depth > 0 && searchPos < remaining.length()) {
                int nextStart = remaining.indexOf("<" + tagName, searchPos);
                int nextEnd = remaining.indexOf(endTag, searchPos);

                if (nextEnd == -1) {
                    throw new IllegalArgumentException("缺少结束标签: " + endTag);
                }

                if (nextStart != -1 && nextStart < nextEnd) {
                    depth++;
                    searchPos = nextStart + 1;
                } else {
                    depth--;
                    if (depth == 0) {
                        endTagPos = nextEnd;
                    } else {
                        searchPos = nextEnd + endTag.length();
                    }
                }
            }

            if (endTagPos == -1) {
                throw new IllegalArgumentException("缺少结束标签: " + endTag);
            }

            String elementXml = remaining.substring(0, endTagPos + endTag.length());
            XmlElement child = parseElement(elementXml);
            parent.addChild(child);

            remaining = remaining.substring(endTagPos + endTag.length()).trim();
        }
    }

    /**
     * 序列化 XML 元素
     */
    private String serializeXml(XmlElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        serializeElement(element, sb, 0);
        return sb.toString();
    }

    private void serializeElement(XmlElement element, StringBuilder sb, int indent) {
        // 缩进
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }

        // 开始标签
        sb.append("<").append(element.getTagName());
        
        // 属性
        Map<String, String> attrs = element.getAttributes();
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=\"").append(escapeXml(entry.getValue())).append("\"");
        }

        // 内容
        if (element.hasText() && element.hasChildren()) {
            throw new IllegalStateException("元素不能同时有文本和子元素（混合内容）");
        }

        if (element.hasText()) {
            sb.append(">").append(escapeXml(element.getText())).append("</").append(element.getTagName()).append(">");
        } else if (element.hasChildren()) {
            sb.append(">\n");
            for (XmlElement child : element.getChildren()) {
                serializeElement(child, sb, indent + 1);
                sb.append("\n");
            }
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append("</").append(element.getTagName()).append(">");
        } else {
            sb.append(" />");
        }
    }

    /**
     * XML 转义
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * XML 反转义
     */
    private String unescapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&apos;", "'")
                   .replace("&quot;", "\"")
                   .replace("&gt;", ">")
                   .replace("&lt;", "<")
                   .replace("&amp;", "&");
    }

    private static final Pattern attrPattern = Pattern.compile("(\\w+)=\"([^\"]*)\"");
}

