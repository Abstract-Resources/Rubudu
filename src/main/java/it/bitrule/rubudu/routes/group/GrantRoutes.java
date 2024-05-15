package it.bitrule.rubudu.routes.group;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.api.Pong;
import it.bitrule.rubudu.object.grant.GrantData;
import it.bitrule.rubudu.object.grant.GrantPostUnloadData;
import it.bitrule.rubudu.object.grant.GrantsResponseData;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.registry.GrantRegistry;
import it.bitrule.rubudu.registry.ProfileRegistry;
import it.bitrule.rubudu.routes.player.PlayerRoutes;
import it.bitrule.rubudu.utils.JavaUtils;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class GrantRoutes {

    public final static @NonNull Route GET = (request, response) -> {
        String xuid = request.queryParams("xuid");
        String name = request.queryParams("name");

        boolean xuidEmpty = xuid == null || xuid.isEmpty();
        if (xuidEmpty && (name == null || name.isEmpty())) {
            Spark.halt(400, "XUID or name is required");
        }

        String state = request.queryParams("state");
        if (state == null || state.isEmpty() || (!Objects.equals(state, PlayerRoutes.STATE_ONLINE) && !Objects.equals(state, PlayerRoutes.STATE_OFFLINE))) {
            Spark.halt(400, "State is required and must be either 'online' or 'offline'");
        }

        if (xuidEmpty) {
            ProfileData profileData = ProfileRegistry.getInstance().fetchUnsafeByName(name);
            if (profileData == null) {
                Spark.halt(404, "Player with name " + name + " not found");
            }

            xuid = profileData.getIdentifier();
        }

        List<GrantData> grantsData = GrantRegistry.getInstance().fetchUnsafePlayerGrants(xuid);
        if (state.equalsIgnoreCase(PlayerRoutes.STATE_ONLINE)) {
            GrantRegistry.getInstance().setPlayerGrants(xuid, grantsData);

            Rubudu.logger.log(Level.INFO, "Grants has been cached into our cache for {0}", xuid);
        }

        return new GrantsResponseData(
                xuid,
                grantsData.stream()
                        .filter(grantData -> !grantData.isExpired())
                        .toList(),
                grantsData.stream()
                        .filter(GrantData::isExpired)
                        .toList(),
                GrantRegistry.getInstance().getLastFetchTimestamp(xuid) != null || state.equalsIgnoreCase(PlayerRoutes.STATE_ONLINE)
                        ? PlayerRoutes.STATE_ONLINE
                        : PlayerRoutes.STATE_OFFLINE
        );
    };

    /**
     * POST route for granting a player a group
     * This route requires a JSON body with the following structure:
     * {
     *    "_id": "id",
     *    "source_xuid": "xuid",
     *    "group_id": "group_id",
     *    "created_at": "created_at",
     *    "expires_at": "expires_at",
     *    "who_granted": "who_granted",
     *    "revoked_at": "revoked_at",
     *    "who_revoked": "who_revoked"
     * }
     */
    public static @NonNull Route POST = (request, response) -> {
        String state = request.queryParams("state");
        if (state == null || state.isEmpty() || (!Objects.equals(state, PlayerRoutes.STATE_ONLINE) && !Objects.equals(state, PlayerRoutes.STATE_OFFLINE))) {
            Spark.halt(400, "State is required and must be either 'online' or 'offline'");
        }

        GrantData grantPostData = Miwiklark.GSON.fromJson(request.body(), GrantData.class);
        if (grantPostData == null) {
            Spark.halt(400, "Invalid body");
        }

        List<GrantData> grantsData = state.equalsIgnoreCase(PlayerRoutes.STATE_ONLINE)
                ? GrantRegistry.getInstance().getSafePlayerGrants(grantPostData.getSourceXuid())
                : GrantRegistry.getInstance().fetchUnsafePlayerGrants(grantPostData.getSourceXuid());
        if (grantsData == null) {
            Spark.halt(404, "Player with xuid " + grantPostData.getSourceXuid() + " not found");
        }

        if (state.equalsIgnoreCase(PlayerRoutes.STATE_ONLINE)) {
            // TODO: Remove the old grant if it exists
            grantsData.removeIf(grantData -> grantData.getIdentifier().equals(grantPostData.getIdentifier()));
            grantsData.add(grantPostData);

            GrantRegistry.getInstance().setPlayerGrants(grantPostData.getSourceXuid(), grantsData);
        }

        Miwiklark.getRepository(GrantData.class).save(grantPostData);

        return new Pong();
    };

    public final static @NonNull Route POST_UNLOAD = (request, response) -> {
        GrantPostUnloadData grantPostUnloadData = Miwiklark.GSON.fromJson(request.body(), GrantPostUnloadData.class);
        if (grantPostUnloadData == null) {
            Spark.halt(400, "Invalid body");
        }

        long timestampLong = JavaUtils.parseDate(grantPostUnloadData.getTimestamp());
        if (timestampLong == -1) {
            Spark.halt(400, "Invalid timestamp");
        }

        Instant lastFetch = GrantRegistry.getInstance().getLastFetchTimestamp(grantPostUnloadData.getXuid());
        if (lastFetch != null && lastFetch.toEpochMilli() > timestampLong) {
            Spark.halt(502, "Timestamp is older than the last fetch"); // 502 = STATUS_CODE_BAD_GATEWAY
        }

        GrantRegistry.getInstance().unloadPlayerGrants(grantPostUnloadData.getXuid());

        return new Pong();
    };
}