package it.bitrule.rubudu.object.grant;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor @Data
public final class GrantsResponseData {

    private final @NonNull String xuid;
    @SerializedName("known_name")
    private final @NonNull String knownName;
    /**
     * The state of the request
     * 'online' means was requested when the user joined
     * 'offline' means was requested manual by a command or something
     */
    private final @NonNull String state;
    @SerializedName("active")
    private final @NonNull List<GrantData> activeGrants;
    @SerializedName("expired")
    private final @NonNull List<GrantData> expiredGrants;
}