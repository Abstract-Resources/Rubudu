package it.bitrule.rubudu.app.profile.routes;

import it.bitrule.rubudu.app.profile.controller.ProfileController;
import it.bitrule.rubudu.app.profile.object.ProfileData;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PlayerJoinRoute implements Route {

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

        String serverId = request.params(":server_id");
        if (serverId == null || serverId.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Server ID is required"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileData(xuid);
        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
        }

//        List<GrantData> grantsData = GrantsController.getInstance().getSafePlayerGrants(xuid);
//        if (grantsData == null) {
//            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
//        }

//        GroupData highestGroupData = grantsData.stream()
//                .map(grantData -> GroupController.getInstance().getGroup(grantData.getGroupId()))
//                .filter(Objects::nonNull)
//                .max(Comparator.comparingInt(GroupData::getPriority))
//                .orElse(null);
//        if (highestGroupData == null) {
//            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player non loaded"));
//        }
//
//        String lastKnownServer = ProfileController.getInstance().getPlayerKnownServer(xuid);
//        if (Objects.equals(lastKnownServer, serverId)) {
//            Spark.halt(400, ResponseTransformerImpl.failedResponse("Player already joined this server"));
//        }
//
//        String prefix = highestGroupData.getPrefix();
//        if (prefix == null) prefix = "&7";
//
//        Rubudu.getPublisherRepository().publish(
//                PlayerJoinedNetworkPacket.create(prefix.replace("§", "&") + profileData.getName(), serverId, lastKnownServer),
//                true
//        );

        ProfileController.getInstance().setPlayerKnownServer(xuid, serverId);

        return new Pong();
    }
}