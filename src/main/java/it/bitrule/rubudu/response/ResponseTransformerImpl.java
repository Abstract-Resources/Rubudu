package it.bitrule.rubudu.response;

import com.google.gson.JsonElement;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.object.Pong;
import lombok.NonNull;
import spark.ResponseTransformer;

public final class ResponseTransformerImpl implements ResponseTransformer {

    @Override
    public String render(Object o) {
        if (o instanceof JsonElement) return o.toString();

        String json = Miwiklark.GSON.toJson(o);
        if (!(o instanceof Pong)) {
            System.out.println("Returning: " + json);
        }

        return json;
    }

    /**
     * Create a failed response
     * @param message The message of the response
     * @return The failed response
     */
    public static @NonNull String failedResponse(@NonNull String message) {
        return "{\"status\":\"failed\",\"message\":\"" + message + "\"}";
    }
}