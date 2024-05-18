package it.bitrule.rubudu.repository.protocol;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PartyNetworkDisbandedPacket extends CloudPacket {

    private @Nullable String id;

    public PartyNetworkDisbandedPacket() {
        super(3);
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