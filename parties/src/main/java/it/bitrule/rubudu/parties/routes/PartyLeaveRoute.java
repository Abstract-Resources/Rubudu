package it.bitrule.rubudu.parties.routes;

import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.parties.object.Member;
import it.bitrule.rubudu.parties.object.Party;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PartyLeaveRoute implements Route {

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

        Member member = party.getMember(xuid);
        if (member == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        party.getMembers().remove(member);
        PartyController.getInstance().removeMember(xuid);

        // TODO: Publish leave packet

        return new Pong();
    }
}