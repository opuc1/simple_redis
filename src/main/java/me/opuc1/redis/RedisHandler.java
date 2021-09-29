package me.opuc1.redis;

public interface RedisHandler<M extends RedisMessage> {
    void handle(M message);
}
