package it.bitrule.rubudu.app.routes;

import it.bitrule.rubudu.common.response.Pong;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public final class PingRoute implements Route {

    @Override
    public Object handle(Request request, Response response) {
        String id = request.params(":id");
        if (id == null || id.isEmpty()) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("ID is required"));
        }

        System.out.println("Route: Ping from server with ID: " + id + " received!");

        return new Pong();
    }
}