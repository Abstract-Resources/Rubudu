package it.bitrule.rubudu.app.group;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor @Data
public final class GroupData implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;

    private @NonNull String name;
    private int priority;

    private @Nullable String display;
    private @Nullable String prefix;
    private @Nullable String suffix;
    private @Nullable String color;
}