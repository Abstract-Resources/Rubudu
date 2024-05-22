package it.bitrule.rubudu.routes.party;

import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.controller.PartyController;
import it.bitrule.rubudu.controller.ProfileController;
import it.bitrule.rubudu.object.party.Party;
import it.bitrule.rubudu.routes.party.response.InviteResponse;
import it.bitrule.rubudu.routes.party.response.InviteResponse.State;
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

        String playerName = request.params(":name");
        if (playerName == null || playerName.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("NAME is required"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileDataByName(playerName);
        if (profileData == null || profileData.getName() == null) {
            return new InviteResponse(null, playerName, State.NO_ONLINE);
        }

        Party party = PartyController.getInstance().getPartyById(id);
        if (party == null) return new InviteResponse(profileData.getIdentifier(), playerName, State.NO_PARTY);

        if (party.getPendingInvites().contains(profileData.getIdentifier())) {
            return new InviteResponse(profileData.getIdentifier(), playerName, State.ALREADY_INVITED);
        }

        if (PartyController.getInstance().getPartyByPlayer(profileData.getIdentifier()) != null) {
            return new InviteResponse(profileData.getIdentifier(), playerName, State.ALREADY_IN_PARTY);
        }

        Rubudu.getPublisherRepository().publish(
                PartyNetworkInvitedPacket.create(party.getId(), playerName, profileData.getName()),
                true
        );

        return new InviteResponse(profileData.getIdentifier(), playerName, State.SUCCESS);
    }
}