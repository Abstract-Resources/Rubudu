package it.bitrule.rubudu.repository.connection;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@RequiredArgsConstructor @Data
public final class RedisConnection {

    private final @NonNull String address;
    private final @Nullable String password;
    private final @NonNull Integer databaseId;

    private @Nullable JedisPool jedisPool;

    private boolean connected = false;
    private boolean enabled = true;

    public void connect() {
        if (this.address.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        String[] addressSplit = this.address.split(":");

        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Protocol.DEFAULT_PORT;

        this.jedisPool = new JedisPool(
                new JedisPoolConfig(),
                host,
                port,
                30_000,
                null,
                0,
                null
        );

        this.connected = true;
    }

    public void shutdown() {
        if (this.jedisPool != null) {
            this.jedisPool.close();
        }
    }
}