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
            Spark.halt(400, ResponseTransformerImpl.failedResponse("it.bitrule.rubudu.app.profile.object.State is required"));
        }

        if (!Objects.equals(state, PlayerState.ONLINE.name()) && !Objects.equals(state, PlayerState.OFFLINE.name())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("it.bitrule.rubudu.app.profile.object.State is required and must be either 'online' or 'offline'"));
        }

        ProfileInfo profileInfo = xuidEmpty
                ? ProfileRepository.getInstance().getProfileByName(name)
                : ProfileRepository.getInstance().getProfile(xuid);
        if (profileInfo == null) {
            System.out.println("Profile not is online! Let's try to fetch it from the database");

            profileInfo = xuidEmpty
                    ? ProfileRepository.getInstance().lookupProfileByName(name)
                    : ProfileRepository.getInstance().lookupProfile(xuid);
        }

        if (profileInfo == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        if (state.equalsIgnoreCase(PlayerState.ONLINE.name())) {
            ProfileRepository.getInstance().cache(profileInfo);
        } else if (ProfileRepository.getInstance().getProfile(profileInfo.getIdentifier()) != null) {
            state = PlayerState.ONLINE.name();
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