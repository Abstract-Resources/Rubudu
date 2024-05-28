package it.bitrule.rubudu.parties.routes;

import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.messaging.PublisherRepository;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.parties.object.Member;
import it.bitrule.rubudu.parties.object.Party;
import it.bitrule.rubudu.parties.protocol.PartyNetworkDisbandedPacket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

@RequiredArgsConstructor
public final class PartyDeleteRoute implements Route {

    private final @NonNull PublisherRepository publisherRepository;

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

        this.publisherRepository.publish(
                PartyNetworkDisbandedPacket.create(party.getId()),
                true
        );

        return new Pong();
    }
}