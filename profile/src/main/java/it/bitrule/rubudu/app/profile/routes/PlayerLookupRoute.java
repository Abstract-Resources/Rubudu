package it.bitrule.rubudu.app.profile.routes;

import it.bitrule.rubudu.app.profile.object.PlayerState;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.object.ProfileResponseData;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PlayerLookupRoute implements Route {

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
        if (id == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        String type = request.params(":type");
        if (type == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Type is required"));
        }

        if (!type.equals("name") && !type.equals("xuid")) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid type"));
        }

        PlayerState state = PlayerState.parse(request.queryParams("state"));
        if (state == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        ProfileInfo profileInfo = type.equals("name")
                ? ProfileRepository.getInstance().getProfileByName(id)
                : ProfileRepository.getInstance().getProfile(id);
        if (profileInfo == null) {
            System.out.println("Profile not is online! Let's try to fetch it from the database");

            profileInfo = type.equals("name")
                    ? ProfileRepository.getInstance().lookupProfileByName(id)
                    : ProfileRepository.getInstance().lookupProfile(id);
        }

        if (profileInfo == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        if (state.equals(PlayerState.ONLINE)) {
            ProfileRepository.getInstance().cache(profileInfo);
        } else if (ProfileRepository.getInstance().getProfile(profileInfo.getIdentifier()) != null) {
            state = PlayerState.ONLINE;
        }

        return new ProfileResponseData(
                profileInfo.getIdentifier(),
                profileInfo.getName(),
                state,
                profileInfo.getKnownAliases(),
                profileInfo.getKnownAddresses(),
                profileInfo.getFirstJoinDate(),
                profileInfo.getLastJoinDate(),
                profileInfo.getLastLogoutDate(),
                profileInfo.getLastKnownServer()
        );
    }
}