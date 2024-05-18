package it.bitrule.rubudu.registry;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.profile.ProfileData;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileRegistry {

    @Getter private final static @NonNull ProfileRegistry instance = new ProfileRegistry();

    private final @NonNull Map<String, ProfileData> profilesData = new ConcurrentHashMap<>();
    private final @NonNull Map<String, String> profilesXuid = new ConcurrentHashMap<>();
    /**
     * The known servers for the profile
     */
    private final @NonNull Map<String, String> knownServer = new ConcurrentHashMap<>();
    /**
     * The pending unloads for the profile
     */
    private final @NonNull Map<String, Instant> pendingUnloads = new ConcurrentHashMap<>();

    public void loadAll() {
        Miwiklark.addRepository(
                ProfileData.class,
                "rubudu",
                "profiles"
        );
    }

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
     * @param profileData The ProfileData to load
     */
    public void loadProfileData(@NonNull ProfileData profileData) {
        this.pendingUnloads.remove(profileData.getIdentifier());

        if (this.profilesData.containsKey(profileData.getIdentifier())) return;

        String currentName = profileData.getName();
        if (currentName == null) {
            throw new IllegalArgumentException("ProfileData name cannot be null");
        }

        this.profilesData.put(profileData.getIdentifier(), profileData);
        this.profilesXuid.put(currentName.toLowerCase(), profileData.getIdentifier());
    }

    /**
     * Add time to the pending unload of the given xuid
     * @param xuid The xuid of the player
     */
    public void unloadProfile(@NonNull String xuid) {
        this.pendingUnloads.put(xuid, Instant.now().plusSeconds(120));
    }

    /**
     * Set the known server for the given xuid
     *
     * @param xuid The xuid of the player
     * @param serverId The server id of the player
     */
    public void setPlayerKnownServer(@NonNull String xuid, @NonNull String serverId) {
        this.knownServer.put(xuid, serverId);
    }

    /**
     * Get the known server for the given xuid
     * Usually this server is stored when the server is online
     * The server can change when the player joins a new server
     *
     * @param xuid The xuid of the player
     * @return The known server for the given xuid, or null if not found
     */
    public @Nullable String getPlayerKnownServer(@NonNull String xuid) {
        return this.knownServer.get(xuid);
    }

    /**
     * Get the ProfileData for the given identifier
     * This method is unsafe due to the fact that it does not check the cache before querying the database
     * So we should use this when we don't know if the ProfileData is already in the cache
     *
     * @param identifier The identifier of the ProfileData
     * @return The ProfileData for the given identifier, or null if not found
     */
    public @Nullable ProfileData fetchUnsafe(@NonNull String identifier) {
        return Optional.ofNullable(this.profilesData.get(identifier))
                .or(() -> Miwiklark.getRepository(ProfileData.class).findOne(identifier))
                .orElse(null);
    }

    /**
     * Get the ProfileData for the given identifier
     * This method is safe due to only querying the cache
     *
     * @param identifier The identifier of the ProfileData
     * @return The ProfileData for the given identifier, or null if not found
     */
    public @Nullable ProfileData getProfileData(@NonNull String identifier) {
        return this.profilesData.get(identifier);
    }

    /**
     * Get the ProfileData for the given identifier
     * This method is unsafe due to the fact that it does not check the cache before querying the database
     * So we should use this when we don't know if the ProfileData is already in the cache
     *
     * @param name The name of the player represented by the ProfileData
     * @return The ProfileData for the given identifier, or null if not found
     */
    public @Nullable ProfileData fetchUnsafeByName(@NonNull String name) {
        return Optional.ofNullable(this.profilesXuid.get(name.toLowerCase()))
                .map(this::fetchUnsafe)
                .or(() -> Miwiklark.getRepository(ProfileData.class).findBy(Filters.eq("name", name)))
                .orElse(null);
    }

    public @NonNull Collection<ProfileData> getProfilesData() {
        return this.profilesData.values();
    }
}