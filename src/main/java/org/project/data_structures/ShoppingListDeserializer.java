package org.project.data_structures;

import com.google.gson.*;
import org.project.data_structures.BGCounter;
import org.project.data_structures.LWWSet;
import org.project.model.Item;
import org.project.model.ShoppingList;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ShoppingListDeserializer implements JsonDeserializer<ShoppingList> {
    @Override
    public ShoppingList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Extract listID and name
        UUID listID = UUID.fromString(jsonObject.get("listID").getAsString());
        String name = jsonObject.get("name").getAsString();

        // Create a new LWWSet to populate
        LWWSet items = new LWWSet();

        // Deserialize items
        JsonObject itemsJson = jsonObject.getAsJsonObject("items");
        if (itemsJson != null) {
            for (Map.Entry<String, JsonElement> entry : itemsJson.entrySet()) {
                String itemName = entry.getKey();
                JsonObject itemInfo = entry.getValue().getAsJsonObject();

                JsonObject itemDetails = itemInfo.getAsJsonObject("item");
                int maxValue = itemDetails.getAsJsonObject("counter").get("maxValue").getAsInt();
                ConcurrentHashMap<String, AtomicLong> payload = itemDetails.getAsJsonObject("counter").getAsJsonObject("payload").entrySet().stream()
                        .collect(ConcurrentHashMap::new, (map, e) -> map.put(e.getKey(), new AtomicLong(e.getValue().getAsLong())), ConcurrentHashMap::putAll);

                BGCounter counter = new BGCounter(maxValue, payload);
                Item item = new Item(itemName, counter);

                // Create item info map
                Map<String, Object> itemInfoMap = new HashMap<>();
                itemInfoMap.put("item", item);
                itemInfoMap.put("add-time", itemInfo.get("add-time").getAsDouble());
                itemInfoMap.put("rmv-time", itemInfo.get("rmv-time").getAsDouble());

                // Add to LWWSet
                items.items.put(itemName, itemInfoMap);
            }
        }

        boolean isDeleted = jsonObject.get("isDeleted").getAsBoolean();
        return new ShoppingList(listID, name, items, isDeleted);
    }
}