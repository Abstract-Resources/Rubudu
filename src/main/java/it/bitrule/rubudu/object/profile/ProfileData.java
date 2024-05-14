package it.bitrule.rubudu.object.profile;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor @Data
public final class ProfileData implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * The name of the profile.
     */
    private @Nullable String name;

    @SerializedName("known_aliases")
    private final @NonNull List<String> knownAliases = new ArrayList<>();
    @SerializedName("first_join_date")
    private final @NonNull String firstJoinDate;
    @SerializedName("last_join_date")
    private @NonNull String lastJoinDate;
    @SerializedName("last_server_name")
    private @NonNull String lastServerName;
}