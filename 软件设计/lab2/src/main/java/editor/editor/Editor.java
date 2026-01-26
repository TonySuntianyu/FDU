package editor.editor;

import editor.editor.command.Command;

import java.util.List;

/**
 * 编辑器接口
 * 定义所有编辑器必须实现的基本操作
 */
public interface Editor {
    /**
     * 追加文本到文件末尾
     * @param text 要追加的文本
     */
    void append(String text);

    /**
     * 在指定位置插入文本
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param text 要插入的文本
     * @throws IllegalArgumentException 如果位置越界
     */
    void insert(int line, int col, String text) throws IllegalArgumentException;

    /**
     * 删除指定位置的字符
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param len 删除长度
     * @throws IllegalArgumentException 如果位置或长度越界
     */
    void delete(int line, int col, int len) throws IllegalArgumentException;

    /**
     * 替换指定位置的字符
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param len 替换长度
     * @param text 替换文本
     * @throws IllegalArgumentException 如果位置或长度越界
     */
    void replace(int line, int col, int len, String text) throws IllegalArgumentException;

    /**
     * 显示指定范围的内容
     * @param startLine 起始行号（从1开始）
     * @param endLine 结束行号（包含）
     * @return 显示的内容
     */
    String show(int startLine, int endLine);

    /**
     * 获取所有行
     * @return 行列表
     */
    List<String> getLines();

    /**
     * 从文件内容加载
     * @param content 文件内容
     */
    void load(String content);

    /**
     * 获取文件内容（用于保存）
     * @return 文件内容
     */
    String getContent();

    /**
     * 标记文件为已修改
     */
    void markModified();

    /**
     * 清除修改标记
     */
    void clearModified();

    /**
     * 检查文件是否已修改
     * @return true如果已修改
     */
    boolean isModified();

    /**
     * 执行命令（用于命令模式）
     * @param command 命令对象
     */
    void executeCommand(Command command);

    /**
     * 撤销上一次操作
     */
    void undo();

    /**
     * 重做上一次撤销的操作
     */
    void redo();

    /**
     * 检查是否可以撤销
     * @return true如果可以撤销
     */
    boolean canUndo();

    /**
     * 检查是否可以重做
     * @return true如果可以重做
     */
    boolean canRedo();
}

