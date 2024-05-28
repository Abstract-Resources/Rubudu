package it.bitrule.rubudu.quark.controller;

import com.google.common.cache.*;
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
import java.util.concurrent.TimeUnit;

public final class QuarkController {

    @Getter private static final QuarkController instance = new QuarkController();

    /**
     * The grants for the players
     */
    private final @NonNull Map<String, List<GrantData>> cachedGrants = new ConcurrentHashMap<>();

    /**
     * The last fetch for the grants
     */
    private final @NonNull Map<String, Instant> lastFetch = new ConcurrentHashMap<>();

    private final @NonNull Cache<String, Instant> offlineCached = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, Instant>) notification -> {
                if (notification.getCause() != RemovalCause.EXPIRED) return;

                cachedGrants.remove(notification.getKey());
                lastFetch.remove(notification.getKey());

                System.out.println("Removed " + notification.getKey() + " from the cache!");
            })
            .build();

    public void loadAll() {
        Miwiklark.addRepository(
                GrantData.class,
                "rubudu",
                "grants"
        );

        Spark.path("/apiv1/grants/", () -> {
            Spark.delete(":xuid/delete", new GrantsDeleteRoute());
            Spark.post(":xuid/save", new GrantsSaveRoute());
            Spark.get(":id/lookup/:type", new GrantsLoadRoute());
        });

        Spark.post("/groups/create", GroupRoutes.POST, new ResponseTransformerImpl());
        Spark.get("/groups", GroupRoutes.GET, new ResponseTransformerImpl());

        // api/quark/grants/:xuid/delete = DELETE mean to unload the grants from our cache
        // api/quark/grants/:xuid/save = POST mean to make an update to the grants
        // api/quark/grants/:identifier/name = GET mean to get the grants of a player by name
        // api/quark/grants/:identifier/xuid = GET mean to get the grants of a player by xuid
    }

    /**
     * Set the grants for the given xuid
     * And remove it from the pending unloads
     *
     * @param xuid The xuid of the player
     * @param grants The grants for the given xuid
     */
    public void setPlayerGrants(@NonNull String xuid, @NonNull List<GrantData> grants) {
        this.offlineCached.invalidate(xuid);

        this.lastFetch.put(xuid, Instant.now());

        if (this.cachedGrants.containsKey(xuid)) return;

        this.cachedGrants.put(xuid, grants);
    }

    /**
     * Add time to the pending unload of the given xuid
     * @param xuid The xuid of the player
     */
    public void clearGrants(@NonNull String xuid) {
        this.offlineCached.put(xuid, Instant.now());
    }

    /**
     * Fetch the grants for the given xuid
     * This method is safe due to we only check the cache
     *
     * @param xuid The xuid of the player
     * @return The grants for the given xuid
     */
    public @Nullable List<GrantData> getPlayerGrants(@NonNull String xuid) {
        return this.cachedGrants.get(xuid);
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
        return Optional.ofNullable(this.cachedGrants.get(xuid))
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