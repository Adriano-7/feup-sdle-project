package org.project.server.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.data_structures.test.AWORSet;
import org.project.data_structures.test.AWORSetSerializer;
import org.project.model.ShoppingList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ServerDB {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(AWORSet.class, new AWORSetSerializer())
            .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
            .create();
    public static Map<String, ShoppingList> loadShoppingLists(int workerNbr) {
        System.out.println("Worker " + workerNbr + " loading shopping lists from local database...");

        String filePath = "src/main/java/org/project/server/database/" + workerNbr + "_db.json";
        File file = new File(filePath);
        if (file.length() == 0) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    public static void saveShoppingList(ShoppingList shoppingList, int workerIdentity) {
        System.out.println("Worker " + workerIdentity + " saving shopping list to local database...");

        String filePath = "src/main/java/org/project/server/database/" + workerIdentity + "_db.json";
        try {
            File file = new File(filePath);
            Map<String, ShoppingList> shoppingLists;

            if (file.length() == 0) {
                shoppingLists = new HashMap<>();
            } else {
                try (FileReader reader = new FileReader(file)) {
                    Type type = new TypeToken<Map<String, ShoppingList>>(){}.getType();
                    shoppingLists = gson.fromJson(reader, type);
                }
            }

            shoppingLists.put(shoppingList.getID().toString(), shoppingList);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(gson.toJson(shoppingLists));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}