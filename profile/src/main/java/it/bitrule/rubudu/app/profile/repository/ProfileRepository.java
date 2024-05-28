package it.bitrule.rubudu.app.profile.repository;

import com.google.common.cache.*;
import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.routes.PlayerLookupRoute;
import it.bitrule.rubudu.app.profile.routes.PlayerSaveRoute;
import it.bitrule.rubudu.app.profile.GlobalProfile;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public final class ProfileRepository {

    @Getter private final static @NonNull ProfileRepository instance = new ProfileRepository();

    private final @NonNull Cache<String, ProfileInfo> temporaryCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, ProfileInfo>) notification -> {
                if (notification.getCause() != RemovalCause.EXPIRED) return;

                this.cacheGlobalProfiles.remove(notification.getKey());
            })
            .build();

    /**
     * The repository for the profile info
     */
    private @Nullable Repository<ProfileInfo> profileInfoRepository = null;

    /**
     * The cache of the profiles
     */
    private final @NonNull Map<String, ProfileInfo> cache = new ConcurrentHashMap<>();
    /**
     * The cache of the xuid of the profiles
     */
    private final @NonNull Map<String, String> cacheXuid = new ConcurrentHashMap<>();
    /**
     * The cache of the global profiles
     */
    private final @NonNull Map<String, GlobalProfile> cacheGlobalProfiles = new ConcurrentHashMap<>();

    public void loadAll() {
        if (this.profileInfoRepository != null) {
            throw new IllegalStateException("ProfileInfo repository already loaded");
        }

        this.profileInfoRepository = Miwiklark.addRepository(
                ProfileInfo.class,
                "rubudu",
                "profiles"
        );

        Spark.path("/apiv1/player", () -> {
            Spark.post("/:xuid/save", new PlayerSaveRoute());

            Spark.get("/:id/lookup/:type", new PlayerLookupRoute());
        });
    }

    /**
     * Cache a profile
     *
     * @param profileInfo The profile to cache
     */
    public void cache(@NonNull ProfileInfo profileInfo) {
        this.temporaryCache.invalidate(profileInfo.getIdentifier());

        this.cache.put(profileInfo.getIdentifier(), profileInfo);

        if (profileInfo.getName() == null) {
            throw new IllegalArgumentException("ProfileData name cannot be null");
        }

        this.cacheXuid.put(profileInfo.getName().toLowerCase(), profileInfo.getIdentifier());

        GlobalProfile globalProfile = this.cacheGlobalProfiles.get(profileInfo.getIdentifier());
        if (globalProfile == null) return;

        globalProfile.setLastRefresh(Instant.now());
    }

    /**
     * Clear the profile from the cache
     *
     * @param identifier The identifier of the profile
     */
    public void clearProfile(@NonNull String identifier) {
        ProfileInfo profileInfo = this.cache.remove(identifier);
        if (profileInfo == null) return;

        if (profileInfo.getName() != null) {
            this.cacheXuid.remove(profileInfo.getName().toLowerCase());
        }

        this.temporaryCache.put(identifier, profileInfo);
    }

    /**
     * Cache a global profile
     *
     * @param globalProfile The global profile to cache
     */
    public void cacheGlobalProfile(@NonNull GlobalProfile globalProfile) {
        globalProfile.setLastRefresh(Instant.now());

        this.cacheGlobalProfiles.put(globalProfile.getXuid(), globalProfile);
    }

    /**
     * Clear the global profile from the cache
     *
     * @param xuid The xuid of the global profile
     */
    public void clearGlobalProfile(@NonNull String xuid) {
        this.cacheGlobalProfiles.remove(xuid);
    }

    public @NonNull Optional<GlobalProfile> getGlobalProfile(@NonNull String xuid) {
        return Optional.ofNullable(this.cacheGlobalProfiles.get(xuid));
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
                .map(this::getProfile)
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
     * Get the values of the cache
     * @return The values of the cache
     */
    public @NonNull Collection<ProfileInfo> values() {
        return this.cache.values();
    }
}