package it.bitrule.rubudu.app.routes.party;

import rubudu.Rubudu;
import rubudu.controller.PartyController;
import rubudu.controller.ProfileController;
import rubudu.object.party.Member;
import rubudu.object.party.Party;
import rubudu.object.party.Role;
import rubudu.object.profile.ProfileData;
import rubudu.repository.protocol.PartyNetworkJoinedPacket;
import rubudu.response.ResponseTransformerImpl;
import rubudu.routes.party.response.AcceptResponse;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import static rubudu.routes.party.response.AcceptResponse.*;

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
        String targetName = request.params(":name");
        if (targetName == null || targetName.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("'NAME' is required"));
        }

        String selfXuid = request.params(":xuid");
        if (selfXuid == null || selfXuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileData(selfXuid);
        if (profileData == null || profileData.getName() == null) {
            return new AcceptResponse(null, null, State.NO_LOADED, null);
        }

        if (PartyController.getInstance().getPartyByPlayer(selfXuid) != null) {
            return new AcceptResponse(null, null, State.ALREADY_IN_PARTY, null);
        }

        ProfileData targetProfileData = ProfileController.getInstance().getProfileDataByName(targetName);
        if (targetProfileData == null || targetProfileData.getName() == null) {
            return new AcceptResponse(null, targetName, State.NO_ONLINE, null);
        }

        Party party = PartyController.getInstance().getPartyByPlayer(targetProfileData.getIdentifier());
        if (party == null) {
            return new AcceptResponse(
                    targetProfileData.getIdentifier(),
                    targetProfileData.getName(),
                    State.NO_PARTY,
                    null
            );
        }

        Member member = party.getMember(selfXuid);
        if (member != null) {
            return new AcceptResponse(
                    targetProfileData.getIdentifier(),
                    targetProfileData.getName(),
                    State.ALREADY_IN_PARTY,
                    null
            );
        }

        if (!party.isOpen() && !party.getPendingInvites().contains(selfXuid)) {
            return new AcceptResponse(
                    targetProfileData.getIdentifier(),
                    targetProfileData.getName(),
                    State.NO_INVITE,
                    null
            );
        }

        party.getPendingInvites().remove(selfXuid);
        party.getMembers().add(new Member(selfXuid, profileData.getName(), Role.MEMBER));

        PartyController.getInstance().cacheMember(selfXuid, party.getId());

        Rubudu.getPublisherRepository().publish(
                PartyNetworkJoinedPacket.create(party.getId(), selfXuid, profileData.getName()),
                true
        );

        return new AcceptResponse(
                targetProfileData.getIdentifier(),
                targetProfileData.getName(),
                State.SUCCESS,
                party
        );
    }
}