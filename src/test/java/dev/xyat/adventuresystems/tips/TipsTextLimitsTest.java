//? if >=1.21 {
/*package dev.xyat.adventuresystems.tips;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class TipsTextLimitsTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() throws Exception { TipsLifecycleTest.bootstrap(); }
    private static boolean valid(String text) {
        var entry=new HelpTip.JsonModel.Entry();entry.text=text;
        return ConfigLoader.areValidEntries(java.util.List.of(entry));
    }
    @Test void rejectsSeventeenthVisibleCharacter() {
        assertTrue(valid("中文提示内容限制十六字以内即可"));
        assertFalse(valid("一".repeat(17)));
        assertFalse(valid("x".repeat(17)));
    }
    @Test void limitsToThreeExplicitLines() {
        assertTrue(valid("第一行\n第二行\n第三行"));
        assertFalse(valid("一\n二\n三\n四"));
        assertFalse(valid("x\nx\nx\n"));
        assertTrue(valid("一\r\n二\r\n三"));
    }
    @Test void countsUnicodeCharactersWithoutColorCodes() {
        assertTrue(valid("§6"+"中".repeat(16)+"§r"));
        assertTrue(valid("😀".repeat(16)));
        assertFalse(valid("😀".repeat(17)));
        assertFalse(valid("a\tb"));
    }
    @Test void rejectsOversizedSerializationRatherThanTruncating() {
        var entry=new HelpTip.JsonModel.Entry();entry.text="中".repeat(17);
        assertThrows(IllegalArgumentException.class,()->ConfigLoader.toJson(java.util.List.of(entry)));
        assertNull(ConfigLoader.fromJson("{\"tips\":[{\"text\":\""+entry.text+"\"}]}"));
    }
    @Test void editorRoundTripPreservesNewlinesAndLiteralEscapes() {
        String text="§6标题§r\n换行用\\n\n原路径C:\\new";
        assertEquals(text,dev.xyat.adventuresystems.tips.config.TipTextLimits.fromEditor(
                dev.xyat.adventuresystems.tips.config.TipTextLimits.toEditor(text)));
        assertEquals("一\n二\n三",dev.xyat.adventuresystems.tips.config.TipTextLimits.normalizeLines("一\r\n二\r三"));
    }
    @Test void editorSnapshotCarriesLegacyTextButSavingStillRejectsIt() {
        var old=new HelpTip.JsonModel.Entry();old.text="A customized tip from an older version";
        var entries=java.util.List.of(old);
        String json=ConfigLoader.toEditorJson(entries);
        assertEquals(old.text,ConfigLoader.fromEditorJson(json).getFirst().text);
        assertNull(ConfigLoader.fromJson(json));
        assertThrows(IllegalArgumentException.class,()->ConfigLoader.toJson(entries));
    }
}
*///?}
