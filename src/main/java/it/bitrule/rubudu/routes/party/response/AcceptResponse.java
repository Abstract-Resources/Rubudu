package it.bitrule.rubudu.routes.party.response;

import com.google.gson.annotations.SerializedName;
import it.bitrule.rubudu.object.party.Party;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public final class AcceptResponse {

    public enum State {
        SUCCESS, NO_ONLINE, NO_LOADED, NO_PARTY, ALREADY_IN_PARTY, NO_INVITE
    }

    private final @Nullable String xuid;
    @SerializedName("known_name")
    private final @Nullable String knownName;

    private final @NonNull State state;

    private final @Nullable Party party;
}