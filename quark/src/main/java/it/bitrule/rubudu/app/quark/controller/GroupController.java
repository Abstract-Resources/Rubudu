package it.bitrule.rubudu.app.quark.controller;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.rubudu.app.grant.GrantData;
import it.bitrule.rubudu.app.group.GroupData;
import it.bitrule.rubudu.app.quark.routes.grant.GrantsLookupRoute;
import it.bitrule.rubudu.app.quark.routes.grant.GrantsSaveRoute;
import it.bitrule.rubudu.app.quark.routes.group.GroupRoutes;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class GroupController {

    @Getter private static final GroupController instance = new GroupController();

    private final @NonNull Map<String, GroupData> groups = new HashMap<>();

    public void loadAll() {
        Miwiklark.addRepository(
                GrantData.class,
                "rubudu",
                "grants"
        );

        Repository<GroupData> groupDataRepository = Miwiklark.addRepository(
                GroupData.class,
                "rubudu",
                "groups"
        );

        groupDataRepository.findAll().forEach(this::addGroup);

        System.out.println("Loaded " + this.groups.size() + " group(s)");

        Spark.path("/apiv1/", () -> {
            Spark.post("grants/:xuid/save", new GrantsSaveRoute());
            Spark.get("grants/:id/lookup/:type", new GrantsLookupRoute());

            Spark.post("groups/create", GroupRoutes.POST);
            Spark.get("groups", GroupRoutes.GET);
        });

        // api/quark/grants/:xuid/delete = DELETE mean to unload the grants from our cache
        // api/quark/grants/:xuid/save = POST mean to make an update to the grants
        // api/quark/grants/:identifier/name = GET mean to get the grants of a player by name
        // api/quark/grants/:identifier/xuid = GET mean to get the grants of a player by xuid
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