package it.bitrule.rubudu.app.profile.routes;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.object.PlayerState;
import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.object.ProfilePostData;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class PlayerSaveRoute implements Route {

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
        PlayerState state = PlayerState.parse(request.queryParams("state"));
        if (state == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required"));
        }

        ProfilePostData profilePostData = ResponseTransformerImpl.parseJson(request.body(), ProfilePostData.class);
        if (profilePostData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("An error occurred while parsing the body"));
        }

        ProfileInfo profileInfo = state.equals(PlayerState.ONLINE)
                ? ProfileRepository.getInstance().getProfile(profilePostData.getXuid())
                : ProfileRepository.getInstance().lookupProfile(profilePostData.getXuid());
        if (profileInfo == null && state.equals(PlayerState.ONLINE)) {
            profileInfo = new ProfileInfo(
                    profilePostData.getXuid(),
                    profilePostData.getFirstJoinDate(),
                    profilePostData.getLastJoinDate(),
                    profilePostData.getLastLogoutDate()
            );
        }

        if (profileInfo == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Storage for the player not found"));
        }

        String currentName = profileInfo.getName();
        if (!Objects.equals(currentName, profilePostData.getName())) {
            if (currentName != null) {
                profileInfo.getKnownAliases().add(currentName);
            }

            profileInfo.setName(profilePostData.getName());
        }

        if (state.equals(PlayerState.ONLINE)) ProfileRepository.getInstance().cache(profileInfo);

        // Save profile data async to avoid blocking the main thread
        // This going to make faster the response to the client
        ProfileInfo finalProfileInfo = profileInfo;
        CompletableFuture.runAsync(() -> Miwiklark.getRepository(ProfileInfo.class).save(finalProfileInfo));

        return new Pong();
    }
}