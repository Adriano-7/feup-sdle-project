package org.project.client;

import java.util.InputMismatchException;
import java.util.Scanner;
import org.project.client.database.LocalDB;
import org.project.data_structures.model.ShoppingList;
import org.zeromq.ZThread;

public class Client {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalDB localDB;
    private ShoppingList shoppingList = null;
    private final String username;
    private final CommunicationHandler communicationHandler;

    public Client(String username) {
        this.username = username;
        this.localDB = new LocalDB(username);
        communicationHandler = new CommunicationHandler(username);
        ZThread.start(communicationHandler);
    }

    public static void main(String[] args) {
        String username = getStringFromUser("Enter your username:");

        Client client = new Client(username);

        while (true){
            System.out.println("\n================== Shopping List App ==================\n");

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
            if (client.shoppingList != null) {
                client.updateShoppingList();
                client.synchronizeShoppingList();
                client.saveShoppingListLocally();
                client.shoppingList = null;
            }
        }
    }

    public void createShoppingList() {
        String name = getStringFromUser("Enter the name of the shopping list:");
        shoppingList = new ShoppingList(name);
        System.out.println("Your shopping list has been successfully created with the ID: " + shoppingList.getID());
        synchronizeShoppingList();
        saveShoppingListLocally();
    }

    public void saveShoppingListLocally() {
        if (!shoppingList.isDeleted()) {
            System.out.println("Saving shopping list...");
            localDB.saveShoppingList(this.shoppingList);
        } else {
            localDB.deleteShoppingList(this.shoppingList.getID().toString());
            System.out.println("Shopping list has been deleted. Saving changes...");
        }
    }

    public void synchronizeShoppingList() {
        try {
            communicationHandler.writeShoppingList(this.shoppingList);
            String response = communicationHandler.getResponse();
            if (response.equals("error/server_unavailable")) {
                // Log the issue silently and continue
                System.err.println("Server unavailable. Changes will remain local.");
                return;
            }
            if (response.equals("error/list_deleted")) {
                shoppingList.setDeleted();
                return;
            }
            ShoppingList serverList = communicationHandler.parseShoppingListResponse(response);
            if (serverList != null) {
                System.out.println("Shopping list synchronized with server successfully!");
                this.shoppingList = serverList;
            }
        } catch (InterruptedException e) {
            System.err.println("Failed to synchronize with server: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void searchShoppingList() {
        while (true) {
            String id = getShoppingListIdFromUser();
            try {
                shoppingList = localDB.getShoppingList(id);

                if (shoppingList != null) {
                    synchronizeShoppingList();
                    System.out.println("Shopping List found in local database!");
                    break;
                }
                else {
                    communicationHandler.readShoppingList(id);
                    String response = communicationHandler.getResponse();
                    if (response.equals("error/list_deleted")) {
                        System.out.println("This Shopping List has been deleted.");
                        break;
                    } else if (response.equals("error/list_not_found")) {
                        System.out.println("Shopping List not found. Please try again.\n");
                    }
                    else {
                        shoppingList = communicationHandler.parseShoppingListResponse(response);
                        if (shoppingList != null) {
                            System.out.println("Shopping List found on server!");
                            localDB.saveShoppingList(shoppingList);
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Search operation was interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void deleteShoppingList() {
        if (shoppingList != null) {
            shoppingList.setDeleted();
            try {
                communicationHandler.deleteShoppingList(shoppingList.getID().toString());
                String response = communicationHandler.getResponse();
                if (response.equals("success/deleted")) {
                    System.out.println("Shopping list deleted successfully.");
                } else {
                    System.out.println("Failed to delete shopping list.");
                }
            } catch (InterruptedException e) {
                System.err.println("Failed to delete shopping list: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("No shopping list to delete.");
        }
    }

    public void updateShoppingList() {
        while (true) {
            if (shoppingList.isDeleted()) {
                return;
            }
            System.out.println("\n================== Shopping List App ==================\n");

            System.out.println(shoppingList);
            System.out.println("Select an option:");
            System.out.println("1. Add item to shopping list");
            System.out.println("2. Remove item from shopping list");
            System.out.println("3. Consume item from shopping list");
            System.out.println("4. Delete shopping list");
            System.out.println("5. Synchronize shopping list with server");
            System.out.println("6. Back");
            

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
                    shoppingList.addItem(username, name, quantity);
                    break;
                case 2:
                    name = getStringFromUser("Enter the name of the item:");
                    shoppingList.removeItem(username, name);
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
                    if(getYesNoFromUser("Are you sure? List will be deleted permanently.")) {
                        deleteShoppingList();
                        return;
                    }
                    break;
                case 5:
                    saveShoppingListLocally();
                    synchronizeShoppingList();
                    break;
                case 6:
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

    private boolean getYesNoFromUser(String prompt) {
        while (true) {
            System.out.println(prompt + " (y/n)");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }
}
