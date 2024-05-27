package it.bitrule.rubudu.repository.protocol;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PartyNetworkJoinedPacket extends CloudPacket {

    private @Nullable String partyId = null;
    private @Nullable String targetXuid = null;
    private @Nullable String targetName = null;

    public PartyNetworkJoinedPacket() {
        super(PARTY_NETWORK_JOINED_PACKET);
    }

    @Override
    public void encode(@NonNull CloudByteStream stream) {
        if (this.partyId == null || this.targetXuid == null || this.targetName == null) {
            throw new IllegalStateException("Cannot encode a packet with null fields!");
        }

        stream.writeString(this.partyId);

        stream.writeString(this.targetXuid);
        stream.writeString(this.targetName);
    }

    public static @NonNull PartyNetworkJoinedPacket create(@NonNull String partyId, @NonNull String targetXuid, @NonNull String targetName) {
        PartyNetworkJoinedPacket packet = new PartyNetworkJoinedPacket();

        packet.partyId = partyId;

        packet.targetXuid = targetXuid;
        packet.targetName = targetName;

        return packet;
    }
}