package it.bitrule.rubudu.quark.routes.group;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.quark.controller.GroupController;
import it.bitrule.rubudu.quark.object.group.GroupData;
import it.bitrule.rubudu.quark.object.group.GroupPostData;
import lombok.NonNull;
import spark.Route;
import spark.Spark;

import java.util.concurrent.CompletableFuture;

public final class GroupRoutes {

    public final static @NonNull Route GET = (request, response) -> GroupController.getInstance().getGroups();

    public final static @NonNull Route POST = (request, response) -> {
        GroupPostData groupPostData = ResponseTransformerImpl.parseJson(request.body(), GroupPostData.class);
        if (groupPostData == null) {
            Spark.halt(400, "Invalid body");
        }

        GroupData groupData = GroupController.getInstance().getGroup(groupPostData.getId());
        if (groupData == null) {
            GroupController.getInstance().addGroup(groupData = new GroupData(groupPostData.getId(), groupPostData.getName()));
        }

        groupData.setPriority(groupPostData.getPriority());
        groupData.setDisplay(groupPostData.getDisplay());
        groupData.setPrefix(groupPostData.getPrefix());
        groupData.setSuffix(groupPostData.getSuffix());
        groupData.setColor(groupPostData.getColor());

        GroupData finalGroupData = groupData;
        CompletableFuture.runAsync(() -> Miwiklark.getRepository(GroupData.class).save(finalGroupData));

        return new Pong();
    };
}