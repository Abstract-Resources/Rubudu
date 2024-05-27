package it.bitrule.rubudu.quark.controller;

import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.quark.object.grant.GrantData;
import it.bitrule.rubudu.quark.routes.grant.GrantsDeleteRoute;
import it.bitrule.rubudu.quark.routes.grant.GrantsLoadRoute;
import it.bitrule.rubudu.quark.routes.grant.GrantsSaveRoute;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GrantsController {

    @Getter private static final GrantsController instance = new GrantsController();

    /**
     * The grants for the players
     */
    private final @NonNull Map<String, List<GrantData>> playerGrants = new ConcurrentHashMap<>();
    /**
     * The last fetch for the grants
     */
    private final @NonNull Map<String, Instant> lastFetch = new ConcurrentHashMap<>();
    /**
     * The pending unloads for the grants
     */
    private final @NonNull Map<String, Instant> pendingUnloads = new ConcurrentHashMap<>();

    public void loadAll() {
        Miwiklark.addRepository(
                GrantData.class,
                "rubudu",
                "grants"
        );

        Spark.path("/apiv1/grants", () -> {
            Spark.delete("/:xuid/delete", new GrantsDeleteRoute());
            Spark.post("/:xuid/save", new GrantsSaveRoute());
            Spark.get("/:id/xuid", new GrantsLoadRoute());
            Spark.get("/:id/name", new GrantsLoadRoute());
        });

        Spark.post("/groups/create", GroupRoutes.POST, new ResponseTransformerImpl());
        Spark.get("/groups", GroupRoutes.GET, new ResponseTransformerImpl());

        // api/quark/grants/:xuid/delete = DELETE mean to unload the grants from our cache
        // api/quark/grants/:xuid/save = POST mean to make an update to the grants
        // api/quark/grants/:identifier/name = GET mean to get the grants of a player by name
        // api/quark/grants/:identifier/xuid = GET mean to get the grants of a player by xuid
    }

    /**
     * Unload the grants for the given xuid
     * After the given time, the grants will be removed from the cache
     */
    public void tick() {
        for (Map.Entry<String, Instant> entry : this.pendingUnloads.entrySet()) {
            if (entry.getValue().isAfter(Instant.now())) continue;

            this.playerGrants.remove(entry.getKey());
            this.pendingUnloads.remove(entry.getKey());
            this.lastFetch.remove(entry.getKey());
        }
    }

    /**
     * Set the grants for the given xuid
     * And remove it from the pending unloads
     *
     * @param xuid The xuid of the player
     * @param grants The grants for the given xuid
     */
    public void setPlayerGrants(@NonNull String xuid, @NonNull List<GrantData> grants) {
        this.pendingUnloads.remove(xuid);

        this.lastFetch.put(xuid, Instant.now());

        if (this.playerGrants.containsKey(xuid)) return;

        this.playerGrants.put(xuid, grants);
    }

    /**
     * Add time to the pending unload of the given xuid
     * @param xuid The xuid of the player
     */
    public void unloadPlayerGrants(@NonNull String xuid) {
        this.pendingUnloads.put(xuid, Instant.now().plusSeconds(120));
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

    /**
     * Get the last fetch for the given xuid
     * @param xuid The xuid of the player
     * @return The last fetch for the given xuid
     */
    public @Nullable Instant getLastFetchTimestamp(@NonNull String xuid) {
        return this.lastFetch.get(xuid);
    }
}