package it.bitrule.rubudu.parties.protocol;

import it.bitrule.rubudu.messaging.protocol.CloudByteStream;
import it.bitrule.rubudu.messaging.protocol.CloudPacket;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PartyNetworkDisbandedPacket extends CloudPacket {

    private @Nullable String id;

    public PartyNetworkDisbandedPacket() {
        super(PARTY_NETWORK_DISBANDED_PACKET);
    }

    @Override
    public void encode(@NonNull CloudByteStream stream) {
        if (this.id == null) {
            throw new IllegalStateException("ID is null");
        }

        stream.writeString(this.id);
    }

    /**
     * Create a new {@link PartyNetworkDisbandedPacket} from a {@link CloudByteStream}.
     * @param id The ID of the party.
     * @return The packet.
     */
    public static @NonNull PartyNetworkDisbandedPacket create(@NonNull String id) {
        PartyNetworkDisbandedPacket packet = new PartyNetworkDisbandedPacket();
        packet.id = id;

        return packet;
    }
}