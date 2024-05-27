package rubudu.routes.party;

import rubudu.controller.PartyController;
import rubudu.controller.ProfileController;
import rubudu.object.Pong;
import rubudu.object.party.Member;
import rubudu.object.party.Party;
import rubudu.object.party.Role;
import rubudu.object.profile.ProfileData;
import rubudu.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PartyCreateRoute implements Route {

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
        if (party != null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Party already exists"));
        }

        ProfileData profileData = ProfileController.getInstance().getProfileData(xuid);
        if (profileData == null || profileData.getName() == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        party = new Party(id, false);
        party.getMembers().add(new Member(xuid, profileData.getName(), Role.OWNER));

        PartyController.getInstance().cacheMember(xuid, id);
        PartyController.getInstance().cache(party);

        return new Pong();
    }
}