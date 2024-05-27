package rubudu.repository.protocol;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerJoinedNetworkPacket extends CloudPacket {

    /**
     * The display name of the player
     */
    private @Nullable String displayName;
    /**
     * The server id of the player
     */
    private @Nullable String serverId;
    /**
     * The last server id of the player
     */
    private @Nullable String lasServerId;

    public PlayerJoinedNetworkPacket() {
        super(PLAYER_JOINED_NETWORK_PACKET);
    }

    @Override
    public void encode(@NonNull CloudByteStream stream) {
        if (this.serverId == null || this.displayName == null) {
            throw new IllegalStateException("Cannot encode a packet with null fields!");
        }

        stream.writeString(this.displayName);
        stream.writeString(this.serverId);

        stream.writeStringNullable(this.lasServerId);
    }

    /**
     * Create a new packet
     * @param displayName The display name of the player
     * @param serverId The server id of the player
     * @return The packet
     */
    public static @NonNull PlayerJoinedNetworkPacket create(@NonNull String displayName, @NonNull String serverId, @Nullable String lasServerId) {
        PlayerJoinedNetworkPacket packet = new PlayerJoinedNetworkPacket();

        packet.displayName = displayName;
        packet.serverId = serverId;
        packet.lasServerId = lasServerId;

        return packet;
    }
}