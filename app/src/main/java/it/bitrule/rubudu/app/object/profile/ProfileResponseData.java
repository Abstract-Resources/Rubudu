package rubudu.object.profile;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public final class ProfileResponseData {

    private final @NonNull String xuid;
    /**
     * The name of the profile.
     */
    private @Nullable String name;
    /**
     * The state of the profile.
     */
    private @Nullable String state;

    @SerializedName("known_aliases")
    private final @NonNull List<String> knownAliases;
    /**
     * The known addresses of the profile.
     */
    @SerializedName("known_addresses")
    private final @NonNull List<String> knownAddresses;
    @SerializedName("first_join_date")
    private final @NonNull String firstJoinDate;
    @SerializedName("last_join_date")
    private @NonNull String lastJoinDate;
    @SerializedName("last_logout_date")
    private @NonNull String lastLogoutDate;

    @SerializedName("last_known_server")
    private @Nullable String lastKnownServer;
}