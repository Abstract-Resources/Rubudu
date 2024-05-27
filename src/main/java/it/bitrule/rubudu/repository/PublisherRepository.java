package it.bitrule.rubudu.repository;

import io.netty.buffer.Unpooled;
import it.bitrule.rubudu.repository.protocol.CloudByteStream;
import it.bitrule.rubudu.repository.protocol.CloudPacket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class PublisherRepository {

    private final @NonNull RedisRepository redisRepository;
    private final @NonNull String channelName;

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
                    System.out.println("Failed to publish packet " + packet.getClass().getSimpleName() + " due to an exception:");
                    e.printStackTrace(System.err);
                }
            });
        } else {
            runnable.run();
        }
    }
}