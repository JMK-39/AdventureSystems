package dev.xyat.adventuresystems.tips.config;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
            return parsed.equals(JsonParser.parseString(resource("legacy/"+name)))
                    || parsed.equals(JsonParser.parseString(resource("legacy/compact_v1/tips_"+language+".json")));
        }
        catch(RuntimeException invalid){return false;}
    }
    private static String resource(String path) throws IOException {
        try(var input=TipDefaults.class.getResourceAsStream("/assets/adventuresystems/tips/"+path)){
            if(input==null)throw new IOException("Missing bundled tips: "+path);
            return new String(input.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
}
