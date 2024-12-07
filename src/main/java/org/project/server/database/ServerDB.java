package org.project.server.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.project.data_structures.LWWSet;
import org.project.data_structures.LWWSetSerializer;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.model.ShoppingList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class ServerDB {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
            .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
            .create();
    private static final String filePath = "src/main/java/org/project/server/database/db.json";

    private void initializeFile() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, ShoppingList> loadShoppingLists() {
        System.out.println("Loading shopping lists from local database...");

        try (FileReader reader = new FileReader(filePath)) {
            // Read existing shopping lists from the file
            Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
            Map<String, ShoppingList> shoppingLists = gson.fromJson(reader, type);

            // Return the shopping list with the given ID
            return shoppingLists;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveShoppingList(ShoppingList shoppingList) {
        System.out.println("Saving shopping list to local database...");

        try {
            File file = new File(filePath);
            // Read existing shopping lists from the file
            Map<String, ShoppingList> shoppingLists;
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
                shoppingLists = gson.fromJson(reader, type);
            }

            // Add the new shopping list to the existing shopping lists
            shoppingLists.put(shoppingList.getID().toString(), shoppingList);
            
            // Write the updated shopping lists back to the file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(shoppingLists));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}