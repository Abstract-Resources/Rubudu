package it.bitrule.rubudu.registry;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.profile.ProfileData;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileRegistry {

    @Getter private final static @NonNull ProfileRegistry instance = new ProfileRegistry();

    private final @NonNull Map<String, ProfileData> profilesData = new ConcurrentHashMap<>();
    private final @NonNull Map<String, String> profilesXuid = new ConcurrentHashMap<>();

    public void loadAll() {
        Miwiklark.addRepository(
                ProfileData.class,
                "rubudu",
                "profiles"
        );
    }

    public void loadProfileData(@NonNull ProfileData profileData) {
        if (this.profilesData.containsKey(profileData.getIdentifier())) return;

        String currentName = profileData.getName();
        if (currentName == null) {
            throw new IllegalArgumentException("ProfileData name cannot be null");
        }

        this.profilesData.put(profileData.getIdentifier(), profileData);
        this.profilesXuid.put(currentName.toLowerCase(), profileData.getIdentifier());
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