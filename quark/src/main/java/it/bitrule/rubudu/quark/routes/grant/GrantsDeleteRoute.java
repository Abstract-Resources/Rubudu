package it.bitrule.rubudu.quark.routes.grant;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.common.utils.JavaUtils;
import it.bitrule.rubudu.quark.controller.QuarkController;
import it.bitrule.rubudu.quark.object.grant.GrantPostUnloadData;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.time.Instant;

public final class GrantsDeleteRoute implements Route {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws Exception implementation can choose to throw exception
     */
    @Override
    public Object handle(Request request, Response response) throws Exception {
        GrantPostUnloadData grantPostUnloadData = Miwiklark.GSON.fromJson(request.body(), GrantPostUnloadData.class);
        if (grantPostUnloadData == null) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid body"));
        }

        long timestampLong = JavaUtils.parseDate(grantPostUnloadData.getTimestamp());
        if (timestampLong == -1) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("Invalid timestamp"));
        }

        Instant lastFetch = QuarkController.getInstance().getLastFetchTimestamp(grantPostUnloadData.getXuid());
        if (lastFetch != null && lastFetch.toEpochMilli() > timestampLong) {
            Spark.halt(502, ResponseTransformerImpl.failedResponse("Timestamp is older than the last fetch")); // 502 = STATUS_CODE_BAD_GATEWAY
        }

        QuarkController.getInstance().clearGrants(grantPostUnloadData.getXuid());

        return new Pong();
    }
}