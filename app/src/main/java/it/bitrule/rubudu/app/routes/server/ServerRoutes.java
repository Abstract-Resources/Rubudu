package rubudu.routes.server;

import rubudu.object.profile.ProfileData;
import rubudu.controller.ProfileController;
import lombok.NonNull;
import spark.Route;

import java.util.Objects;

public final class ServerRoutes {

    public final static @NonNull Route GET_ALL = (request, response) -> {
        String serverId = request.queryParams("server_id");

        if (serverId == null || serverId.isEmpty()) {
            return ProfileController.getInstance().getProfilesData().stream()
                    .map(ProfileData::getName)
                    .toList();
        }

        return ProfileController.getInstance().getProfilesData().stream()
                .filter(profileData -> Objects.equals(profileData.getLastKnownServer(), serverId))
                .map(ProfileData::getName)
                .toList();
    };
}