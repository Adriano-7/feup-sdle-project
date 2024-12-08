package org.project;

import org.project.client.Client;
import org.project.server.Server;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please specify 'server' or 'client' as an argument.");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> {
                try {
                    Server.main(new String[]{});
                } catch (Exception e) {
                    System.err.println("Error while running the server: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            case "client" -> {
                Client.main(new String[]{});
            }

            default -> {
                System.out.println("Unknown argument: " + args[0]);
                System.out.println("Use 'server' to run the server or 'client' to run the client.");
            }
        }
    }
}
