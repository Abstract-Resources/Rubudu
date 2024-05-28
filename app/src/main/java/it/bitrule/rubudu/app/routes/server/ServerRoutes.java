package it.bitrule.rubudu.app.routes.server;

import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.util.Objects;

public final class ServerRoutes {

    public final static @NonNull Route GET_ALL = (request, response) -> {
        String serverId = request.params(":id");
        if (serverId == null || serverId.isEmpty()) {
            Spark.halt(400, "Invalid server id");
        }

        if (serverId.equals("all")) {
            return ProfileRepository.getInstance().values().stream()
                    .map(ProfileInfo::getName)
                    .toList();
        }

        return ProfileRepository.getInstance().values().stream()
                .filter(profileData -> Objects.equals(profileData.getLastKnownServer(), serverId))
                .map(ProfileInfo::getName)
                .toList();
    };
}