package org.project.data_structures.serializers;

import com.google.gson.*;
import org.project.data_structures.crdts.AWORSet;
import org.project.data_structures.crdts.VClockItemPair;
import org.project.data_structures.model.ShoppingList;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class ShoppingListDeserializer implements JsonDeserializer<ShoppingList> {
    @Override
    public ShoppingList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Extract listID and name
        UUID listID = UUID.fromString(jsonObject.get("listID").getAsString());
        String name = jsonObject.get("name").getAsString();

        // Create a new LWWSet to populate
        AWORSet items = new AWORSet();

        // Deserialize items
        JsonObject itemsJson = jsonObject.getAsJsonObject("items");
        JsonObject addSetJson = itemsJson.has("addSet") ? itemsJson.getAsJsonObject("addSet") : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : addSetJson.entrySet()) {
            String itemName = entry.getKey();
            JsonObject pairJson = entry.getValue().getAsJsonObject();
            VClockItemPair pair = context.deserialize(pairJson, VClockItemPair.class);
            items.addSet.put(itemName, pair);
        }

        JsonObject remSetJson = itemsJson.has("remSet") ? itemsJson.getAsJsonObject("remSet") : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : remSetJson.entrySet()) {
            String itemName = entry.getKey();
            JsonObject pairJson = entry.getValue().getAsJsonObject();
            VClockItemPair pair = context.deserialize(pairJson, VClockItemPair.class);
            items.removeSet.put(itemName, pair);
        }

        boolean isDeleted = jsonObject.get("isDeleted").getAsBoolean();
        return new ShoppingList(listID, name, items, isDeleted);
    }
}