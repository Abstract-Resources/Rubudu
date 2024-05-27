package it.bitrule.rubudu.app.routes.party;

import rubudu.object.party.Party;
import rubudu.object.profile.ProfileData;
import rubudu.controller.PartyController;
import rubudu.controller.ProfileController;
import rubudu.response.ResponseTransformerImpl;
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
            ProfileData profileData = ProfileController.getInstance().getProfileData(name);
            if (profileData == null) {
                Spark.halt(404, ResponseTransformerImpl.failedResponse("Player not found"));
            }

            xuid = profileData.getIdentifier();
        }

        Party party = PartyController.getInstance().getPartyByPlayer(xuid);
        if (party == null) {
            Spark.halt(404, ResponseTransformerImpl.failedResponse("Player is not in a party"));
        }

        return party;
    };
}