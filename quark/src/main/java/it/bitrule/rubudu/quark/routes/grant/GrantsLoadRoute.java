package it.bitrule.rubudu.quark.routes.grant;

import it.bitrule.rubudu.app.profile.object.PlayerState;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.quark.controller.QuarkController;
import it.bitrule.rubudu.quark.object.grant.GrantData;
import it.bitrule.rubudu.quark.object.grant.GrantsResponseData;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.List;
import java.util.Objects;

public final class GrantsLoadRoute implements Route {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws Exception implementation can choose to throw exception
     */
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String id = request.params(":id");
        if (id == null || id.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        String type = request.params(":type");
        if (type == null || type.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Type is required"));
        }

        if (!Objects.equals(type, "xuid") && !Objects.equals(type, "name")) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Type is required and must be either 'xuid' or 'name'"));
        }

        PlayerState state = PlayerState.parse(request.queryParams("state"));
        if (state == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        ProfileInfo profileInfo = type.equals("name")
                ? ProfileRepository.getInstance().lookupProfileByName(id)
                : ProfileRepository.getInstance().lookupProfile(id);
        if (profileInfo == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        if (profileInfo.getName() == null) {
            Spark.halt(502, ResponseTransformerImpl.failedResponse("Internal server error, name is not defined"));
        }

        List<GrantData> grantsData = QuarkController.getInstance().fetchUnsafePlayerGrants(profileInfo.getIdentifier());
        if (state.equals(PlayerState.ONLINE)) {
            QuarkController.getInstance().setPlayerGrants(profileInfo.getIdentifier(), grantsData);
        }

        return new GrantsResponseData(
                profileInfo.getIdentifier(),
                profileInfo.getName(),
                QuarkController.getInstance().getLastFetchTimestamp(profileInfo.getIdentifier()) != null || state.equals(PlayerState.ONLINE)
                        ? PlayerState.ONLINE.name()
                        : PlayerState.OFFLINE.name(),
                grantsData.stream()
                        .filter(grantData -> !grantData.isExpired())
                        .toList(),
                grantsData.stream()
                        .filter(GrantData::isExpired)
                        .toList()
        );
    }
}