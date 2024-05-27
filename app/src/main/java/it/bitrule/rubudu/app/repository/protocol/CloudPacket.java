package rubudu.repository.protocol;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CloudPacket {

    public final static int PLAYER_JOINED_NETWORK_PACKET = 1;
    public final static int PARTY_NETWORK_INVITED_PACKET = 2;
    public final static int PARTY_NETWORK_DISBANDED_PACKET = 3;
    public final static int PARTY_NETWORK_JOINED_PACKET = 4;

    private final @NonNull Integer pid;

    public void encode(@NonNull CloudByteStream stream) {
        throw new IllegalCallerException("This packet cannot be encoded!");
    }

    public void decode(@NonNull CloudByteStream stream) {
        throw new IllegalCallerException("This packet cannot be decoded!");
    }

    public @NonNull Integer pid() {
        return this.pid;
    }
}