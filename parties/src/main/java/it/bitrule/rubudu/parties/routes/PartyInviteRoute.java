package it.bitrule.rubudu.parties.routes;

import rubudu.Rubudu;
import rubudu.controller.PartyController;
import rubudu.controller.ProfileController;
import rubudu.object.party.Party;
import rubudu.routes.party.response.InviteResponse;
import rubudu.routes.party.response.InviteResponse.State;
import rubudu.object.profile.ProfileData;
import rubudu.repository.protocol.PartyNetworkInvitedPacket;
import rubudu.response.ResponseTransformerImpl;
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