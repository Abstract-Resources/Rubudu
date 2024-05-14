package it.bitrule.rubudu.routes;

import it.bitrule.rubudu.Rubudu;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.logging.Level;

public final class APIKeyInterceptor implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        response.type("application/json");

        String apiKey = request.headers("X-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Rubudu.logger.log(Level.FINER, "API key is required");

            Spark.halt(401, "API key is required");
        }

        if (!apiKey.equals(Rubudu.getInstance().getApiKey())) {
            Rubudu.logger.log(Level.FINER, "Unauthorized API key");

            Spark.halt(403, "Unauthorized API key");
        }

        String contentType = request.contentType();
        if (contentType == null || !contentType.equals("application/json")) {
            Rubudu.logger.log(Level.FINER, "Bad request");

            Spark.halt(400, "Bad request");
        }

        Rubudu.logger.log(Level.FINER, "Accepting request from {0} with API key {1}", new Object[]{request.ip(), apiKey});
    }
}