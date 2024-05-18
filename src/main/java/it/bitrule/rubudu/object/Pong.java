package it.bitrule.rubudu.object;

import lombok.NonNull;
import lombok.ToString;

@ToString
public final class Pong {

    private final @NonNull String message = "Pong";
    private final long timestamp = System.currentTimeMillis();
}