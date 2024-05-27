package it.bitrule.rubudu.quark.object.grant;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class GrantPostUnloadData {

    private final @NonNull String xuid;
    private final @NonNull String timestamp;
}