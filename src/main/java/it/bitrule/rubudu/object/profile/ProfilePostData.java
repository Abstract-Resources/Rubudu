package it.bitrule.rubudu.object.profile;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor @Getter
public final class ProfilePostData {

    private final @NonNull String xuid;
    private final @NonNull String name;
    @SerializedName("first_join_date")
    private final @NonNull String firstJoinDate;
    @SerializedName("last_join_date")
    private final @NonNull String lastJoinDate;
    @SerializedName("last_known_server")
    private final @NonNull String lastKnownServer;
}