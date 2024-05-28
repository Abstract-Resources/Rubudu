package it.bitrule.rubudu.parties.routes;

import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.parties.object.Member;
import it.bitrule.rubudu.parties.object.Party;
import it.bitrule.rubudu.parties.object.Role;
import it.bitrule.rubudu.parties.object.response.AcceptResponse;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import static it.bitrule.rubudu.parties.object.response.AcceptResponse.*;

public final class PartyAcceptRoute implements Route {

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
        String selfXuid = request.params(":xuid");
        if (selfXuid == null || selfXuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        ProfileInfo profileInfo = ProfileRepository.getInstance().getProfile(selfXuid);
        if (profileInfo == null || profileInfo.getName() == null) {
            return new AcceptResponse(null, null, State.NO_LOADED, null);
        }

        if (PartyController.getInstance().getPartyByPlayer(selfXuid) != null) {
            return new AcceptResponse(null, null, State.ALREADY_IN_PARTY, null);
        }

        String targetName = request.params(":name");
        if (targetName == null || targetName.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("'NAME' is required"));
        }

        ProfileInfo ownershipInfo = ProfileRepository.getInstance().getProfileByName(targetName);
        if (ownershipInfo == null || ownershipInfo.getName() == null) {
            return new AcceptResponse(null, targetName, State.NO_ONLINE, null);
        }

        Party party = PartyController.getInstance().getPartyByPlayer(ownershipInfo.getIdentifier());
        if (party == null) {
            return new AcceptResponse(
                    ownershipInfo.getIdentifier(),
                    ownershipInfo.getName(),
                    State.NO_PARTY,
                    null
            );
        }

        Member member = party.getMember(selfXuid);
        if (member != null) {
            return new AcceptResponse(
                    ownershipInfo.getIdentifier(),
                    ownershipInfo.getName(),
                    State.ALREADY_IN_PARTY,
                    null
            );
        }

        if (!party.isOpen() && !party.getPendingInvites().contains(selfXuid)) {
            return new AcceptResponse(
                    ownershipInfo.getIdentifier(),
                    ownershipInfo.getName(),
                    State.NO_INVITE,
                    null
            );
        }

        party.getPendingInvites().remove(selfXuid);
        party.getMembers().add(new Member(selfXuid, profileInfo.getName(), Role.MEMBER));

        PartyController.getInstance().cacheMember(selfXuid, party.getId());

//        Rubudu.getPublisherRepository().publish(
//                PartyNetworkJoinedPacket.create(party.getId(), selfXuid, profileInfo.getName()),
//                true
//        );

        return new AcceptResponse(
                ownershipInfo.getIdentifier(),
                ownershipInfo.getName(),
                State.SUCCESS,
                party
        );
    }
}