package it.bitrule.rubudu.app.profile.repository;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.routes.PlayerDisconnectRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerGetRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerJoinRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerSaveRoute;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileRepository {

    @Getter private final static @NonNull ProfileRepository instance = new ProfileRepository();

    private @Nullable Repository<ProfileInfo> profileInfoRepository = null;

    private final @NonNull Map<String, ProfileInfo> cache = new ConcurrentHashMap<>();
    private final @NonNull Map<String, String> cacheXuid = new ConcurrentHashMap<>();
    /**
     * The known servers for the profile
     */
    private final @NonNull Map<String, String> knownServer = new ConcurrentHashMap<>();

    public void loadAll() {
        if (this.profileInfoRepository != null) {
            throw new IllegalStateException("ProfileInfo repository already loaded");
        }

        this.profileInfoRepository = Miwiklark.addRepository(
                ProfileInfo.class,
                "rubudu",
                "profiles"
        );

        Spark.path("/api/v1/player", () -> {
            Spark.post("/:xuid/disconnect", new PlayerDisconnectRoute());
            Spark.post("/:xuid/join/:server_id", new PlayerJoinRoute());
            Spark.post("/:xuid/save", new PlayerSaveRoute());
            Spark.get("/", new PlayerGetRoute());
        });
    }

    /**
     * Cache a profile
     *
     * @param profileInfo The profile to cache
     */
    public void cache(@NonNull ProfileInfo profileInfo) {
        this.cache.put(profileInfo.getIdentifier(), profileInfo);

        if (profileInfo.getName() == null) {
            throw new IllegalArgumentException("ProfileData name cannot be null");
        }

        this.cacheXuid.put(profileInfo.getName().toLowerCase(), profileInfo.getIdentifier());
    }

    /**
     * Clear the profile from the cache
     *
     * @param identifier The identifier of the profile
     */
    public void clearProfile(@NonNull String identifier) {
        this.cache.remove(identifier);
        this.cacheXuid.remove(identifier);
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
     * Get a profile from the cache by its identifier
     * @param identifier The identifier of the profile
     * @return The profile, or null if not found
     */
    public @Nullable ProfileInfo getProfile(@NonNull String identifier) {
        return this.cache.get(identifier);
    }

    /**
     * Get a profile from the cache by its name
     * First we get the xuid from the cache, then we get the profile
     *
     * @param name The name of the profile
     * @return The profile, or null if not found
     */
    public @Nullable ProfileInfo getProfileByName(@NonNull String name) {
        return Optional.ofNullable(this.cacheXuid.get(name.toLowerCase()))
                .map(this::getProfileByName)
                .orElse(null);
    }

    /**
     * Lookup a profile by its xuid in the cache or in the database
     * @param xuid The xuid of the profile
     * @return The profile, or null if not found
     */
    public @Nullable ProfileInfo lookupProfile(@NonNull String xuid) {
        if (this.profileInfoRepository == null) {
            throw new IllegalStateException("ProfileInfo repository not loaded");
        }

        return Optional.ofNullable(this.getProfile(xuid))
                .or(() -> this.profileInfoRepository.findOne(xuid))
                .orElse(null);
    }

    /**
     * Lookup a profile by its name in the cache or in the database
     * @param name The name of the profile
     * @return The profile, or null if not found
     */
    public @Nullable ProfileInfo lookupProfileByName(@NonNull String name) {
        if (this.profileInfoRepository == null) {
            throw new IllegalStateException("ProfileInfo repository not loaded");
        }

        return Optional.ofNullable(this.getProfileByName(name))
                .or(() -> this.profileInfoRepository.findBy(Filters.eq("name", name)))
                .orElse(null);
    }

    /**
     * Update the profile into the mongodb repository
     *
     * @param profileInfo The profile to update
     */
    public void update(@NonNull ProfileInfo profileInfo) {
        if (this.profileInfoRepository == null) {
            throw new IllegalStateException("ProfileInfo repository not loaded");
        }

        this.profileInfoRepository.save(profileInfo);
    }
}