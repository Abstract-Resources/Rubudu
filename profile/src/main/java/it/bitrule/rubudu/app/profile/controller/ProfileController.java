package it.bitrule.rubudu.app.profile.controller;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.routes.PlayerDisconnectRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerGetRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerJoinRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerSaveRoute;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

private final class ProfileController {

    @Getter private final static @NonNull ProfileController instance = new ProfileController();

    private final @NonNull Map<String, ProfileInfo> profilesData = new ConcurrentHashMap<>();
    private final @NonNull Map<String, String> profilesXuid = new ConcurrentHashMap<>();
    /**
     * The known servers for the profile
     */
    private final @NonNull Map<String, String> knownServer = new ConcurrentHashMap<>();
    /**
     * The pending unloads for the profile
     */
    private final @NonNull Map<String, Instant> pendingUnloads = new ConcurrentHashMap<>();

    /**
     * Unload the profile for the given xuid
     * After the given time, the profile will be removed from the cache
     */
    public void tick() {
        for (Map.Entry<String, Instant> entry : this.pendingUnloads.entrySet()) {
            if (entry.getValue().isAfter(Instant.now())) continue;

            this.profilesXuid.remove(entry.getKey());
            this.profilesData.remove(entry.getKey());
            this.pendingUnloads.remove(entry.getKey());
        }
    }

    /**
     * Load the given ProfileData into the cache
     * And remove it from the pending unloads
     *
     * @param profileInfo The ProfileData to load
     */
    public void loadProfileData(@NonNull ProfileInfo profileInfo) {
        this.pendingUnloads.remove(profileInfo.getIdentifier());

        if (this.profilesData.containsKey(profileInfo.getIdentifier())) return;

        String currentName = profileInfo.getName();
        if (currentName == null) {
            throw new IllegalArgumentException("ProfileData name cannot be null");
        }

        this.profilesData.put(profileInfo.getIdentifier(), profileInfo);
        this.profilesXuid.put(currentName.toLowerCase(), profileInfo.getIdentifier());
    }
}