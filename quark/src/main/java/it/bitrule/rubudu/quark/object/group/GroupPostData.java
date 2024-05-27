package it.bitrule.rubudu.quark.object.group;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor @Getter
public final class GroupPostData {

    private final @NonNull String id;
    private final @NonNull String name;
    private final int priority;

    private final @Nullable String display;
    private final @Nullable String prefix;
    private final @Nullable String suffix;
    private final @Nullable String color;
}