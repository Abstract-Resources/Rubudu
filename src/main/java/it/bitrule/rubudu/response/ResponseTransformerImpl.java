package it.bitrule.rubudu.response;

import com.google.gson.JsonElement;
import it.bitrule.miwiklark.common.Miwiklark;
import spark.ResponseTransformer;

public final class ResponseTransformerImpl implements ResponseTransformer {

    @Override
    public String render(Object o) {
        if (o instanceof JsonElement) return o.toString();

        String json = Miwiklark.GSON.toJson(o);
        System.out.println("Returning: " + json);

        return json;
    }
}