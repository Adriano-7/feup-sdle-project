package org.project.data_structures.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.project.data_structures.AWORSet;
import org.project.data_structures.VClockItemPair;
import java.lang.reflect.Type;
import java.util.Map;

public class AWORSetSerializer implements JsonSerializer<AWORSet> {
    @Override
    public JsonElement serialize(AWORSet src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        JsonObject addSetJson = new JsonObject();
        for (Map.Entry<String, VClockItemPair> entry : src.addSet.entrySet()) {
            String itemName = entry.getKey();
            VClockItemPair vClockItemPair = entry.getValue();

            JsonObject pairJson = context.serialize(vClockItemPair).getAsJsonObject();
            addSetJson.add(itemName, pairJson);
        }

        JsonObject remSetJson = new JsonObject();
        for (Map.Entry<String, VClockItemPair> entry : src.removeSet.entrySet()) {
            String itemName = entry.getKey();
            VClockItemPair vClockItemPair = entry.getValue();

            JsonObject pairJson = context.serialize(vClockItemPair).getAsJsonObject();
            remSetJson.add(itemName, pairJson);
        }

        jsonObject.add("addSet", addSetJson);
        jsonObject.add("remSet", remSetJson);
        return jsonObject;
    }
}