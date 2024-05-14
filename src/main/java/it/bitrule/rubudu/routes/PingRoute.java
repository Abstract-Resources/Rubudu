package it.bitrule.rubudu.routes;

import it.bitrule.rubudu.api.Pong;
import spark.Request;
import spark.Response;
import spark.Route;

public final class PingRoute implements Route {

    @Override
    public Object handle(Request request, Response response) {
        return new Pong();
    }
}