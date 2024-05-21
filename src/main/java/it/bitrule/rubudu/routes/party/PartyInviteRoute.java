package it.bitrule.rubudu.routes.party;

import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.controller.PartyController;
import it.bitrule.rubudu.controller.ProfileController;
import it.bitrule.rubudu.object.Pong;
import it.bitrule.rubudu.object.party.Member;
import it.bitrule.rubudu.object.party.Party;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.repository.protocol.PartyNetworkInvitedPacket;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PartyInviteRoute implements Route {

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
        String id = request.params(":id");
        if (id == null || id.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        String xuid = request.params(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        Party party = PartyController.getInstance().getPartyById(id);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Party not found"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileData(xuid);
        if (profileData == null || profileData.getName() == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        Member member = party.getMember(xuid);
        if (member != null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Player is already in the party"));
        }

        Rubudu.getPublisherRepository().publish(
                PartyNetworkInvitedPacket.create(party.getId(), xuid, profileData.getName()),
                true
        );

        return new Pong();
    }
}