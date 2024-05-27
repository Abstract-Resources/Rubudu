package it.bitrule.rubudu.quark.routes.grant;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.quark.controller.GrantsController;
import it.bitrule.rubudu.quark.object.grant.GrantData;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.List;
import java.util.Objects;

public final class GrantsSaveRoute implements Route {

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
    @Override
    public Object handle(Request request, Response response) throws Exception {
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
    }
}