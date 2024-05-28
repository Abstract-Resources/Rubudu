package it.bitrule.rubudu.messaging.protocol;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerDisconnectNetworkPacket extends CloudPacket {

    /**
     * The display name of the player
     */
    private @Nullable String displayName;
    /**
     * The server id of the player
     */
    private @Nullable String serverId;

    public PlayerDisconnectNetworkPacket() {
        super(PLAYER_DISCONNECT_NETWORK_PACKET);
    }

    @Override
    public void encode(@NonNull CloudByteStream stream) {
        if (this.serverId == null || this.displayName == null) {
            throw new IllegalStateException("Cannot encode a packet with null fields!");
        }

        stream.writeString(this.displayName);
        stream.writeString(this.serverId);
    }

    /**
     * Create a new packet
     * @param displayName The display name of the player
     * @param serverId The server id of the player
     * @return The packet
     */
    public static @NonNull PlayerDisconnectNetworkPacket create(@NonNull String displayName, @NonNull String serverId) {
        PlayerDisconnectNetworkPacket packet = new PlayerDisconnectNetworkPacket();

        packet.displayName = displayName;
        packet.serverId = serverId;

        return packet;
    }
}