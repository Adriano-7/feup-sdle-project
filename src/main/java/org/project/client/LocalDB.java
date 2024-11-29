package org.project.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.project.model.ShoppingList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
public class LocalDB {
    private final Gson gson;
    private final String filePath = "src/main/java/org/project/client/db.json";
    public LocalDB() {
        this.gson = new Gson();
        initializeFile();
    }

    private void initializeFile() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{}");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ShoppingList getShoppingList(String id) {
        try {
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
            Map<String, ShoppingList> shoppingLists = gson.fromJson(reader, type);
            reader.close();
            return shoppingLists.get(id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveShoppingList(ShoppingList shoppingList) {
        System.out.println("Saving shopping list to local database...");
        try {
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<Map<String, ShoppingList>>() {}.getType();
            Map<String, ShoppingList> shoppingLists = gson.fromJson(reader, type);
            reader.close();

            if (shoppingLists == null) {
                shoppingLists = new HashMap<>();
            }

            shoppingLists.put(shoppingList.getID(), shoppingList);
            FileWriter writer = new FileWriter(filePath);
            gson.toJson(shoppingLists, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}