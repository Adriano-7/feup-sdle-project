package org.project.client;

import org.project.model.Item;
import org.project.model.ShoppingList;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    private Scanner scanner = new Scanner(System.in);
    private LocalDB localDB = new LocalDB();
    private ShoppingList shoppingList = null;
    private final UUID userID = UUID.randomUUID();


    public static void main(String[] args) {
        System.out.println("Welcome to the Shopping List App!");

        Client client = new Client();
        Scanner scanner = new Scanner(System.in);
        while (true){
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
            int a = 1;
        }
    }

    public void createShoppingList() {
        System.out.println("Enter the name of the shopping list:");
        String name = scanner.nextLine();
        shoppingList = new ShoppingList(name);
        System.out.println("Your shopping list has been successfully created with the ID: " + shoppingList.getID());
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
            System.out.println("3. Consume item from shopping list");
            System.out.println("4. Back to main menu");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over
            switch (option) {
                case 1:
                    System.out.println("Enter the name of the item:");
                    String name = scanner.nextLine();
                    System.out.println("Enter the quantity of the item:");
                    int quantity = scanner.nextInt();
                    scanner.nextLine();
                    shoppingList.addItem(name, quantity);
                    break;
                case 2:
                    System.out.println("Enter the index of the item:");
                    int index = scanner.nextInt();
                    scanner.nextLine();
                    shoppingList.removeItem(index);
                    break;
                case 3:
                    System.out.println("Enter the index of the item:");
                    // get and int from the user
                    int itemIndex = scanner.nextInt();
                    scanner.nextLine();

                    int a = 1;
                    boolean consumed = shoppingList.consumeItem(itemIndex, userID.toString());
                    if (consumed) {
                        System.out.println("Item consumed successfully!");
                    } else {
                        System.out.println("Item could not be consumed. Please try again.");
                    }
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
