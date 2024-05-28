package it.bitrule.rubudu.profile;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class PlayerPostUnload {

    private final @NonNull String xuid;
    private final @NonNull String timestamp;
}