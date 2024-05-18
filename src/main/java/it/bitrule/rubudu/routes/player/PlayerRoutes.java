package it.bitrule.rubudu.routes.player;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.object.Pong;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.object.profile.ProfilePostData;
import it.bitrule.rubudu.registry.ProfileRegistry;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class PlayerRoutes {

    public final static @NonNull String STATE_ONLINE = "online";
    public final static @NonNull String STATE_OFFLINE = "offline";

    public final static @NonNull Route GET = (request, response) -> {
        String xuid = request.queryParams("xuid");
        String name = request.queryParams("name");

        boolean xuidEmpty = xuid == null || xuid.isEmpty();
        if (xuidEmpty && (name == null || name.isEmpty())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID or name is required"));
        }

        String state = request.queryParams("state");
        if (state == null || state.isEmpty() || (!Objects.equals(state, STATE_ONLINE) && !Objects.equals(state, STATE_OFFLINE))) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required and must be either 'online' or 'offline'"));
        }

        ProfileData profileData = xuidEmpty
                ? ProfileRegistry.getInstance().fetchUnsafeByName(name)
                : ProfileRegistry.getInstance().fetchUnsafe(xuid);
        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        if (state.equalsIgnoreCase(STATE_ONLINE)) {
            ProfileRegistry.getInstance().loadProfileData(profileData);

            Rubudu.logger.log(Level.INFO, "Forced profile data load for {0}", profileData.getName());
        }

        return profileData;
    };

    public final static @NonNull Route POST = (request, response) -> {
        String state = request.queryParams("state");
        if (state == null || state.isEmpty() || (!Objects.equals(state, STATE_ONLINE) && !Objects.equals(state, STATE_OFFLINE))) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("State is required and must be either 'online' or 'offline'"));
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

        ProfileData profileData = state.equalsIgnoreCase(STATE_ONLINE)
                ? ProfileRegistry.getInstance().getProfileData(profilePostData.getXuid())
                : ProfileRegistry.getInstance().fetchUnsafe(profilePostData.getXuid());
        if (profileData == null && state.equalsIgnoreCase(STATE_ONLINE)) {
            profileData = new ProfileData(
                    profilePostData.getXuid(),
                    profilePostData.getFirstJoinDate(),
                    profilePostData.getLastJoinDate(),
                    profilePostData.getLastKnownServer()
            );
        }

        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        Rubudu.logger.log(Level.INFO, "Updating profile data for {0}", profileData.getName());

        String currentName = profileData.getName();
        if (!Objects.equals(currentName, profilePostData.getName())) {
            if (currentName != null) {
                profileData.getKnownAliases().add(currentName);
            }

            profileData.setName(profilePostData.getName());
        }

        if (state.equalsIgnoreCase(STATE_ONLINE)) {
            ProfileRegistry.getInstance().loadProfileData(profileData);
            Rubudu.logger.log(Level.INFO, "Forced profile data load for {0}", profileData.getName());
        }

        profileData.setLastKnownServer(profilePostData.getLastKnownServer());

        // Save profile data async to avoid blocking the main thread
        // This going to make faster the response to the client
        ProfileData finalProfileData = profileData;
        CompletableFuture.runAsync(() -> Miwiklark.getRepository(ProfileData.class).save(finalProfileData));

        Rubudu.logger.log(Level.INFO, "Updated profile data for {0}", profileData.getName());

        return new Pong();
    };

    public final static @NonNull Route POST_UNLOAD = (request, response) -> {
        String xuid = request.params(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        ProfileRegistry.getInstance().unloadProfile(xuid);

        return new Pong();
    };
}