package it.bitrule.rubudu.repository;

import it.bitrule.rubudu.repository.connection.RedisConnection;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor @Data
public final class RedisRepository {

    private final @NonNull RedisConnection redisConnection;

    /**
     * Execute a command for redis and return the value expected
     *
     * @param function The function to return the value
     * @return Return a value expected
     * @param <T> idk
     */
    public @Nullable <T> T supplyCommand(@NonNull Function<Jedis, T> function) {
        if (!this.redisConnection.isConnected()) {
            throw new IllegalStateException("Connection with Redis never initialized");
        }

        if (!this.redisConnection.isEnabled()) return null;

        JedisPool jedisPool = this.redisConnection.getJedisPool();
        if (jedisPool == null) {
            throw new IllegalStateException("JedisPool is null");
        }

        if (jedisPool.isClosed()) {
            throw new IllegalStateException("JedisPool is closed");
        }

        try (Jedis jedis = this.redisConnection.getJedisPool().getResource()) {
            String password = this.redisConnection.getPassword();
            if (password != null && !password.isEmpty()) {
                jedis.auth(password);
            }

            if (!jedis.isConnected()) {
                throw new IllegalStateException("JedisPool is not connected");
            }

            return function.apply(jedis);
        }
    }

    /**
     * Execute a redis command with a consumer
     *
     * @param consumer The consumer
     */
    public void runCommand(@NonNull Consumer<Jedis> consumer) {
        if (this.redisConnection == null || !this.redisConnection.isConnected()) {
            throw new IllegalStateException("Connection with Redis never initialized");
        }

        if (!this.redisConnection.isEnabled()) return;

        JedisPool jedisPool = this.redisConnection.getJedisPool();
        if (jedisPool == null) {
            throw new IllegalStateException("JedisPool is null");
        }

        if (jedisPool.isClosed()) {
            throw new IllegalStateException("JedisPool is closed");
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String password = this.redisConnection.getPassword();
            if (password != null && !password.isEmpty()) {
                jedis.auth(password);
            }

            if (!jedis.isConnected()) {
                throw new IllegalStateException("JedisPool is not connected");
            }

            consumer.accept(jedis);
        }
    }
}