package it.bitrule.rubudu.app.profile;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public enum PlayerState {
    ONLINE, OFFLINE;

    public static @Nullable PlayerState parse(@NonNull String state) {
        return switch (state.toLowerCase()) {
            case "online" -> ONLINE;
            case "offline" -> OFFLINE;
            default -> null;
        };
    }
}