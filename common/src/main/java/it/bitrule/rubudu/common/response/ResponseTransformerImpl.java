package it.bitrule.rubudu.common.response;

import com.google.gson.JsonElement;
import it.bitrule.miwiklark.common.Miwiklark;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
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
     * Parse a json string into a class
     * @param json The json string
     * @param clazz The class to parse into
     * @return The parsed class
     * @param <T> The type of the class
     */
    public static @Nullable <T> T parseJson(@NonNull String json, @NonNull Class<T> clazz) {
        try {
            return Miwiklark.GSON.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return null;
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