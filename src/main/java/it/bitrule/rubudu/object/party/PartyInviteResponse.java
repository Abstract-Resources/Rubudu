package it.bitrule.rubudu.object.party;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor @Data
public final class PartyInviteResponse {

    public enum State {
        SUCCESS, NO_INVITED, NO_ONLINE, NO_PARTY
    }

    private final @Nullable String xuid;
    @SerializedName("known_name")
    private final @NonNull String knownName;

    private final @NonNull State state;

    public static @NonNull PartyInviteResponse successfully(@NonNull String xuid, @NonNull String knownName) {
        return new PartyInviteResponse(xuid, knownName, State.SUCCESS);
    }

    public static @NonNull PartyInviteResponse noInvited(@NonNull String xuid, @NonNull String knownName) {
        return new PartyInviteResponse(xuid, knownName, State.NO_INVITED);
    }

    public static @NonNull PartyInviteResponse noOnline(@NonNull String knownName) {
        return new PartyInviteResponse(null, knownName, State.NO_ONLINE);
    }

    public static @NonNull PartyInviteResponse noParty(@NonNull String xuid, @NonNull String knownName) {
        return new PartyInviteResponse(xuid, knownName, State.NO_PARTY);
    }
}