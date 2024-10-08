package it.bitrule.rubudu.app.profile.object;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor @Getter
public final class ProfilePostData {

    private final @NonNull String xuid;
    private final @NonNull String name;

    @SerializedName("known_aliases")
    private final @NonNull List<String> knownAliases;
    @SerializedName("known_addresses")
    private final @NonNull List<String> knownAddresses;

    private final @NonNull String state;
    @SerializedName("first_join_date")
    private final @NonNull String firstJoinDate;
    @SerializedName("last_join_date")
    private final @NonNull String lastJoinDate;
    @SerializedName("last_logout_date")
    private final @NonNull String lastLogoutDate;
}