package dev.xyat.adventuresystems.tips.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Version-specific bundled help, with exact migration of untouched earlier defaults. */
final class TipDefaults {
    private TipDefaults() {}
    static String read(String language) throws IOException {
        return resource("defaults/tips_"+language+".json");
    }
    static boolean isLegacy(String raw,String language) throws IOException {
        String name="tips_en_us.json";
        if(language.equals("zh_cn")) {
            //? if >=1.21 {
            /*name="tips_zh_cn_components.json";*/
            //?} else {
            name="tips_zh_cn_nbt.json";
            //?}
        }
        try{
            var parsed=JsonParser.parseString(raw);
            var legacy=JsonParser.parseString(resource("legacy/"+name));
            // The two Chinese demo sets differ in one tip ([NBT演示] / [组件演示]); a file from either version counts.
            List<JsonElement> demoSets=language.equals("zh_cn")
                    ?List.of(JsonParser.parseString(resource("legacy/tips_zh_cn_nbt.json")),JsonParser.parseString(resource("legacy/tips_zh_cn_components.json")))
                    :List.of(legacy);
            return parsed.equals(legacy)
                    || parsed.equals(JsonParser.parseString(resource("legacy/compact_v1/tips_"+language+".json")))
                    || onlyLegacyTexts(parsed,demoSets);
        }
        catch(RuntimeException invalid){return false;}
    }

    /**
     * Files saved by older editors, or trimmed by hand, hold the old demo tips with other fields. Every old tip is
     * over the line limits, so such a file shows nothing and the editor cannot save it until each one is fixed.
     */
    private static boolean onlyLegacyTexts(JsonElement file,List<JsonElement> demoSets) {
        Set<String> legacyTexts=new HashSet<>();
        for(JsonElement set:demoSets)
            for(JsonElement tip:set.getAsJsonObject().getAsJsonArray("tips"))legacyTexts.add(tip.getAsJsonObject().get("text").getAsString());
        if(!file.isJsonObject()||!(file.getAsJsonObject().get("tips") instanceof JsonArray tips)||tips.isEmpty())return false;
        for(JsonElement tip:tips){
            if(!tip.isJsonObject()||!(tip.getAsJsonObject().get("text") instanceof JsonPrimitive text)||!text.isString()
                    ||!legacyTexts.contains(text.getAsString()))return false;
        }
        return true;
    }
    private static String resource(String path) throws IOException {
        try(var input=TipDefaults.class.getResourceAsStream("/assets/adventuresystems/tips/"+path)){
            if(input==null)throw new IOException("Missing bundled tips: "+path);
            return new String(input.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
}
