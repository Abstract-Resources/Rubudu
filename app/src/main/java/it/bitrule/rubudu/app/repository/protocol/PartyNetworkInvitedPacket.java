package rubudu.repository.protocol;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class PartyNetworkInvitedPacket extends CloudPacket {

    private @Nullable String partyId = null;
    private @Nullable String targetXuid = null;
    private @Nullable String targetName = null;

    public PartyNetworkInvitedPacket() {
        super(PARTY_NETWORK_INVITED_PACKET);
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

    public static @NonNull PartyNetworkInvitedPacket create(@NonNull String partyId, @NonNull String targetXuid, @NonNull String targetName) {
        PartyNetworkInvitedPacket packet = new PartyNetworkInvitedPacket();

        packet.partyId = partyId;

        packet.targetXuid = targetXuid;
        packet.targetName = targetName;

        return packet;
    }
}