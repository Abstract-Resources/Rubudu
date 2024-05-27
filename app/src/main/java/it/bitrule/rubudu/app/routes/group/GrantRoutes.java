package rubudu.routes.group;

import it.bitrule.miwiklark.common.Miwiklark;
import rubudu.object.Pong;
import rubudu.object.grant.GrantData;
import rubudu.object.grant.GrantPostUnloadData;
import rubudu.object.grant.GrantsResponseData;
import rubudu.object.profile.ProfileData;
import rubudu.controller.GrantsController;
import rubudu.controller.ProfileController;
import rubudu.response.ResponseTransformerImpl;
import rubudu.utils.JavaUtils;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static rubudu.object.profile.ProfilePostData.*;

public final class GrantRoutes {

    public final static @NonNull Route GET = (request, response) -> {
        String xuid = request.queryParams("xuid");
        String name = request.queryParams("name");

        boolean xuidEmpty = xuid == null || xuid.isEmpty();
        if (xuidEmpty && (name == null || name.isEmpty())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID or name is required"));
        }

        String state = request.queryParams("state");
        if (state == null || state.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        if (!Objects.equals(state, State.ONLINE.name()) && !Objects.equals(state, State.OFFLINE.name())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required and must be either 'online' or 'offline'"));
        }

        ProfileData profileData = xuidEmpty
                ? ProfileController.getInstance().fetchUnsafeByName(name)
                : ProfileController.getInstance().fetchUnsafe(xuid);
        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player with name " + name + " not found"));
        }

        if (profileData.getName() == null) {
            Spark.halt(502, ResponseTransformerImpl.failedResponse("Internal server error, name is not defined"));
        }

        List<GrantData> grantsData = GrantsController.getInstance().fetchUnsafePlayerGrants(profileData.getIdentifier());
        if (state.equalsIgnoreCase(State.ONLINE.name())) {
            GrantsController.getInstance().setPlayerGrants(profileData.getIdentifier(), grantsData);
        }

        return new GrantsResponseData(
                profileData.getIdentifier(),
                profileData.getName(),
                GrantsController.getInstance().getLastFetchTimestamp(profileData.getIdentifier()) != null || state.equalsIgnoreCase(State.ONLINE.name())
                        ? State.ONLINE.name()
                        : State.OFFLINE.name(),
                grantsData.stream()
                        .filter(grantData -> !grantData.isExpired())
                        .toList(),
                grantsData.stream()
                        .filter(GrantData::isExpired)
                        .toList()
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
        if (state == null || state.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        if (!Objects.equals(state, State.ONLINE.name()) && !Objects.equals(state, State.OFFLINE.name())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required and must be either 'online' or 'offline'"));
        }

        GrantData grantPostData = Miwiklark.GSON.fromJson(request.body(), GrantData.class);
        if (grantPostData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        List<GrantData> grantsData = state.equalsIgnoreCase(State.ONLINE.name())
                ? GrantsController.getInstance().getSafePlayerGrants(grantPostData.getSourceXuid())
                : GrantsController.getInstance().fetchUnsafePlayerGrants(grantPostData.getSourceXuid());
        if (grantsData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Grants non loaded after joining the server"));
        }

        if (state.equalsIgnoreCase(State.ONLINE.name())) {
            // Remove the old grant if it exists
            grantsData.removeIf(grantData -> grantData.getIdentifier().equals(grantPostData.getIdentifier()));
            grantsData.add(grantPostData);

            GrantsController.getInstance().setPlayerGrants(grantPostData.getSourceXuid(), grantsData);
        }

        Miwiklark.getRepository(GrantData.class).save(grantPostData);

        return new Pong();
    };

    public final static @NonNull Route POST_UNLOAD = (request, response) -> {
        GrantPostUnloadData grantPostUnloadData = Miwiklark.GSON.fromJson(request.body(), GrantPostUnloadData.class);
        if (grantPostUnloadData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        long timestampLong = JavaUtils.parseDate(grantPostUnloadData.getTimestamp());
        if (timestampLong == -1) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid timestamp"));
        }

        Instant lastFetch = GrantsController.getInstance().getLastFetchTimestamp(grantPostUnloadData.getXuid());
        if (lastFetch != null && lastFetch.toEpochMilli() > timestampLong) {
            Spark.halt(502, ResponseTransformerImpl.failedResponse("Timestamp is older than the last fetch")); // 502 = STATUS_CODE_BAD_GATEWAY
        }

        GrantsController.getInstance().unloadPlayerGrants(grantPostUnloadData.getXuid());

        return new Pong();
    };
}