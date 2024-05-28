package it.bitrule.rubudu.parties.routes;

import it.bitrule.rubudu.app.profile.object.ProfileInfo;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.parties.object.Party;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

@RequiredArgsConstructor
public final class PartyLookupRoute implements Route {

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

        String type = request.params(":type");
        if (type == null || type.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Type is required"));
        }

        if (!type.equals("name") && !type.equals("xuid")) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid type"));
        }

        if (type.equals("name")) {
            ProfileInfo profileInfo = ProfileRepository.getInstance().getProfileByName(id);
            if (profileInfo == null || profileInfo.getName() == null) {
                Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
            }

            id = profileInfo.getIdentifier();
        }

        Party party = PartyController.getInstance().getPartyByPlayer(id);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player is not in a party"));
        }

        return party;
    }
}