package it.bitrule.rubudu.object.party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor @Data
public final class Party {

    /**
     * The ID of the party.
     */
    private final @NonNull String id;
    /**
     * If the party is open.
     */
    private boolean open;
    /**
     * The members of the party.
     */
    private final @NonNull List<Member> members = new ArrayList<>();

    public @Nullable Member getMember(@NonNull String xuid) {
        for (Member member : this.members) {
            if (!member.getXuid().equals(xuid)) continue;

            return member;
        }

        return null;
    }
}