package editor.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件发布者测试
 */
public class EventPublisherTest {
    private EventPublisher publisher;
    private TestEventListener listener1;
    private TestEventListener listener2;

    @BeforeEach
    void setUp() {
        publisher = new EventPublisher();
        listener1 = new TestEventListener();
        listener2 = new TestEventListener();
    }

    @Test
    void testSubscribe() {
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        
        CommandEvent event = new CommandEvent("test command", "test.txt");
        publisher.publish(event);
        
        assertEquals(1, listener1.getEventCount());
        assertEquals(1, listener2.getEventCount());
        assertEquals(event, listener1.getLastEvent());
        assertEquals(event, listener2.getLastEvent());
    }

    @Test
    void testUnsubscribe() {
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        
        CommandEvent event1 = new CommandEvent("command1", "file1.txt");
        publisher.publish(event1);
        
        assertEquals(1, listener1.getEventCount());
        assertEquals(1, listener2.getEventCount());
        
        publisher.unsubscribe(listener1);
        
        CommandEvent event2 = new CommandEvent("command2", "file2.txt");
        publisher.publish(event2);
        
        // listener1 应该只收到第一个事件
        assertEquals(1, listener1.getEventCount());
        // listener2 应该收到两个事件
        assertEquals(2, listener2.getEventCount());
    }

    @Test
    void testPublishEvent() {
        publisher.subscribe(listener1);
        
        CommandEvent event = new CommandEvent("append \"test\"", "test.txt");
        publisher.publish(event);
        
        assertEquals(1, listener1.getEventCount());
        assertEquals("append \"test\"", listener1.getLastEvent().getCommand());
        assertEquals("test.txt", listener1.getLastEvent().getFilePath());
        assertNotNull(listener1.getLastEvent().getTimestamp());
    }

    @Test
    void testMultipleListeners() {
        TestEventListener listener3 = new TestEventListener();
        
        publisher.subscribe(listener1);
        publisher.subscribe(listener2);
        publisher.subscribe(listener3);
        
        CommandEvent event = new CommandEvent("save", "test.txt");
        publisher.publish(event);
        
        // 所有监听器都应该收到事件
        assertEquals(1, listener1.getEventCount());
        assertEquals(1, listener2.getEventCount());
        assertEquals(1, listener3.getEventCount());
        assertEquals(event, listener1.getLastEvent());
        assertEquals(event, listener2.getLastEvent());
        assertEquals(event, listener3.getLastEvent());
    }

    @Test
    void testEventNotification() {
        publisher.subscribe(listener1);
        
        // 发布多个事件
        CommandEvent event1 = new CommandEvent("load test.txt", "test.txt");
        CommandEvent event2 = new CommandEvent("append \"hello\"", "test.txt");
        CommandEvent event3 = new CommandEvent("save", "test.txt");
        
        publisher.publish(event1);
        publisher.publish(event2);
        publisher.publish(event3);
        
        assertEquals(3, listener1.getEventCount());
        assertEquals(event3, listener1.getLastEvent());
        
        // 验证所有事件都被接收
        List<CommandEvent> receivedEvents = listener1.getReceivedEvents();
        assertEquals(3, receivedEvents.size());
        assertEquals(event1, receivedEvents.get(0));
        assertEquals(event2, receivedEvents.get(1));
        assertEquals(event3, receivedEvents.get(2));
    }

    @Test
    void testNoListeners() {
        // 没有监听器时，发布事件不应该抛出异常
        CommandEvent event = new CommandEvent("test", "test.txt");
        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    void testUnsubscribeNonExistent() {
        // 取消订阅不存在的监听器不应该抛出异常
        assertDoesNotThrow(() -> publisher.unsubscribe(listener1));
    }

    @Test
    void testEventTimestamp() {
        publisher.subscribe(listener1);
        
        CommandEvent event = new CommandEvent("test", "test.txt");
        publisher.publish(event);
        
        assertNotNull(listener1.getLastEvent().getTimestamp());
        assertNotNull(listener1.getLastEvent().getFormattedTimestamp());
    }

    /**
     * 测试用的事件监听器
     */
    private static class TestEventListener implements EventListener {
        private final List<CommandEvent> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(CommandEvent event) {
            receivedEvents.add(event);
        }

        public int getEventCount() {
            return receivedEvents.size();
        }

        public CommandEvent getLastEvent() {
            return receivedEvents.isEmpty() ? null : receivedEvents.get(receivedEvents.size() - 1);
        }

        public List<CommandEvent> getReceivedEvents() {
            return new ArrayList<>(receivedEvents);
        }
    }
}

