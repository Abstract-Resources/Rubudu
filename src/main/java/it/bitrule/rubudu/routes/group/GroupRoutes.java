package it.bitrule.rubudu.routes.group;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.group.GroupData;
import it.bitrule.rubudu.object.group.GroupPostData;
import it.bitrule.rubudu.registry.GroupRegistry;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

public final class GroupRoutes {

    public final static @NonNull Route GET = (request, response) -> GroupRegistry.getInstance().getGroups();

    public final static @NonNull Route POST = (request, response) -> {
        GroupPostData groupPostData = Miwiklark.GSON.fromJson(request.body(), GroupPostData.class);
        if (groupPostData == null) {
            Spark.halt(400, "Invalid body");
        }

        GroupData groupData = GroupRegistry.getInstance().getGroup(groupPostData.getId());
        if (groupData == null) {
            groupData = new GroupData(groupPostData.getId(), groupPostData.getName());
            groupData.setPriority(groupPostData.getPriority());
            groupData.setDisplay(groupPostData.getDisplay());
            groupData.setPrefix(groupPostData.getPrefix());
            groupData.setSuffix(groupPostData.getSuffix());
            groupData.setColor(groupPostData.getColor());

            GroupRegistry.getInstance().addGroup(groupData);
        }

        Miwiklark.getRepository(GroupData.class).save(groupData);

        return null;
    };
}