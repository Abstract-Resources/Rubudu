package it.bitrule.rubudu.app.controller;

import rubudu.object.party.Party;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

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