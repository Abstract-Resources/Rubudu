package rubudu.object.party;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor @Data
public final class Member {

    /**
     * The XUID of the member.
     */
    private final @NonNull String xuid;
    /**
     * The known name of the member.
     */
    @SerializedName("known_name")
    private final @NonNull String knownName;
    /**
     * The role of the member in the party.
     */
    private @NonNull Role role;
}