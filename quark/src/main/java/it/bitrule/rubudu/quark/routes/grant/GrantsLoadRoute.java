package it.bitrule.rubudu.quark.routes.grant;

import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.quark.controller.GrantsController;
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
    }
}