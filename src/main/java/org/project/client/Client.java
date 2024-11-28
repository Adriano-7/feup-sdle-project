package org.project.client;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.project.model.ShoppingList;


public class Client {
    Scanner scanner = new Scanner(System.in);
    LocalDB localDB = new LocalDB();
    ShoppingList shoppingList = null;


    public static void main(String[] args) {
        System.out.println("Welcome to the Shopping List App!");
        System.out.println("Select an option:");
        System.out.println("1. Create a new shopping list");
        System.out.println("2. Search for an existing shopping list");
        System.out.println("3. Exit");

        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();
        Client client = new Client();
        switch (option) {
            case 1:
                client.createShoppingList();
                break;
            case 2:
                client.searchShoppingList();
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }

        client.updateShoppingList();
    }

    public void createShoppingList() {
        System.out.println("Enter the name of the shopping list:");
        String name = scanner.nextLine();
        UUID uuid = UUID.randomUUID();
        shoppingList = new ShoppingList(uuid, name, List.of());

        System.out.println("Your shopping list has been successfully created with the ID: " + uuid.toString());

        localDB.saveShoppingList(shoppingList);
    }

    public void searchShoppingList() {
        System.out.println("Enter the ID of the shopping list:");
        String id = scanner.nextLine();
        shoppingList = localDB.getShoppingList(id);

        if (shoppingList != null) {
            System.out.println("Shopping List found!");
        } else {
            System.out.println("Shopping List not found. Please try again.");
        }
    }

    public void updateShoppingList() {
        while (true) {
            System.out.println(shoppingList);
            System.out.println("Select an option:");
            System.out.println("1. Add item to shopping list");
            System.out.println("2. Remove item from shopping list");
            System.out.println("3. Exit");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over
            switch (option) {
                case 1:
                    System.out.println("Enter the name of the item:");
                    String name = scanner.nextLine();
                    System.out.println("Enter the quantity of the item:");
                    int quantity = scanner.nextInt();
                    scanner.nextLine(); // Consume newline left-over
                    shoppingList.addItem(name, quantity);
                    break;
                case 2:
                    System.out.println("Enter the index of the item:");
                    int index = scanner.nextInt();
                    scanner.nextLine(); // Consume newline left-over
                    shoppingList.removeItem(index);
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

    }

    /*public static void xmq(String[] args)
    {
        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Collecting updates from weather server");
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");

            //  Subscribe to zipcode, default is NYC, 10001
            String filter = (args.length > 0) ? args[0] : "10001 ";
            subscriber.subscribe(filter.getBytes(ZMQ.CHARSET));

            //  Process 100 updates
            int update_nbr;
            long total_temp = 0;
            for (update_nbr = 0; update_nbr < 100; update_nbr++) {
                //  Use trim to remove the tailing '0' character
                String string = subscriber.recvStr(0).trim();

                StringTokenizer sscanf = new StringTokenizer(string, " ");
                int zipcode = Integer.valueOf(sscanf.nextToken());
                int temperature = Integer.valueOf(sscanf.nextToken());
                int relhumidity = Integer.valueOf(sscanf.nextToken());

                total_temp += temperature;
            }

            System.out.println(
                String.format(
                    "Average temperature for zipcode '%s' was %d.",
                    filter,
                    (int)(total_temp / update_nbr)
                )
            );
        }
    }*/
}
