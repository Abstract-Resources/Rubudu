package rubudu.routes.player;

import rubudu.controller.ProfileController;
import rubudu.object.Pong;
import rubudu.response.ResponseTransformerImpl;
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

        ProfileController.getInstance().unloadProfile(xuid);

        return new Pong();
    }
}