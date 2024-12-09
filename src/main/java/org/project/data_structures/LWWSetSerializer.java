package org.project.data_structures;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.project.data_structures.LWWSet;

import java.lang.reflect.Type;

public class LWWSetSerializer implements JsonSerializer<LWWSet> {
    @Override
    public JsonElement serialize(LWWSet src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.items);
    }
}
