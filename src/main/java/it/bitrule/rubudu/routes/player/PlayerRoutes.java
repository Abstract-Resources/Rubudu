package it.bitrule.rubudu.routes.player;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.api.Pong;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.object.profile.ProfilePostData;
import it.bitrule.rubudu.registry.ProfileRegistry;
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
            Spark.halt(400, "XUID or name is required");
        }

        String state = request.queryParams("state");
        if (state == null || state.isEmpty() || (!Objects.equals(state, STATE_ONLINE) && !Objects.equals(state, STATE_OFFLINE))) {
            Spark.halt(400, "State is required and must be either 'online' or 'offline'");
        }

        ProfileData profileData = xuidEmpty
                ? ProfileRegistry.getInstance().fetchUnsafeByName(name)
                : ProfileRegistry.getInstance().fetchUnsafe(xuid);
        if (profileData == null) {
            Spark.halt(404, "Profile not found");
        }

        if (state.equalsIgnoreCase(STATE_ONLINE)) {
            ProfileRegistry.getInstance().loadProfileData(profileData);

            Rubudu.logger.log(Level.INFO, "Forced profile data load for {0}", profileData.getName());
        }

        return profileData;
    };

    public final static @NonNull Route POST = (request, response) -> {
        String force = request.queryParams("force");
        if (force == null || force.isEmpty()) {
            Spark.halt(400, "Force is required");
        }

        boolean forceBool;
        try {
            forceBool = Boolean.parseBoolean(force);
        } catch (Exception e) {
            Spark.halt(400, "Force must be a boolean");
            return null;
        }

        ProfilePostData profilePostData = null;
        try {
            profilePostData = Miwiklark.GSON.fromJson(request.body(), ProfilePostData.class);
            if (profilePostData == null) {
                Spark.halt(400, "Invalid body");
            }
        } catch (Exception e) {
            Spark.halt(500, "Internal server error");
        }

        ProfileData profileData = forceBool
                ? ProfileRegistry.getInstance().fetchUnsafe(profilePostData.getXuid())
                : ProfileRegistry.getInstance().getProfileData(profilePostData.getName());
        if (profileData == null && forceBool) {
            profileData = new ProfileData(profilePostData.getXuid(), "", "", "Lobby1");
        }

        if (profileData == null) {
            Spark.halt(404, "Profile non loaded");
        }

        Rubudu.logger.log(Level.INFO, "Updating profile data for {0}", profileData.getName());

        String currentName = profileData.getName();
        if (!Objects.equals(currentName, profilePostData.getName())) {
            if (currentName != null) {
                profileData.getKnownAliases().add(currentName);
            }

            profileData.setName(profilePostData.getName());
        }

        if (forceBool) {
            System.out.println("Loading profile data for " + profileData.getName());
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
            Spark.halt(400, "XUID is required");
        }

        ProfileRegistry.getInstance().unloadProfile(xuid);

        return new Pong();
    };
}