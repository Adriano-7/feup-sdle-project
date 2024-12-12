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
    private final CommunicationHandler communicationHandler;

    public Client(String username) {
        this.username = username;
        this.localDB = new LocalDB(username);
        communicationHandler = new CommunicationHandler("tcp://localhost:5555");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(communicationHandler);
    }

    public static void main(String[] args) {
        String username = getStringFromUser("Enter your username:");

        Client client = new Client(username);

        while (true){
            System.out.println("\n================== Shopping List App ==================\n");
            System.out.println(  "                Server Status: " + (client.communicationHandler.isServerRunning() ? "Online" : "Offline") + "\n");

            System.out.println("\nSelect an option:");
            System.out.println("1. Create a new shopping list");
            System.out.println("2. Search for an existing shopping list");
            System.out.println("3. Exit");

            int option = client.getIntFromUser("Enter your choice:");
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
            client.synchronizeShoppingList();
            client.saveShoppingList();
        }
    }

    public void createShoppingList() {
        String name = getStringFromUser("Enter the name of the shopping list:");
        shoppingList = new ShoppingList(name);
        System.out.println("Your shopping list has been successfully created with the ID: " + shoppingList.getID());
        synchronizeShoppingList();
        saveShoppingList();
    }

    public void saveShoppingList() {
        localDB.saveShoppingList(this.shoppingList);
    }

    public void synchronizeShoppingList() {
        if (communicationHandler.isServerRunning()) {
            try {
                communicationHandler.writeShoppingList(this.shoppingList);
                String response = communicationHandler.getResponse();
                ShoppingList serverList = communicationHandler.parseShoppingListResponse(response);
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
            String id = getShoppingListIdFromUser();

            try {
                shoppingList = localDB.getShoppingList(id);

                if (shoppingList != null && communicationHandler.isServerRunning()) {
                    synchronizeShoppingList();
                    System.out.println("Shopping List found in local database and synchronized with server!");
                    break;
                }
                else if (shoppingList != null) {
                    System.out.println("Shopping List found in local database!");
                    break;
                }
                else {
                    communicationHandler.readShoppingList(id);
                    String response = communicationHandler.getResponse();
                    if (!response.equals("error/list_not_found")) {
                        shoppingList = communicationHandler.parseShoppingListResponse(response);
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
            System.out.println(  "                Server Status: " + (communicationHandler.isServerRunning() ? "Online" : "Offline") + "\n");

            System.out.println(shoppingList);
            System.out.println("Select an option:");
            System.out.println("1. Add item to shopping list");
            System.out.println("2. Remove item from shopping list");
            System.out.println("3. Consume item from shopping list");
            if (communicationHandler.isServerRunning()) {
                System.out.println("4. Synchronize shopping list with server");
                System.out.println("5. Back");
            }
            else {
                System.out.println("4. Back");
            }


            int option = getIntFromUser("Enter your choice:");
            String name;
            switch (option) {
                case 1:
                    name = getStringFromUser("Enter the name of the item:");
                    int quantity;
                    if (shoppingList.hasItem(name)) {
                        System.out.println("Item already exists in the shopping list.");
                        quantity = getIntFromUser("Target quantity will be added by:");
                    } else {
                        System.out.println("Item does not exist in the shopping list. Adding new item.");
                        quantity = getIntFromUser("Enter the quantity:");
                    }
                    shoppingList.addItem(name, quantity);
                    break;
                case 2:
                    name = getStringFromUser("Enter the name of the item:");
                    shoppingList.removeItem(name);
                    break;
                case 3:
                    name = getStringFromUser("Enter the name of the item:");
                    int num = getIntFromUser("How many:");
                    long itemsConsumed = shoppingList.consumeItem(name, username, num);
                    if (itemsConsumed > 0) {
                        System.out.println("Consumed " + itemsConsumed + " items.");
                    } else {
                        System.out.println("Item could not be consumed.");
                    }
                    break;
                case 4:
                    if (communicationHandler.isServerRunning()) {
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

    private static String getStringFromUser(String prompt) {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.println(prompt);
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            if (input.matches("^[0-9].*")) {
                System.out.println("Input cannot start with a number. Please try again.");
                continue;
            }

            if (!input.matches("^[a-zA-Z0-9_]+$")) {
                System.out.println("Input can only contain letters, numbers, and underscores. Please try again.");
                continue;
            }

            return input;
        }
    }
    private String getShoppingListIdFromUser() {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.println("Enter the ID of the shopping list:");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            if (!input.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$")) {
                System.out.println("Input must be a valid UUID. Please try again.");
                continue;
            }

            return input;
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
