package it.bitrule.rubudu.parties.controller;

import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.parties.object.Party;
import it.bitrule.rubudu.parties.routes.*;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyController {

    @Getter private final static @NonNull PartyController instance = new PartyController();

    /**
     * The map of parties.
     * The key is the ID of the party.
     * The value is the party.
     */
    private final @NonNull Map<String, Party> parties = new ConcurrentHashMap<>();
    /**
     * The map of players and their party.
     * The key is the XUID of the player.
     * The value is the ID of the party.
     */
    private final @NonNull Map<String, String> playersParty = new ConcurrentHashMap<>();

    public void loadAll() {
        Spark.path("/api/v1/parties", () -> {
            // This is the section for Party routes
            Spark.post("/:id/transfer/:xuid", new PartyTransferRoute(), new ResponseTransformerImpl());
            Spark.post("/:name/accept/:xuid", new PartyAcceptRoute(), new ResponseTransformerImpl());
            Spark.post("/:id/invite/:name", new PartyInviteRoute(), new ResponseTransformerImpl());
            Spark.post("/:id/create/:xuid", new PartyCreateRoute(), new ResponseTransformerImpl());
            Spark.post("/:id/leave/:xuid", new PartyLeaveRoute(), new ResponseTransformerImpl());
            Spark.post("/:id/kick/:xuid", new PartyKickRoute(), new ResponseTransformerImpl());
            Spark.delete("/:id/delete", new PartyDeleteRoute(), new ResponseTransformerImpl());

            Spark.get("/", PartyRoutes.GET, new ResponseTransformerImpl());
        });
    }

    /**
     * Cache a party.
     *
     * @param party The party to cache.
     */
    public void cache(@NonNull Party party) {
        this.parties.put(party.getId(), party);
    }

    /**
     * Cache the party id of a player.
     *
     * @param xuid The XUID of the player.
     * @param partyId The ID of the party.
     */
    public void cacheMember(@NonNull String xuid, @NonNull String partyId) {
        this.playersParty.put(xuid, partyId);
    }

    /**
     * Remove a party from the registry.
     *
     * @param id The ID of the party.
     */
    public @Nullable Party remove(String id) {
        return this.parties.remove(id);
    }

    public void removeMember(@NonNull String xuid) {
        this.playersParty.remove(xuid);
    }

    /**
     * Get a party by its ID.
     *
     * @param id The ID of the party.
     * @return The party, or null if not found.
     */
    public @Nullable Party getPartyById(@NonNull String id) {
        return this.parties.get(id);
    }

    /**
     * Get a party by a player's XUID.
     *
     * @param xuid The XUID of the player.
     * @return The party, or null if not found.
     */
    public @Nullable Party getPartyByPlayer(@NonNull String xuid) {
        return Optional.ofNullable(this.playersParty.get(xuid))
                .map(this.parties::get)
                .orElse(null);
    }
}