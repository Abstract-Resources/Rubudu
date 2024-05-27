package it.bitrule.rubudu.app.profile.routes;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.controller.ProfileController;
import it.bitrule.rubudu.app.profile.object.PlayerState;
import it.bitrule.rubudu.app.profile.object.ProfileData;
import it.bitrule.rubudu.app.profile.object.ProfilePostData;
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
        String state = request.queryParams("state");
        if (state == null || state.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("PlayerState is required"));
        }

        if (!Objects.equals(state, PlayerState.ONLINE.name()) && !Objects.equals(state, PlayerState.OFFLINE.name())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("it.bitrule.rubudu.app.profile.object.State is required and must be either 'online' or 'offline'"));
        }

        ProfilePostData profilePostData = null;
        try {
            profilePostData = Miwiklark.GSON.fromJson(request.body(), ProfilePostData.class);
        } catch (Exception e) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        if (profilePostData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        ProfileData profileData = state.equalsIgnoreCase(PlayerState.ONLINE.name())
                ? ProfileController.getInstance().getProfileData(profilePostData.getXuid())
                : ProfileController.getInstance().fetchUnsafe(profilePostData.getXuid());
        if (profileData == null && state.equalsIgnoreCase(PlayerState.ONLINE.name())) {
            profileData = new ProfileData(
                    profilePostData.getXuid(),
                    profilePostData.getFirstJoinDate(),
                    profilePostData.getLastJoinDate(),
                    profilePostData.getLastLogoutDate()
            );
        }

        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        String currentName = profileData.getName();
        if (!Objects.equals(currentName, profilePostData.getName())) {
            if (currentName != null) {
                profileData.getKnownAliases().add(currentName);
            }

            profileData.setName(profilePostData.getName());
        }

        if (state.equalsIgnoreCase(PlayerState.ONLINE.name())) {
            ProfileController.getInstance().loadProfileData(profileData);
        }

        // Save profile data async to avoid blocking the main thread
        // This going to make faster the response to the client
        ProfileData finalProfileData = profileData;
        CompletableFuture.runAsync(() -> Miwiklark.getRepository(ProfileData.class).save(finalProfileData));

        return new Pong();
    }
}