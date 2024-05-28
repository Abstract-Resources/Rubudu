package it.bitrule.rubudu.app.profile.routes;

import it.bitrule.rubudu.app.profile.controller.ProfileController;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PlayerDisconnectRoute implements Route {

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
        String xuid = request.params(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        ProfileRepository.getInstance().clearProfile(xuid);

        return new Pong();
    }
}