package org.project.client.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.project.data_structures.serializers.ShoppingListDeserializer;
import org.project.data_structures.crdts.AWORSet;
import org.project.data_structures.serializers.AWORSetSerializer;
import org.project.data_structures.model.ShoppingList;
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

            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
                shoppingLists = gson.fromJson(reader, type);
            }

            if (shoppingLists != null && shoppingLists.containsKey(id)) {
                shoppingLists.remove(id);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(gson.toJson(shoppingLists));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}