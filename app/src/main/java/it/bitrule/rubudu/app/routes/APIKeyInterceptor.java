package it.bitrule.rubudu.app.routes;

import it.bitrule.rubudu.app.Rubudu;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public final class APIKeyInterceptor implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        response.type("application/json");

        String apiKey = request.headers("X-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Spark.halt(401, ResponseTransformerImpl.failedResponse("API key is required"));
        }

        if (!apiKey.equals(Rubudu.getInstance().getApiKey())) {
            Spark.halt(403, ResponseTransformerImpl.failedResponse("Unauthorized API key"));
        }

        String contentType = request.contentType();
        if (contentType == null || !contentType.equals("application/json")) {
            Spark.halt(400, ResponseTransformerImpl.failedResponse("No valid content type"));
        }
    }
}