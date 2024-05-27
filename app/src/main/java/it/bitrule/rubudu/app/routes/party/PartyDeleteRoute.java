package it.bitrule.rubudu.app.routes.party;

import rubudu.Rubudu;
import rubudu.controller.PartyController;
import rubudu.object.party.Member;
import rubudu.object.party.Party;
import rubudu.repository.protocol.PartyNetworkDisbandedPacket;
import rubudu.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PartyDeleteRoute implements Route {

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

        Party party = PartyController.getInstance().remove(id);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Party not found"));
        }

        for (Member member : party.getMembers()) {
            PartyController.getInstance().removeMember(member.getXuid());
        }

        Rubudu.getPublisherRepository().publish(
                PartyNetworkDisbandedPacket.create(party.getId()),
                true
        );

        return new Pong();
    }
}