package it.bitrule.rubudu.app.quark.routes.grant;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.PlayerState;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.app.profile.GlobalProfile;
import it.bitrule.rubudu.app.grant.GrantData;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

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
        PlayerState state = PlayerState.parse(request.queryParams("state"));
        if (state == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        GrantData grantPostData = ResponseTransformerImpl.parseJson(request.body(), GrantData.class);
        if (grantPostData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        Miwiklark.getRepository(GrantData.class).save(grantPostData);

        if (state.equals(PlayerState.OFFLINE)) return new Pong();

        GlobalProfile globalProfile = ProfileRepository.getInstance().getGlobalProfile(grantPostData.getSourceXuid()).orElse(null);
        if (globalProfile == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        globalProfile.getActiveGrants().removeIf(grantData -> grantData.getIdentifier().equals(grantPostData.getIdentifier()));
        globalProfile.getActiveGrants().add(grantPostData);

        return new Pong();
    }
}