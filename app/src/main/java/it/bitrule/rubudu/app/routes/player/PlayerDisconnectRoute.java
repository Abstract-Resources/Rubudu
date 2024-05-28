package it.bitrule.rubudu.app.routes.player;

import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.common.utils.JavaUtils;
import it.bitrule.rubudu.messaging.PublisherRepository;
import it.bitrule.rubudu.messaging.protocol.PlayerDisconnectNetworkPacket;
import it.bitrule.rubudu.app.profile.GlobalProfile;
import it.bitrule.rubudu.app.profile.PlayerPostUnload;
import it.bitrule.rubudu.app.quark.controller.GroupController;
import it.bitrule.rubudu.app.group.GroupData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

@RequiredArgsConstructor
public final class PlayerDisconnectRoute implements Route {

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
        PlayerPostUnload playerPostUnload = ResponseTransformerImpl.parseJson(request.body(), PlayerPostUnload.class);
        if (playerPostUnload == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        long timestampLong = JavaUtils.parseDate(playerPostUnload.getTimestamp());
        if (timestampLong == -1) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid timestamp"));
        }

        GlobalProfile globalProfile = ProfileRepository.getInstance().getGlobalProfile(playerPostUnload.getXuid()).orElse(null);
        if (globalProfile == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        Instant lastRefresh = globalProfile.getLastRefresh();
        if (lastRefresh != null && lastRefresh.toEpochMilli() > timestampLong) {
            Spark.halt(502, ResponseTransformerImpl.failedResponse("Timestamp is older than the last fetch")); // 502 = STATUS_CODE_BAD_GATEWAY
        }

        ProfileRepository.getInstance().clearProfile(globalProfile.getXuid());
        ProfileRepository.getInstance().clearGlobalProfile(globalProfile.getXuid());

        GroupData highestGroupData = globalProfile.getActiveGrants().stream()
                .map(grantData -> GroupController.getInstance().getGroup(grantData.getGroupId()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(GroupData::getPriority))
                .orElse(null);
        if (highestGroupData == null) return new Pong();

        String lastKnownServer = globalProfile.getKnownServer();
        if (lastKnownServer == null) return ResponseTransformerImpl.failedResponse("No known server");

        String prefix = highestGroupData.getPrefix();
        if (prefix == null) prefix = "&7";

        this.publisherRepository.publish(
                PlayerDisconnectNetworkPacket.create(prefix.replace("§", "&") + globalProfile.getName(), lastKnownServer),
                true
        );

        return new Pong();
    }
}