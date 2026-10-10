package dev.xyat.adventurevalidation;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.TipTextLimits;
import dev.xyat.kineticcore.api.runtime.KineticPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/** Runs only in opt-in, owned client profiles; restores exact configuration bytes. */
public final class TipsRuntimeChecks {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(TipsRuntimeChecks.class);
    private TipsRuntimeChecks() {}
    public static void verifyFiles() throws Exception {
        for(String language:List.of("en_us","zh_cn")) {
            var path=KineticPaths.configFile("kineticcore/tips/tips_"+language+".json");
            byte[] original=Files.exists(path)?Files.readAllBytes(path):null;
            try {
                String defaults=resource("defaults/tips_"+language+".json");
                var entries=ConfigLoader.fromJson(defaults);
                require(entries!=null && entries.size()==16,"16 valid bundled tips "+language);
                require(entries.stream().filter(e->e.conditions!=null).count()==4,"contextual defaults");
                verifySelection(language,entries);
                Files.deleteIfExists(path);
                require(ConfigLoader.getRawEntriesForLanguage(language).size()==16,"fresh file generation");
                String legacy="tips_en_us.json";
                if(language.equals("zh_cn")) {
                    //? if >=1.21 {
                    /*legacy="tips_zh_cn_components.json";*/
                    //?} else {
                    legacy="tips_zh_cn_nbt.json";
                    //?}
                }
                Files.writeString(path,resource("legacy/"+legacy),StandardCharsets.UTF_8);
                require(ConfigLoader.getRawEntriesForLanguage(language).size()==16,"untouched defaults migrate");
                Files.writeString(path,resource("legacy/compact_v1/tips_"+language+".json"),StandardCharsets.UTF_8);
                var compactMigrated=ConfigLoader.getRawEntriesForLanguage(language);
                require(compactMigrated.size()==16 && compactMigrated.get(0).text.equals(entries.get(0).text),"untouched compact defaults migrate");
                String migrated=Files.readString(path);
                var invalid=new HelpTip.JsonModel.Entry();invalid.text="x".repeat(17);
                require(!ConfigLoader.saveRawEntriesForLanguage(language,List.of(invalid)),"server rejects 17 characters");
                invalid.text="1\n2\n3\n4";
                require(!ConfigLoader.saveRawEntriesForLanguage(language,List.of(invalid)),"server rejects fourth line");
                require(migrated.equals(Files.readString(path)),"rejected save preserves file");
                String oldText="Older custom text ".repeat(40);
                String custom="{\"tips\":[{\"text\":\""+oldText+"\",\"stage\":\"game\",\"time\":5000},{\"text\":\"Still valid\",\"stage\":\"game\",\"time\":5000}]}";
                Files.writeString(path,custom,StandardCharsets.UTF_8);
                require(ConfigLoader.getRawEntriesForLanguage(language).size()==2,"old long custom tip remains editable");
                require(custom.equals(Files.readString(path)),"custom config never overwritten");
                require(!ConfigLoader.areValidEntries(ConfigLoader.getRawEntriesForLanguage(language)),"old long tip must be shortened to save");
                var snapshot=dev.xyat.adventuresystems.tips.TipsNetwork.class.getDeclaredMethod("snapshotJson",String.class);
                snapshot.setAccessible(true);
                String editorJson=(String)snapshot.invoke(null,language);
                dev.xyat.adventuresystems.tips.TipsNetwork.ClientProxy.handleOpenEditor(language,editorJson);
                var oldEditor=dev.xyat.kineticcore.api.client.gui.KineticGui.currentPage(
                        dev.xyat.adventuresystems.tips.client.gui.editor.TipEditorScreen.class);
                require(oldEditor!=null,"server snapshot opens customized long tip in client editor");
                var selected=oldEditor.getClass().getDeclaredField("selectedEntry");selected.setAccessible(true);
                require(oldText.equals(((HelpTip.JsonModel.Entry)selected.get(oldEditor)).text),"opening does not truncate old custom text");
                var runtimeSnapshot=dev.xyat.adventuresystems.tips.TipsNetwork.class.getDeclaredMethod("runtimeSnapshotJson",String.class);
                runtimeSnapshot.setAccessible(true);
                var runtime=ConfigLoader.fromJson((String)runtimeSnapshot.invoke(null,language));
                require(runtime!=null && runtime.size()==1 && runtime.get(0).text.equals("Still valid"),"runtime snapshot preserves valid tips alongside old oversized tips");
                var save=oldEditor.getClass().getDeclaredMethod("save");save.setAccessible(true);save.invoke(oldEditor);
                var pending=oldEditor.getClass().getDeclaredField("savePending");pending.setAccessible(true);
                require(!pending.getBoolean(oldEditor),"GUI rejects oversized text before sending");
                require(custom.equals(Files.readString(path)),"GUI rejection keeps custom file");
                String escaped="§6标题§r\n换行用\\n\n路径C:\\new";
                require(escaped.equals(TipTextLimits.fromEditor(TipTextLimits.toEditor(escaped))),"editor escape round trip");
            } finally {
                if(original==null)Files.deleteIfExists(path);else Files.write(path,original);
            }
        }
        LOG.info("TIPS_RUNTIME_PASS defaults=16 lineLimit=16 maxLines=3 migration=exact customPreserved=true rejectedWritesPreserved=true");
    }
    private static void verifySelection(String language,List<HelpTip.JsonModel.Entry> entries)throws Exception {
        var probe=new HelpTip.JsonModel.Entry();probe.text="Title\nUse \\n here\nLast";
        var copy=new java.util.ArrayList<>(entries);copy.add(probe);
        var editor=new dev.xyat.adventuresystems.tips.client.gui.editor.TipEditorScreen(language,copy);
        dev.xyat.kineticcore.api.client.gui.KineticGui.open(editor);
        var select=editor.getClass().getDeclaredMethod("select",HelpTip.JsonModel.Entry.class);select.setAccessible(true);
        String original=probe.text;
        select.invoke(editor,probe);
        require(original.equals(probe.text),"select preserves actual newlines and literal backslash n");
        var input=editor.getClass().getDeclaredField("textInput");input.setAccessible(true);
        require(TipTextLimits.toEditor(original).equals(((dev.xyat.kineticcore.api.client.gui.widget.KineticTextField)input.get(editor)).textValue()),"selection displays escaped editor form");
        select.invoke(editor,entries.get(0));select.invoke(editor,probe);
        require(original.equals(probe.text),"repeated selection does not mutate text");
    }
    public static PauseScreen pause() throws Exception {
        return pause(0);
    }
    public static PauseScreen pause(int tipIndex) throws Exception {
        var mc=Minecraft.getInstance();
        String language=mc.getLanguageManager().getSelected();
        var entries=ConfigLoader.fromJson(resource("defaults/tips_"+language+".json"));
        var screen=new PauseScreen(true);
        TipCache.TIP_MANAGER.replaceServerEntries(List.of(entries.get(tipIndex)));
        require(TipCache.TIP_MANAGER.getValidTip(screen)!=null,"pause general tip");
        var contextual=new HelpTip.JsonModel.Entry();contextual.stage="game";contextual.text="Context probe";
        contextual.conditions=new HelpTip.JsonModel.Conditions();
        //? if >=26.1 {
        /*contextual.conditions.biome=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().orElseThrow().identifier().toString();*/
        //?} else {
        contextual.conditions.biome=mc.level.getBiome(mc.player.blockPosition()).unwrapKey().orElseThrow().location().toString();
        //?}
        contextual.conditions.structure="minecraft:village_plains";
        String originalStructure=TipCache.currentStructure;
        try {
            TipCache.TIP_MANAGER.replaceServerEntries(List.of(contextual));
            TipCache.currentStructure="minecraft:village_plains";
            require(TipCache.TIP_MANAGER.getValidTip(screen)!=null,"matching biome and structure");
            TipCache.currentStructure="minecraft:village_desert";
            require(TipCache.TIP_MANAGER.getValidTip(screen)==null,"nonmatching structure");
            TipCache.currentStructure="minecraft:village_plains";
            contextual.conditions.biome="minecraft:the_void";
            TipCache.TIP_MANAGER.replaceServerEntries(List.of(contextual));
            require(TipCache.TIP_MANAGER.getValidTip(screen)==null,"nonmatching biome");
        } finally {
            TipCache.currentStructure=originalStructure;
            TipCache.TIP_MANAGER.replaceServerEntries(List.of(entries.get(tipIndex)));
        }
        mc.setScreen(screen);
        TipRenderer.refresh(screen);
        LOG.info("TIPS_PAUSE_PASS language={} general=true biomeAndStructureFiltering=true",language);
        return screen;
    }
    private static String resource(String name)throws Exception {
        try(var in=ConfigLoader.class.getResourceAsStream("/assets/adventuresystems/tips/"+name)){
            require(in!=null,"bundled resource "+name);return new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
