package it.bitrule.rubudu.app;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.rubudu.app.profile.controller.ProfileController;
import it.bitrule.rubudu.app.profile.repository.ProfileRepository;
import it.bitrule.rubudu.app.routes.APIKeyInterceptor;
import it.bitrule.rubudu.app.routes.PingRoute;
import it.bitrule.rubudu.app.routes.server.ServerRoutes;
import it.bitrule.rubudu.common.response.ResponseTransformerImpl;
import it.bitrule.rubudu.common.utils.JavaUtils;
import it.bitrule.rubudu.messaging.PublisherRepository;
import it.bitrule.rubudu.messaging.RedisRepository;
import it.bitrule.rubudu.messaging.connection.RedisConnection;
import it.bitrule.rubudu.parties.controller.PartyController;
import it.bitrule.rubudu.quark.controller.GrantsController;
import it.bitrule.rubudu.quark.controller.GroupController;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.util.Timer;
import java.util.TimerTask;

@Getter
public final class App {

    @Getter private final static @NonNull App instance = new App();

    static @Nullable Timer timer = null;

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

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar rubudu.jar <apiKey> <port>");
            System.exit(1);
        }

        String apiKey = args[0];
        Integer port = JavaUtils.parseInt(args[1]);
        if (port == null) {
            System.err.println("Invalid port number: " + args[1]);
            System.exit(1);
        }

        try {
            instance.loadAll(apiKey, port);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTaskImpl(), 1000L, 1000L);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

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

        ProfileRepository.getInstance().loadAll();
        GrantsController.getInstance().loadAll();
        GroupController.getInstance().loadAll();
        PartyController.getInstance().loadAll();

        Spark.get("/api/v1/ping/:id", new PingRoute());

        // api/server/:id/update = POST mean to update the server data
        // api/server/:id/players = GET mean to get all the players on the server
        // api/servers = GET mean to get all the servers

        Spark.get("/server/players", ServerRoutes.GET_ALL, new ResponseTransformerImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Rubudu");

            if (!App.this.running) return;

            App.this.running = false;

            if (timer != null) {
                timer.cancel();

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

    private static class TimerTaskImpl extends TimerTask {
        @Override
        public void run() {
//            ProfileController.getInstance().tick();
            GrantsController.getInstance().tick();
        }
    }
}