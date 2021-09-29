package me.opuc1.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final ExecutorService SUB_SERVICE = Executors.newSingleThreadExecutor();
    private static final ExecutorService PUB_SERVICE = Executors.newCachedThreadPool();

    private static final JedisPoolConfig DEFAULT_CONFIG = new JedisPoolConfig();
    private static final int TIMEOUT = 10_000;

    private final JedisPool jedisPool;

    public RedisManager(String host, int port, String password) {
        jedisPool = password.isEmpty() ? new JedisPool(DEFAULT_CONFIG, host, port, TIMEOUT) :
                new JedisPool(DEFAULT_CONFIG, host, port, TIMEOUT, password);
    }

    public void subscribe(Type type, String channel, RedisHandler<?> handler) {
        SUB_SERVICE.execute(() -> jedisPool.getResource().subscribe(new JedisPubSub() {
            public void onMessage(String channel, String message) {
                handler.handle(GSON.fromJson(message, type));
            }
        }, channel));
    }

    public void publish(String channel, RedisMessage message) {
        PUB_SERVICE.execute(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel, GSON.toJson(message));
            }
        });
    }

    public void shutdown() {
        PUB_SERVICE.shutdown();
        SUB_SERVICE.shutdown();
        jedisPool.close();
    }
}
