package it.bitrule.rubudu;

import it.bitrule.rubudu.utils.JavaUtils;

public final class Loader {

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
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}