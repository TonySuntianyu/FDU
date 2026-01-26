package editor.event;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件发布者（观察者模式）
 * 用于发布命令执行事件
 */
public class EventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * 订阅事件
     * @param listener 事件监听器
     */
    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * 取消订阅
     * @param listener 事件监听器
     */
    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    /**
     * 发布命令执行事件
     * @param event 事件对象
     */
    public void publish(CommandEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}

