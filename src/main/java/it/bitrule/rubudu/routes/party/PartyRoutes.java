package it.bitrule.rubudu.routes.party;

import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.object.Pong;
import it.bitrule.rubudu.object.party.Member;
import it.bitrule.rubudu.object.party.Party;
import it.bitrule.rubudu.object.party.Role;
import it.bitrule.rubudu.object.profile.ProfileData;
import it.bitrule.rubudu.registry.PartyRegistry;
import it.bitrule.rubudu.registry.ProfileRegistry;
import it.bitrule.rubudu.repository.protocol.PartyNetworkDisbandedPacket;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

public final class PartyRoutes {

    public final static @NonNull Route GET = (request, response) -> {
        String xuid = request.queryParams("xuid");
        String name = request.queryParams("name");

        boolean xuidEmpty = xuid == null || xuid.isEmpty();
        if (xuidEmpty && (name == null || name.isEmpty())) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID or name is required"));
        }

        if (xuidEmpty) {
            ProfileData profileData = ProfileRegistry.getInstance().getProfileData(name);
            if (profileData == null) {
                Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
            }

            xuid = profileData.getIdentifier();
        }

        Party party = PartyRegistry.getInstance().getPartyByPlayer(xuid);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player is not in a party"));
        }

        return party;
    };

    public final static @NonNull Route POST_JOINED = (request, response) -> {
        String xuid = request.queryParams("xuid");
        String role = request.queryParams("role");
        String id = request.queryParams("id");

        if (xuid == null || xuid.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("XUID is required"));
        }

        if (role == null || role.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Role is required"));
        }

        if (id == null || id.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        Party party = PartyRegistry.getInstance().getPartyById(id);
        if (party == null) {
            PartyRegistry.getInstance().cache(party = new Party(id, false));
        }

        ProfileData profileData = ProfileRegistry.getInstance().getProfileData(xuid);
        if (profileData == null || profileData.getName() == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
        }

        Member member = party.getMember(xuid);
        if (member == null) {
            party.getMembers().add(member = new Member(xuid, profileData.getName(), Role.valueOf(role.toUpperCase())));
            PartyRegistry.getInstance().cacheMember(xuid, id);
        }

        member.setRole(Role.valueOf(role.toUpperCase()));

        return new Pong();
    };

    public final static @NonNull Route DELETE = (request, response) -> {
        String id = request.params(":id");
        if (id == null || id.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        Party party = PartyRegistry.getInstance().remove(id);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Party not found"));
        }

        for (Member member : party.getMembers()) {
            PartyRegistry.getInstance().removeMember(member.getXuid());
        }

        Rubudu.getPublisherRepository().publish(
                PartyNetworkDisbandedPacket.create(party.getId()),
                true
        );

        return new Pong();
    };
}