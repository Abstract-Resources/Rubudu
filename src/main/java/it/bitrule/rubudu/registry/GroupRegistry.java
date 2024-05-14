package it.bitrule.rubudu.registry;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.rubudu.Rubudu;
import it.bitrule.rubudu.object.group.GroupData;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class GroupRegistry {

    @Getter private static final GroupRegistry instance = new GroupRegistry();

    private final @NonNull Map<String, GroupData> groups = new HashMap<>();

    public void loadAll() {
        Repository<GroupData> groupDataRepository = Miwiklark.addRepository(
                GroupData.class,
                "rubudu",
                "groups"
        );

        groupDataRepository.findAll().forEach(this::addGroup);

        Rubudu.logger.info("Loaded " + this.groups.size() + " groups");
    }

    /**
     * Add a GroupData to the registry
     * @param group The GroupData to add
     */
    public void addGroup(@NonNull GroupData group) {
        this.groups.put(group.getIdentifier(), group);
    }

    /**
     * Get the GroupData for the given identifier
     * @param id The identifier of the GroupData
     * @return The GroupData for the given identifier, or null if not found
     */
    public @Nullable GroupData getGroup(@NonNull String id) {
        return this.groups.get(id);
    }

    public @NonNull Collection<GroupData> getGroups() {
        return this.groups.values();
    }
}