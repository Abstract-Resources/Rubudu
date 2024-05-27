package it.bitrule.rubudu;

import it.bitrule.rubudu.controller.GrantsController;
import it.bitrule.rubudu.utils.JavaUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public final class Loader {

    static @Nullable Timer timer = null;

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
            Rubudu.getInstance().loadAll(apiKey, port);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTaskImpl(), 1000L, 1000L);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static class TimerTaskImpl extends TimerTask {
        @Override
        public void run() {
            GrantsController.getInstance().tick();
        }
    }
}