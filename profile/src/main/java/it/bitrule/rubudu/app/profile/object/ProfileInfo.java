package it.bitrule.rubudu.app.profile.object;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor @Data
public final class ProfileInfo implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * The name of the profile.
     */
    private @Nullable String name;

    @SerializedName("known_aliases")
    private final @NonNull List<String> knownAliases = new ArrayList<>();
    /**
     * The known addresses of the profile.
     */
    @SerializedName("known_addresses")
    private final @NonNull List<String> knownAddresses = new ArrayList<>();

    @SerializedName("first_join_date")
    private final @NonNull String firstJoinDate;
    @SerializedName("last_join_date")
    private @NonNull String lastJoinDate;
    @SerializedName("last_logout_date")
    private @NonNull String lastLogoutDate;

    @SerializedName("last_known_server")
    private @Nullable String lastKnownServer;
}