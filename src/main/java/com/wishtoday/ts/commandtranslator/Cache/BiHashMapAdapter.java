package com.wishtoday.ts.commandtranslator.Cache;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class BiHashMapAdapter<K, V> implements JsonSerializer<ConcurrentBiHashMap<K, V>>,
        JsonDeserializer<ConcurrentBiHashMap<K, V>> {

    @Override
    public JsonElement serialize(ConcurrentBiHashMap<K, V> src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("map", context.serialize(src.getK2v()));
        return jsonObject;
    }

    @Override
    public ConcurrentBiHashMap<K, V> deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException {
        ConcurrentBiHashMap<K, V> result = new ConcurrentBiHashMap<>();

        JsonObject jsonObject = json.getAsJsonObject();
        Map<K, V> map = context.deserialize(jsonObject.get("map"),
                new TypeToken<Map<K, V>>(){}.getType());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}