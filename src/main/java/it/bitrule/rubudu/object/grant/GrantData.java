package it.bitrule.rubudu.object.grant;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor @Data
public final class GrantData implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    @SerializedName("group_id")
    private final @NonNull String groupId;
    @SerializedName("source_xuid")
    private final @NonNull String sourceXuid;
    @SerializedName("created_at")
    private final @NonNull String createdAt;
    @SerializedName("expires_at")
    private final @Nullable String expiresAt;
    @SerializedName("who_granted")
    private final @NonNull String whoGranted;
    @SerializedName("revoked_at")
    private @Nullable String revokedAt;
    @SerializedName("who_revoked")
    private @Nullable String whoRevoked;
    /**
     * The scopes of the grant
     * If the scope starts with 'g=' then it's a group scope
     * If the scope starts with 's=' then it's a server scope
     * Empty scopes mean the grant is global
     */
    private @NonNull List<String> scopes = new ArrayList<>();

    /**
     * Check if the grant is expired
     *
     * @return True if the grant is expired, false otherwise
     */
    public boolean isExpired() {
        // TODO: Fix this because it's not working
        return this.expiresAt != null && System.currentTimeMillis() > Long.parseLong(this.expiresAt);
    }
}