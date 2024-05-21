package it.bitrule.rubudu.routes.party;

import it.bitrule.rubudu.controller.PartyController;
import it.bitrule.rubudu.controller.ProfileController;
import it.bitrule.rubudu.object.party.Member;
import it.bitrule.rubudu.object.party.Party;
import it.bitrule.rubudu.object.party.Role;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

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

        String xuid = request.params(":xuid");
        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        ProfileData selfProfileData = ProfileController.getInstance().getProfileData(xuid);
        if (selfProfileData == null || selfProfileData.getName() == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileDataByName(targetName);
        if (profileData == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player is not online"));
        }

        Party party = PartyController.getInstance().getPartyByPlayer(profileData.getIdentifier());
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not in party"));
        }

        Member member = party.getMember(xuid);
        if (member != null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Self already in party"));
        }

        if (!party.isOpen() && !party.getPendingInvites().contains(xuid)) {
            // TODO: Return the party without adding the member
            // The client now know the player not has been added to the party
            // So that's mean, never was invited or the invite expired
            return party;
        }

        party.getPendingInvites().remove(xuid);
        party.getMembers().add(new Member(xuid, selfProfileData.getName(), Role.MEMBER));

        PartyController.getInstance().cacheMember(xuid, party.getId());

        // TODO: Publish party update to all members

        return party;
    }
}