package editor.editor.command;

/**
 * 命令接口（命令模式）
 * 所有编辑命令必须实现此接口
 */
public interface Command {
    /**
     * 执行命令
     */
    void execute();

    /**
     * 撤销命令
     */
    void undo();
}

