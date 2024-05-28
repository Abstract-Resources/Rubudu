package it.bitrule.rubudu.app.routes.player;

import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.messaging.PublisherRepository;
import it.bitrule.rubudu.messaging.protocol.PlayerJoinedNetworkPacket;
import it.bitrule.rubudu.profile.GlobalProfile;
import it.bitrule.rubudu.quark.controller.GroupController;
import it.bitrule.rubudu.quark.object.group.GroupData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.Comparator;
import java.util.Objects;

@RequiredArgsConstructor
public final class PlayerJoinRoute implements Route {

    private final @NonNull PublisherRepository publisherRepository;

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
        String xuid = request.params(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        String serverId = request.params(":server");
        if (serverId == null || serverId.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Server ID is required"));
        }

        ProfileInfo profileInfo = ProfileRepository.getInstance().getProfile(xuid);
        if (profileInfo == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
        }

//        List<GrantData> grantsData = GrantsController.getInstance().getSafePlayerGrants(xuid);
//        if (grantsData == null) {
//            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
//        }

        GlobalProfile globalProfile = ProfileRepository.getInstance().getGlobalProfile(xuid).orElse(null);
        if (globalProfile == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
        }

        GroupData highestGroupData = globalProfile.getActiveGrants().stream()
                .map(grantData -> GroupController.getInstance().getGroup(grantData.getGroupId()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(GroupData::getPriority))
                .orElse(null);
        if (highestGroupData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
        }

        String lastKnownServer = globalProfile.getKnownServer();
        if (Objects.equals(lastKnownServer, serverId)) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Player already on this server"));
        }

        String prefix = highestGroupData.getPrefix();
        if (prefix == null) prefix = "&7";

        this.publisherRepository.publish(
                PlayerJoinedNetworkPacket.create(prefix.replace("§", "&") + globalProfile.getName(), serverId, lastKnownServer),
                true
        );

        globalProfile.setKnownServer(serverId);

        return new Pong();
    }
}