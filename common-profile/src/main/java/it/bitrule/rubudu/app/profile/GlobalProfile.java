package it.bitrule.rubudu.app.profile;

import it.bitrule.rubudu.app.grant.GrantData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor @Data
public final class GlobalProfile {

    private final @NonNull String xuid;
    private final @NonNull String name;

    private @NonNull PlayerState state;
    private @Nullable String knownServer;

    private final @NonNull List<GrantData> activeGrants;
    private final @NonNull List<String> permissions;

    private @Nullable Instant lastRefresh = null;
}