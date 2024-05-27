package it.bitrule.rubudu.app;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.controller.ProfileController;
import it.bitrule.rubudu.app.routes.APIKeyInterceptor;
import it.bitrule.rubudu.app.routes.PingRoute;
import it.bitrule.rubudu.app.routes.server.ServerRoutes;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.messaging.PublisherRepository;
import it.bitrule.rubudu.messaging.RedisRepository;
import it.bitrule.rubudu.messaging.connection.RedisConnection;
import it.bitrule.rubudu.quark.controller.GrantsController;
import it.bitrule.rubudu.quark.controller.GroupController;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

@Getter
public final class Rubudu {

    @Getter private final static @NonNull Rubudu instance = new Rubudu();

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

        System.out.println("Loaded Rubudu with api-key " + apiKey + " and port " + port);

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

        Spark.port(3000);
        Spark.init();

        System.out.println("Spark listening on port " + port);

        Spark.defaultResponseTransformer(new ResponseTransformerImpl());
        Spark.before("/*", new APIKeyInterceptor());

        ProfileController.getInstance().loadAll();
        GrantsController.getInstance().loadAll();
        GroupController.getInstance().loadAll();

        Spark.path("/api", () -> {
            Spark.get("/ping/:id", new PingRoute()); // This is the ping route

            // This is the section for Party routes
            Spark.post("/parties/:id/transfer/:xuid", new PartyTransferRoute(), new ResponseTransformerImpl());
            Spark.post("/parties/:name/accept/:xuid", new PartyAcceptRoute(), new ResponseTransformerImpl());
            Spark.post("/parties/:id/invite/:name", new PartyInviteRoute(), new ResponseTransformerImpl());
            Spark.post("/parties/:id/create/:xuid", new PartyCreateRoute(), new ResponseTransformerImpl());
            Spark.post("/parties/:id/leave/:xuid", new PartyLeaveRoute(), new ResponseTransformerImpl());
            Spark.post("/parties/:id/kick/:xuid", new PartyKickRoute(), new ResponseTransformerImpl());
            Spark.delete("/parties/:id/delete", new PartyDeleteRoute(), new ResponseTransformerImpl());

            Spark.get("/parties", PartyRoutes.GET, new ResponseTransformerImpl());

            // api/server/:id/update = POST mean to update the server data
            // api/server/:id/players = GET mean to get all the players on the server
            // api/servers = GET mean to get all the servers

            Spark.get("/server/players", ServerRoutes.GET_ALL, new ResponseTransformerImpl());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Rubudu");

            if (!Rubudu.this.running) return;

            Rubudu.this.running = false;

            if (Loader.timer != null) {
                Loader.timer.cancel();

                System.out.println("Timer cancelled");
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