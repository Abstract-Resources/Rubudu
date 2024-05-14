package it.bitrule.rubudu.routes.player;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.object.profile.ProfilePostData;
import it.bitrule.rubudu.registry.ProfileRegistry;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.util.Objects;

public final class PlayerRoutes {

    public final static @NonNull Route GET = (request, response) -> {
        String xuid = request.queryParams(":xuid");
        String name = request.queryParams(":name");

        boolean xuidEmpty = xuid == null || xuid.isEmpty();
        if (xuidEmpty && (name == null || name.isEmpty())) {
            Spark.halt(400, "XUID or name is required");
        }

        ProfileData profileData = xuidEmpty
                ? ProfileRegistry.getInstance().fetchUnsafeByName(name)
                : ProfileRegistry.getInstance().fetchUnsafe(xuid);
        if (profileData == null) {
            Spark.halt(404, "Profile not found");
        }

        return profileData;
    };

    public final static @NonNull Route POST = (request, response) -> {
        String xuid = request.queryParams(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, "XUID is required");
        }

        ProfilePostData profilePostData = Miwiklark.GSON.fromJson(request.body(), ProfilePostData.class);
        if (profilePostData == null) {
            Spark.halt(400, "Invalid body");
        }

        ProfileData profileData = ProfileRegistry.getInstance().getProfileData(xuid);
        if (profileData == null) {
            Spark.halt(404, "Profile not found");
        }

        String currentName = profileData.getName();
        if (!Objects.equals(currentName, profilePostData.getName())) {
            if (currentName != null) {
                profileData.getKnownAliases().add(currentName);
            }

            profileData.setName(profilePostData.getName());
        }

        Miwiklark.getRepository(ProfileData.class).save(profileData);

        return profileData;
    };
}