package editor.event;

/**
 * 事件监听器接口（观察者模式）
 */
public interface EventListener {
    /**
     * 处理事件
     * @param event 事件对象
     */
    void onEvent(CommandEvent event);
}

