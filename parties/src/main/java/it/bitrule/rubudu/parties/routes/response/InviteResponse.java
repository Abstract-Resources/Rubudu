package it.bitrule.rubudu.parties.routes.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor @Data
public final class InviteResponse {

    public enum State {
        SUCCESS, NO_ONLINE, NO_PARTY, ALREADY_INVITED, ALREADY_IN_PARTY
    }

    private final @Nullable String xuid;
    @SerializedName("known_name")
    private final @NonNull String knownName;

    private final @NonNull State state;
}