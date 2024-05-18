package it.bitrule.rubudu.repository;

import io.netty.buffer.Unpooled;
import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.repository.protocol.CloudByteStream;
import it.bitrule.rubudu.repository.protocol.CloudPacket;
import it.bitrule.rubudu.repository.protocol.InboundHandler;
import it.bitrule.rubudu.repository.protocol.PlayerJoinedNetworkPacket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.BinaryJedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class PublisherRepository {

    private final @NonNull Map<Integer, Class<? extends CloudPacket>> packetPool = new HashMap<>();

    private final @NonNull RedisRepository redisRepository;
    private final @NonNull String channelName;

    private @Nullable Subscription jedisPubSub = null;
    private @Nullable Thread thread = null;

    public void start() {
        this.thread = new Thread(() -> this.redisRepository.runCommand(jedis -> jedis.subscribe(this.jedisPubSub = new Subscription(), this.channelName.getBytes(StandardCharsets.UTF_8))));
        this.thread.start();

        this.registerPacket(
                new PlayerJoinedNetworkPacket()
        );
    }

    /**
     * Publish a packet to the channel
     * @param packet the packet to publish
     * @param async if the publishing should be async
     */
    public void publish(@NonNull CloudPacket packet, boolean async) {
        CloudByteStream stream = new CloudByteStream(Unpooled.buffer());

        stream.writeInt(packet.pid());
        packet.encode(stream);

        Runnable runnable = () -> this.redisRepository.runCommand(jedis -> jedis.publish(this.channelName.getBytes(StandardCharsets.UTF_8), stream.toArray()));

        if (async) {
            CompletableFuture.runAsync(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Rubudu.logger.severe("Failed to publish packet " + packet.getClass().getSimpleName() + " due to an exception:");
                    e.printStackTrace(System.out);
                }
            });
        } else {
            runnable.run();
        }
    }

    /**
     * Register a new packet
     * @param packets
     */
    public void registerPacket(@NonNull CloudPacket... packets) {
        for (CloudPacket packet : packets) {
            this.packetPool.put(packet.pid(), packet.getClass());
        }
    }

    public void shutdown() {
        if (this.thread != null) {
            this.thread.interrupt();
        }

        if (this.jedisPubSub != null) {
            this.jedisPubSub.unsubscribe();
        }
    }

    protected class Subscription extends BinaryJedisPubSub {

        @Override @SuppressWarnings("deprecation")
        public void onMessage(byte[] channel, byte[] message) {
            CloudByteStream stream = new CloudByteStream(Unpooled.wrappedBuffer(message));

            Class<? extends CloudPacket> clazz = packetPool.get(stream.readInt());
            if (clazz == null) return;

            try {
                CloudPacket packet = clazz.newInstance();
                if (packet instanceof InboundHandler) {
                    packet.decode(stream);

                    ((InboundHandler) packet).onInbound();
                }
            } catch (Exception e) {
                Rubudu.logger.severe("Unhandled packet " + clazz.getSimpleName() + " due to an exception:");
                e.printStackTrace(System.out);
            }
        }
    }
}