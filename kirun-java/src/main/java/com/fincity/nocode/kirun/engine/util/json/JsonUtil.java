package com.fincity.nocode.kirun.engine.util.json;

import com.fincity.nocode.kirun.engine.util.primitive.PrimitiveUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtil {

    private JsonUtil() {
    }

    public static Map<String, Object> toMap(JsonObject jsonObject) {

        if (jsonObject == null || jsonObject.isJsonNull())
            return Collections.emptyMap();

        return jsonObject.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toObject(entry.getValue()),
                        (a, b) -> b));
    }

    public static List<Object> toList(JsonArray jsonArray) {

        if (jsonArray == null || jsonArray.isEmpty())
            return Collections.emptyList();

        List<Object> list = new ArrayList<>();

        jsonArray.forEach(jsonElement -> list.add(toObject(jsonElement)));

        return list;
    }

    public static Object toObject(JsonElement jsonElement) {

        if (jsonElement == null || jsonElement.isJsonNull())
            return null;
        if (jsonElement.isJsonObject())
            return toMap(jsonElement.getAsJsonObject());
        if (jsonElement.isJsonArray())
            return toList(jsonElement.getAsJsonArray());
        if (jsonElement.isJsonPrimitive())
            return PrimitiveUtil.findPrimitive(jsonElement).getT2();

        return null;
    }
}

