package it.bitrule.rubudu.routes.server;

import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.registry.ProfileRegistry;
import lombok.NonNull;
import spark.Route;

public final class ServerRoutes {

    public final static @NonNull Route GET_ALL = (request, response) -> {
        String serverId = request.queryParams("server_id");

        if (serverId == null || serverId.isEmpty()) {
            return ProfileRegistry.getInstance().getProfilesData().stream()
                    .map(ProfileData::getName)
                    .toList();
        }

        return ProfileRegistry.getInstance().getProfilesData().stream()
                .filter(profileData -> profileData.getLastKnownServer().equals(serverId))
                .map(ProfileData::getName)
                .toList();
    };
}