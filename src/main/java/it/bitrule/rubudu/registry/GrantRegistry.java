package it.bitrule.rubudu.registry;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.grant.GrantData;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GrantRegistry {

    @Getter private static final GrantRegistry instance = new GrantRegistry();

    private final @NonNull Map<String, List<GrantData>> playerGrants = new ConcurrentHashMap<>();

    public void loadAll() {
        Miwiklark.addRepository(
                GrantData.class,
                "rubudu",
                "grants"
        );
    }

    /**
     * Get the grants for the given xuid
     *
     * @param xuid The xuid of the player
     * @param grants The grants for the given xuid
     */
    public void setPlayerGrants(@NonNull String xuid, @NonNull List<GrantData> grants) {
        if (this.playerGrants.containsKey(xuid)) return;

        this.playerGrants.put(xuid, grants);
    }

    /**
     * Fetch the grants for the given xuid
     * This method is safe due to we only check the cache
     *
     * @param xuid The xuid of the player
     * @return The grants for the given xuid
     */
    public @Nullable List<GrantData> getSafePlayerGrants(@NonNull String xuid) {
        return this.playerGrants.get(xuid);
    }

    /**
     * Fetch the grants for the given xuid
     * This method non is safe due to we first check the cache before querying the database
     * If none are found in the cache, we query the database on the main thread
     *
     * @param xuid The xuid of the player
     * @return The grants for the given xuid
     */
    public @NonNull List<GrantData> fetchUnsafePlayerGrants(@NonNull String xuid) {
        return Optional.ofNullable(this.playerGrants.get(xuid))
                .or(() -> Optional.of(Miwiklark.getRepository(GrantData.class).findMany(Filters.eq("source_xuid", xuid))))
                .orElse(new ArrayList<>());
    }
}