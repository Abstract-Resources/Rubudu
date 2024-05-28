package it.bitrule.rubudu.parties.routes;

import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.parties.object.Party;
import it.bitrule.rubudu.parties.object.response.InviteResponse;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import static it.bitrule.rubudu.parties.object.response.InviteResponse.*;

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

        ProfileInfo profileInfo = ProfileRepository.getInstance().getProfileByName(playerName);
        if (profileInfo == null || profileInfo.getName() == null) {
            return new InviteResponse(null, playerName, State.NO_ONLINE);
        }

        Party party = PartyController.getInstance().getPartyById(id);
        if (party == null) return new InviteResponse(profileInfo.getIdentifier(), playerName, State.NO_PARTY);

        if (party.getPendingInvites().contains(profileInfo.getIdentifier())) {
            return new InviteResponse(profileInfo.getIdentifier(), playerName, State.ALREADY_INVITED);
        }

        if (PartyController.getInstance().getPartyByPlayer(profileInfo.getIdentifier()) != null) {
            return new InviteResponse(profileInfo.getIdentifier(), playerName, State.ALREADY_IN_PARTY);
        }

//        Rubudu.getPublisherRepository().publish(
//                PartyNetworkInvitedPacket.create(party.getId(), playerName, profileInfo.getName()),
//                true
//        );

        return new InviteResponse(profileInfo.getIdentifier(), playerName, State.SUCCESS);
    }
}