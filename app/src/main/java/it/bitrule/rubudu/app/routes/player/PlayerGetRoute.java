package rubudu.routes.player;

import rubudu.controller.ProfileController;
import rubudu.object.profile.ProfileData;
import rubudu.object.profile.ProfileResponseData;
import rubudu.response.ResponseTransformerImpl;
import rubudu.object.profile.ProfilePostData;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.Objects;

public final class PlayerGetRoute implements Route {

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

        if (!Objects.equals(state, ProfilePostData.State.ONLINE.name()) && !Objects.equals(state, ProfilePostData.State.OFFLINE.name())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required and must be either 'online' or 'offline'"));
        }

        ProfileData profileData = xuidEmpty
                ? ProfileController.getInstance().fetchUnsafeByName(name)
                : ProfileController.getInstance().fetchUnsafe(xuid);
        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        if (ProfileController.getInstance().getProfileData(profileData.getIdentifier()) != null) state = ProfilePostData.State.ONLINE.name();

        if (state.equalsIgnoreCase(ProfilePostData.State.ONLINE.name())) {
            ProfileController.getInstance().loadProfileData(profileData);
        }

        return new ProfileResponseData(
                profileData.getIdentifier(),
                profileData.getName(),
                state,
                profileData.getKnownAliases(),
                profileData.getKnownAddresses(),
                profileData.getFirstJoinDate(),
                profileData.getLastJoinDate(),
                profileData.getLastLogoutDate(),
                profileData.getLastKnownServer()
        );
    }
}