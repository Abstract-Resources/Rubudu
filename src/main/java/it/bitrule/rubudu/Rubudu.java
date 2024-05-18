package it.bitrule.rubudu;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.registry.GrantRegistry;
import it.bitrule.rubudu.registry.GroupRegistry;
import it.bitrule.rubudu.registry.ProfileRegistry;
import it.bitrule.rubudu.repository.PublisherRepository;
import it.bitrule.rubudu.repository.RedisRepository;
import it.bitrule.rubudu.repository.connection.RedisConnection;
import it.bitrule.rubudu.response.ResponseTransformerImpl;
import it.bitrule.rubudu.routes.APIKeyInterceptor;
import it.bitrule.rubudu.routes.PingRoute;
import it.bitrule.rubudu.routes.group.GrantRoutes;
import it.bitrule.rubudu.routes.group.GroupRoutes;
import it.bitrule.rubudu.routes.party.PartyRoutes;
import it.bitrule.rubudu.routes.player.PlayerRoutes;
import it.bitrule.rubudu.routes.server.ServerRoutes;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public final class Rubudu {

    @Getter private final static @NonNull Rubudu instance = new Rubudu();

    /**
     * Logger for Rubudu
     */
    public final static @NonNull Logger logger = Logger.getLogger(Rubudu.class.getName());
    /**
     * The publisher repository
     */
    private static @Nullable PublisherRepository publisherRepository = null;

    /**
     * This variable is used to check if the Rubudu instance is running
     */
    private boolean running = false;

    /**
     * The API key for Rubudu
     */
    private @Nullable String apiKey;
    /**
     * The port for Rubudu
     */
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

        RedisConnection redisConnection = new RedisConnection("127.0.0.1", null, 0);
        redisConnection.connect();

        publisherRepository = new PublisherRepository(
                new RedisRepository(redisConnection),
                "rubudu"
        );
        publisherRepository.start();

        ProfileRegistry.getInstance().loadAll();
        GroupRegistry.getInstance().loadAll();
        GrantRegistry.getInstance().loadAll();

        Spark.port(3000);
        Spark.init();

        logger.log(Level.INFO, "Spark listening on port {0}", port);

        Spark.before("/*", new APIKeyInterceptor());

        Spark.path("/api", () -> {
            Spark.get("/ping", new PingRoute(), new ResponseTransformerImpl());

            // This is the section for Profile routes
            Spark.post("/player/unload/:xuid", PlayerRoutes.POST_UNLOAD, new ResponseTransformerImpl());
            Spark.post("/player/joined", PlayerRoutes.POST_JOINED, new ResponseTransformerImpl());
            Spark.post("/player", PlayerRoutes.POST, new ResponseTransformerImpl());
            Spark.get("/player", PlayerRoutes.GET, new ResponseTransformerImpl());

            Spark.post("/parties", PartyRoutes.POST_JOINED, new ResponseTransformerImpl());
            Spark.get("/parties", PartyRoutes.GET, new ResponseTransformerImpl());
            Spark.delete("/parties/:id", PartyRoutes.DELETE, new ResponseTransformerImpl());

            Spark.post("/groups/create", GroupRoutes.POST, new ResponseTransformerImpl());
            Spark.get("/groups", GroupRoutes.GET, new ResponseTransformerImpl());

            Spark.post("/grants/unload", GrantRoutes.POST_UNLOAD, new ResponseTransformerImpl());
            Spark.post("/grants", GrantRoutes.POST, new ResponseTransformerImpl());
            Spark.get("/grants", GrantRoutes.GET, new ResponseTransformerImpl());

            Spark.get("/server/players", ServerRoutes.GET_ALL, new ResponseTransformerImpl());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.log(Level.INFO, "Shutting down Rubudu");

            if (!Rubudu.this.running) return;

            Rubudu.this.running = false;

            if (Loader.timer != null) {
                Loader.timer.cancel();

                logger.log(Level.INFO, "Timer cancelled");
            }

            Spark.awaitStop();
        }));

        this.running = true;
    }

    public static @NonNull PublisherRepository getPublisherRepository() {
        if (publisherRepository == null) {
            throw new IllegalStateException("PublisherRepository not initialized");
        }

        return publisherRepository;
    }
}