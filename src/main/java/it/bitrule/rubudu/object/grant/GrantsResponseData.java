package it.bitrule.rubudu.object.grant;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor @Data
public final class GrantsResponseData {

    private final @NonNull String xuid;
    @SerializedName("active")
    private final @NonNull List<GrantData> activeGrants;
    @SerializedName("expired")
    private final @NonNull List<GrantData> expiredGrants;
    private final @NonNull String state;
}