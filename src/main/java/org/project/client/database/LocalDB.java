package org.project.client.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.project.data_structures.ShoppingListDeserializer;
import org.project.data_structures.test.AWORSet;
import org.project.data_structures.test.AWORSetSerializer;
import org.project.model.ShoppingList;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LocalDB {
    private final Gson gson;
    private final String filePath;

    public LocalDB(String username) {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(AWORSet.class, new AWORSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .create();
        this.filePath = "src/main/java/org/project/client/database/storage/" + username + ".json";
        this.initializeFile();
    }

    private void initializeFile() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ShoppingList getShoppingList(String id) {
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
            Map<String, ShoppingList> shoppingLists = gson.fromJson(reader, type);

            if(shoppingLists != null && shoppingLists.containsKey(id)){
                return shoppingLists.get(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveShoppingList(ShoppingList shoppingList) {
        try {
            File file = new File(filePath);
            Map<String, ShoppingList> shoppingLists;

            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
                shoppingLists = gson.fromJson(reader, type);
            }

            if (shoppingLists == null) {
                shoppingLists = new HashMap<>();
            }

            shoppingLists.put(shoppingList.getID().toString(), shoppingList);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(shoppingLists));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteShoppingList(String id) {
        try {
            File file = new File(filePath);
            Map<String, ShoppingList> shoppingLists;

            // Read existing shopping lists
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
                shoppingLists = gson.fromJson(reader, type);
            }

            // Check if shopping lists exist and contain the specified id
            if (shoppingLists != null && shoppingLists.containsKey(id)) {
                // Remove the shopping list
                shoppingLists.remove(id);

                // Write updated shopping lists back to the file
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(gson.toJson(shoppingLists));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}