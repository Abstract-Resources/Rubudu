package it.bitrule.rubudu;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.registry.GroupRegistry;
import it.bitrule.rubudu.registry.ProfileRegistry;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import it.bitrule.rubudu.routes.APIKeyInterceptor;
import it.bitrule.rubudu.routes.PingRoute;
import it.bitrule.rubudu.routes.group.GroupRoutes;
import it.bitrule.rubudu.routes.player.PlayerRoutes;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public final class Rubudu {

    @Getter private final static @NonNull Rubudu instance = new Rubudu();

    public final static @NonNull Logger logger = Logger.getLogger(Rubudu.class.getName());

    private boolean running = false;

    private @Nullable String apiKey;
    private int port = -1;

    public void loadAll(@NonNull String apiKey, int port) {
        if (this.apiKey != null) {
            throw new IllegalStateException("Rubudu already loaded");
        }

        this.apiKey = apiKey;
        this.port = port;

        logger.log(Level.INFO, "Loaded Rubudu with api-key {0} and port {1}", new Object[]{apiKey, port});

        String monguri = "mongodb://hyrium_database:5vXHTO256DIkwJZ@127.0.0.1:27017/";
        if (monguri == null || monguri.isEmpty()) {
            throw new IllegalStateException("MONGODB_URI environment variable not set");
        }

        Miwiklark.authMongo(monguri);

        ProfileRegistry.getInstance().loadAll();
        GroupRegistry.getInstance().loadAll();

        Spark.port(3000);
        Spark.init();

        logger.log(Level.INFO, "Spark listening on port {0}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutting down Rubudu");

            if (!Rubudu.this.running) return;

            Rubudu.this.running = false;

            Spark.awaitStop();
        }));

        this.running = true;

        Spark.before("/*", new APIKeyInterceptor());

        Spark.path("/api", () -> {
            Spark.get("/ping", new PingRoute(), new ResponseTransformerImpl());

            // This is the section for Profile routes
            Spark.get("/players", PlayerRoutes.GET, new ResponseTransformerImpl());
            Spark.post("/players/:xuid", PlayerRoutes.POST, new ResponseTransformerImpl());

            Spark.get("/groups/", GroupRoutes.GET, new ResponseTransformerImpl());
            Spark.get("/groups/create/", GroupRoutes.POST);
        });
    }
}