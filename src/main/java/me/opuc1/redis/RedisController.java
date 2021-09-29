package me.opuc1.redis;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class RedisController<M extends RedisMessage> {
    private final RedisManager manager;
    private final String channel;

    private Class<M> clazz;
    private RedisHandler<M> handler;

    public void handle(M message) {
        if (handler != null) handler.handle(message);
    }

    public void subscribe(RedisHandler<?> handler) {
        manager.subscribe(clazz, channel, handler);
    }

    public void publish(RedisMessage message) {
        manager.publish(channel, message);
    }
}
