package org.project.client;

import org.project.client.database.LocalDB;
import org.project.model.ShoppingList;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalDB localDB;
    private ShoppingList shoppingList = null;
    private final String username;
    private final CommunicationHandler serverHandler;

    public Client(String username) {
        this.username = username;
        this.localDB = new LocalDB(username);
        serverHandler = new CommunicationHandler("tcp://localhost:5555");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(serverHandler);
    }

    public static void main(String[] args) {
        System.out.println("\n================== Shopping List App ==================\n");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username:");
        String username = scanner.nextLine();

        Client client = new Client(username);

        while (true){
            System.out.println(  "                Server Status: " + (client.serverHandler.isServerRunning() ? "Online" : "Offline") + "\n");

            System.out.println("\nSelect an option:");
            System.out.println("1. Create a new shopping list");
            System.out.println("2. Search for an existing shopping list");
            System.out.println("3. Exit");

            int option = scanner.nextInt();
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
            client.saveShoppingList();
        }
    }

    public void createShoppingList() {
        System.out.println("Enter the name of the shopping list:");
        String name = scanner.nextLine();
        shoppingList = new ShoppingList(name);
        System.out.println("Your shopping list has been successfully created with the ID: " + shoppingList.getID());
        saveShoppingList();
    }

    public void saveShoppingList() {
        localDB.saveShoppingList(this.shoppingList);
    }

    public void synchronizeShoppingList() {
        if (serverHandler.isServerRunning()) {
            try {
                serverHandler.writeShoppingList(this.shoppingList);
                String response = serverHandler.getResponse();
                ShoppingList serverList = serverHandler.parseShoppingListResponse(response);
                if (serverList != null) {
                    System.out.println("Shopping list synchronized with server successfully!");
                    this.shoppingList = serverList;
                }
            } catch (InterruptedException e) {
                System.err.println("Failed to synchronize with server: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Server is offline. Cannot synchronize shopping list.");
        }
    }

    public void searchShoppingList() {
        while (true) {
            System.out.println("Enter the ID of the shopping list:");
            String id = scanner.nextLine();

            try {
                shoppingList = localDB.getShoppingList(id);

                if (shoppingList != null) {
                    System.out.println("Shopping List found in local database!");
                    break;
                } else {
                    serverHandler.readShoppingList(id);
                    String response = serverHandler.getResponse();
                    if (!response.equals("error/list_not_found")) {
                        shoppingList = serverHandler.parseShoppingListResponse(response);
                        if (shoppingList != null) {
                            System.out.println("Shopping List found on server!");
                            localDB.saveShoppingList(shoppingList);
                            break;
                        }
                    } else {
                        System.out.println("Shopping List not found. Please try again.\n");
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Search operation was interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void updateShoppingList() {
        while (true) {
            System.out.println("\n================== Shopping List App ==================\n");
            System.out.println(  "                Server Status: " + (serverHandler.isServerRunning() ? "Online" : "Offline") + "\n");

            System.out.println(shoppingList);
            System.out.println("Select an option:");
            System.out.println("1. Add item to shopping list");
            System.out.println("2. Remove item from shopping list");
            System.out.println("3. Consume item from shopping list");
            if (serverHandler.isServerRunning()) {
                System.out.println("4. Synchronize shopping list with server");
                System.out.println("5. Back");
            }
            else {
                System.out.println("4. Back");
            }


            int option = getIntFromUser("Enter your choice:");
            switch (option) {
                case 1:
                    System.out.println("Enter the name of the item:");
                    String name = scanner.nextLine();
                    int quantity;
                    if (shoppingList.hasItem(name)) {
                        System.out.println("Item already exists in the shopping list.");
                        quantity = getIntFromUser("Target quantity will be added by:");
                    } else {
                        quantity = getIntFromUser("Enter the quantity:");
                    }
                    shoppingList.addItem(name, quantity);
                    break;
                case 2:
                    System.out.println("Enter the name of the item:");
                    String id = scanner.nextLine();
                    shoppingList.removeItem(id);
                    break;
                case 3:
                    System.out.println("Enter the name of the item:");
                    String id2 = scanner.nextLine();
                    int num = getIntFromUser("How many:");
                    long itemsConsumed = shoppingList.consumeItem(id2, username, num);
                    if (itemsConsumed > 0) {
                        System.out.println("Consumed " + itemsConsumed + " items.");
                    } else {
                        System.out.println("Item could not be consumed.");
                    }
                    break;
                case 4:
                    if (serverHandler.isServerRunning()) {
                        synchronizeShoppingList();
                        break;
                    }
                    else {
                        return;
                    }
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private int getIntFromUser(String prompt) {
        while (true) {
            System.out.println(prompt);
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                return input;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next();
            }
        }
    }
}
