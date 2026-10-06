//? if >=1.21 && <26 {
/*package dev.xyat.adventureguivalidation;

import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.KineticGui;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import java.lang.reflect.Field;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.adventuresystems.curios.wallet.client.gui.ShopScreen;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
import dev.xyat.adventuresystems.ftb.client.gui.*;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.gui.editor.*;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/^** Uses the existing client and unsaved page drafts. Never clicks or saves editor changes. *^/
public final class GuiLongTextValidation {
    private static final Logger LOG=LoggerFactory.getLogger(GuiLongTextValidation.class);
    private static final String ROOT=System.getProperty("adventuresystems.guiValidation.output", "gui-validation");
    private static final String[] NAMES={"shop-empty","shop-buy","shop-sell","shop-locked","shop-gacha","shop-choice-overlay","shop-quest-picker","shop-reward-picker","entry-buy","entry-sell","entry-gacha","entry-choice","commands-empty","commands","command-picker","currency-picker","wallet","quests-empty","quests","quest-picker","rewards-empty","rewards-gacha","rewards-choice","rewards-sell","ftb-binding-empty","ftb-binding","ftb-submit","ftb-select","ftb-item-blacklist","ftb-task-blacklist","tips-empty","tips","tip-time","tip-registry"};
    private static boolean installed,started,screenshot,finished,originalFullscreen;
    private static String originalLanguage;
    private static int originalScale,originalWidth,originalHeight,phase=-1,page=-1,captures,failures;
    private static long due;
    private static CompletableFuture<Void> reload;
    private static Language stressOriginal;
    private static KineticPage fixturePage;
    private static Field mouseXField,mouseYField;
    private static double originalMouseX,originalMouseY;
    private static final String LONG_NAME="§bA deliberately long name for bounded text §e".repeat(4);
    private static final String WALLET="dev.xyat.adventuresystems.curios.wallet.client.gui.";

    public static void install() { if(installed)return;installed=true;KineticClientEvents.onTick(KineticClientEvents.TickPhase.END,GuiLongTextValidation::tick); }
    private static void tick() {
        if(finished)return;
        try {
            var mc=Minecraft.getInstance();
            if(fixturePage!=null && KineticGui.currentPage()==fixturePage)suppressCachedMouse();
            if(!started) {
                if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
                started=true;originalLanguage=mc.getLanguageManager().getSelected();originalScale=mc.options.guiScale().get();
                originalWidth=mc.getWindow().getWidth();originalHeight=mc.getWindow().getHeight();originalFullscreen=mc.getWindow().isFullscreen();
                mc.options.guiScale().set(0);
                if(originalFullscreen)mc.getWindow().toggleFullScreen();
                nextPhase();
                return;
            }
            if(reload!=null) {
                if(!reload.isDone() || mc.getOverlay()!=null)return;
                reload.join();reload=null;
                if(phase==4) {
                    stressOriginal=Language.getInstance();
                    Language.inject(new StressLanguage(stressOriginal));
                }
                nextPage();return;
            }
            long now=System.currentTimeMillis();
            if(!screenshot && now>=due) {
                capture("start");
                if (phase == 4 && page == 31) {
                    int x=(Integer)field(fixturePage,"editX"), y=(Integer)invoke(fixturePage,"conditionTop");
                    Object scroller=field(fixturePage,"conditionScroll");
                    if ((Integer)invoke(scroller,"maxOffset") <= 0) throw new IllegalStateException("Expected overflowing conditions");
                    Object accepted=invoke(fixturePage,"onMouseScroll",new dev.xyat.kineticcore.api.client.gui.input.ScrollInput(x+4,y+4,0,-10));
                    if (!Boolean.TRUE.equals(accepted) || (Integer)invoke(scroller,"offset") <= 0) throw new IllegalStateException("Conditions wheel did not scroll");
                    LOG.info("ADVENTURE_GUI_CONDITION_SCROLL accepted=true offset={}",invoke(scroller,"offset"));
                }
                screenshot=true;due=now+(phase==4?3400:550);return;
            }
            if(screenshot && now>=due) {
                if(phase==4)capture("scroll");
                nextPage();
            }
        } catch(Throwable error) {
            failures++;LOG.error("ADVENTURE_GUI_FAIL phase="+phase+" page="+page,error);
            finish();
        }
    }
    private static void nextPhase() {
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        phase++;page=-1;
        String selectedPhases=System.getProperty("adventuresystems.guiValidation.phases", "");
        while(phase<5 && !selectedPhases.isBlank() && !List.of(selectedPhases.split(",")).contains(String.valueOf(phase)))phase++;
        if(phase>=5){finish();return;}
        var mc=Minecraft.getInstance();
        mc.setScreen(null);
        String lang=phase==2 || phase==3?"zh_cn":"en_us";
        boolean changed=!lang.equals(mc.getLanguageManager().getSelected());
        mc.getLanguageManager().setSelected(lang);
        mc.options.languageCode=lang;
        int width=phase==1 || phase==3?1920:854,height=phase==1 || phase==3?1080:480;
        mc.getWindow().setWindowed(width,height);mc.resizeDisplay();
        if(changed)reload=mc.reloadResourcePacks();
        else {if(phase==4){stressOriginal=Language.getInstance();Language.inject(new StressLanguage(stressOriginal));}try{nextPage();}catch(Exception error){throw new IllegalStateException(error);}}
        LOG.info("ADVENTURE_GUI_PHASE phase={} language={} requested={}x{} autoScale=true",phase,lang,width,height);
    }
    private static void nextPage() throws Exception {
        page++;
        String selectedPages=System.getProperty("adventuresystems.guiValidation.pages", "");
        while(page<NAMES.length && !selectedPages.isBlank() && !List.of(selectedPages.split(",")).contains(String.valueOf(page)))page++;
        if(page>=NAMES.length){nextPhase();return;}
        openPage(page);
        screenshot=false;due=System.currentTimeMillis()+1000;
        fixturePage=KineticGui.currentPage();suppressCachedMouse();
        LOG.info("ADVENTURE_GUI_OPEN phase={} case={} page={}",phase,NAMES[page],KineticGui.currentPage().getClass().getName());
    }
    @SuppressWarnings("unchecked")
    private static void openPage(int index) throws Exception {
        var mode=index==2 || index==9 || index==23?Shop.Mode.SELL:Shop.Mode.BUY;
        boolean gacha=index==4 || index==10 || index==21;
        boolean choice=index==5 || index==7 || index==11 || index==22 || index==23;
        var entry=entry(mode,gacha,choice,index==3);
        Object draft=construct(WALLET+"ShopGuiSupport$EditorDraft",entry);
        if(index==12 || index==17 || index==20){((List<?>)field(draft,"commands")).clear();((List<?>)field(draft,"questIds")).clear();((List<?>)field(draft,"rewards")).clear();}
        else {((List<Object>)field(draft,"commands")).add(construct(WALLET+"ShopGuiSupport$CommandDraft","minecraft:diamond_sword",LONG_NAME,"say Local unsaved validation"));}
        KineticPage p;
        switch(index) {
            case 0,1,2,3,4,5,6,7 -> {
                var tag=new CompoundTag();tag.putBoolean("CanEdit",true);tag.putBoolean("UseBackpack",true);tag.putBoolean("UseRs",true);
                var entries=new ListTag();
                var method=Shop.class.getDeclaredMethod("entryToTag",Shop.Entry.class);method.setAccessible(true);
                if(index!=0)entries.add((CompoundTag)method.invoke(null,entry));
                tag.put(mode==Shop.Mode.BUY?"Buy":"Sell",entries);
                p=new ShopScreen(false,balances(),tag,true);setField(p,"mode",mode);setField(p,"selectedIndex",index==0?-1:0);
                KineticGui.open(p);
                if(index==4)setField(p,"rewardPreviewExpanded",true);
                if(index==5)invoke(p,"openChoiceOverlay",entry);
                if(index==6 || index==7){var enumType=Class.forName(WALLET+"ShopScreen$OverlayLayer");invoke(field(p,"overlayLayers"),"open",Enum.valueOf((Class)enumType,index==6?"QUEST_PICKER":"REWARD_PICKER"));}
                return;
            }
            case 8,9,10,11 -> p=(KineticPage)construct(WALLET+"ShopEntryEditorScreen",new ShopScreen(false,balances(),new CompoundTag(),true),draft);
            case 12,13 -> p=(KineticPage)construct(WALLET+"CommandManageScreen",draft);
            case 14 -> p=(KineticPage)construct(WALLET+"CommandRewardPickerScreen",draft,false);
            case 15 -> p=(KineticPage)construct(WALLET+"CurrencyPickerScreen",draft);
            case 16 -> p=(KineticPage)construct(WALLET+"MainScreen",balances(),true);
            case 17,18 -> p=(KineticPage)construct(WALLET+"QuestManageScreen",draft);
            case 19 -> p=(KineticPage)construct(WALLET+"QuestPickerScreen",(java.util.function.Consumer<Long>)v->{});
            case 20,21,22,23 -> p=(KineticPage)construct(WALLET+"RewardPoolScreen",draft);
            case 24,25 -> p=new FTBItemBindingEditorScreen();
            case 26 -> {
                var file=ClientQuestFile.getInstance();var chapter=new Chapter(0x7000,file,null);var quest=new Quest(0x7001,chapter);
                var task=new ItemTask(0x7002,quest).setStackAndCount(new ItemStack(Items.EMERALD),32);
                p=new FTBSubmitCountScreen(task);
            }
            case 27 -> p=new SelectScreenFTB(namedStack(),refs());
            case 28 -> p=new ItemBlacklistScreenFTB();
            case 29 -> p=new FTBBlacklistScreen();
            case 30,31 -> {var tip=new HelpTip.JsonModel.Entry();tip.text=LONG_NAME;tip.conditions=new HelpTip.JsonModel.Conditions();tip.conditions.biome="minecraft:plains";tip.conditions.structure="minecraft:village_plains";tip.conditions.dimension="minecraft:overworld";tip.conditions.advancement="minecraft:story/enter_the_nether";tip.conditions.curios=List.of();p=new TipEditorScreen("en_us",index==30?List.of():List.of(tip));}
            case 32 -> p=new TimeEditScreen(5000,(java.util.function.Consumer<Integer>)v->{});
            case 33 -> p=new TipSelectors.RegistrySelectorScreen("biomes",(java.util.function.Consumer<String>)v->{});
            default -> throw new IllegalArgumentException("Unknown fixture page "+index);
        }
        KineticGui.open(p);
        if(index==13){setField(p,"selectedIndex",0);invoke(p,"selectCommand",0);}
        if(index==21 || index==22 || index==23)invoke(p,"selectReward",0);
        if(index==25){((List<RefFTB>)field(p,"visibleTasks")).addAll(refs());((List<RefFTB>)field(p,"allTasks")).addAll(refs());((List<RefFTB>)field(p,"boundTasks")).addAll(refs());setField(p,"selectedStack",namedStack());setField(p,"dirty",true);}
    }
    private static CompoundTag balances(){var tag=new CompoundTag();for(var currency:dev.xyat.adventuresystems.curios.wallet.data.Data.currencies())tag.putLong(currency.itemId(),123456789L);return tag;}
    private static ItemStack namedStack(){var stack=new ItemStack(Items.DIAMOND_SWORD);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,dev.xyat.adventuresystems.text.AdventureText.literal(LONG_NAME));return stack;}
    private static List<RefFTB> refs(){return List.of(new RefFTB(1,"0000000000000001",LONG_NAME,LONG_NAME,"local",LONG_NAME),new RefFTB(2,"0000000000000002","Short task","Short chapter","local","Short task"));}
    private static Shop.Entry entry(Shop.Mode mode,boolean gacha,boolean choice,boolean locked){
        var rewards=List.of(new Shop.Reward(namedStack(),10,50.0D,LONG_NAME,""),new Shop.Reward(new ItemStack(Items.EMERALD),10,50.0D,"Short reward",""));
        return new Shop.Entry(mode,0,"validation",namedStack(),"minecraft:emerald",1500L,1800,100,LONG_NAME,LONG_NAME,LONG_NAME,"",1,0,List.of(),1L,LONG_NAME,List.of(1L,2L),List.of(LONG_NAME,"Short quest"),!locked,gacha,choice,rewards,0,2,120L,64L,3000L,64L,64L,true,true,true,"READY",3L,ItemStack.EMPTY);
    }
    private static void suppressCachedMouse() throws Exception {
        var mouse = Minecraft.getInstance().mouseHandler;
        if (mouseXField == null) {
            // Exact official/SRG names verified in this worktree's build/createMcpToSrg/output.tsrg.
            mouseXField = mouseCoordinateField(mouse, "xpos", "f_91507_");
            mouseYField = mouseCoordinateField(mouse, "ypos", "f_91508_");
            originalMouseX = mouseXField.getDouble(mouse);
            originalMouseY = mouseYField.getDouble(mouse);
            LOG.info("ADVENTURE_GUI_MOUSE cachedCoordinatesOnly=true fields={},{}", mouseXField.getName(), mouseYField.getName());
        }
        mouseXField.setDouble(mouse, -1000.0D);
        mouseYField.setDouble(mouse, -1000.0D);
    }

    private static Field mouseCoordinateField(Object mouse, String official, String srg) throws Exception {
        for (String name : List.of(official, srg)) try {
            Field coordinate = mouse.getClass().getDeclaredField(name);
            if (coordinate.getType() != double.class) throw new IllegalStateException("Unexpected mouse coordinate field type: " + name);
            coordinate.setAccessible(true);
            return coordinate;
        } catch (NoSuchFieldException ignored) { }
        throw new NoSuchFieldException(official + " / " + srg);
    }

    private static void setField(Object target,String name,Object value)throws Exception {
        for(Class<?> type=target.getClass();type!=null;type=type.getSuperclass())try{var f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);return;}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(name);
    }
    private static Object field(Object target,String name)throws Exception {
        for(Class<?> type=target.getClass();type!=null;type=type.getSuperclass())try {
            var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);
        }catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(name);
    }
    private static Object invoke(Object target,String name,Object...args)throws Exception {
        for(Class<?> type=target.getClass();type!=null;type=type.getSuperclass())for(var m:type.getDeclaredMethods()) {
            if(m.getName().equals(name)&&compatible(m.getParameterTypes(),args)) {
                m.setAccessible(true);return m.invoke(target,args);
            }
        }
        throw new NoSuchMethodException(name);
    }
    private static Object construct(String name,Object...args)throws Exception {
        for(var c:Class.forName(name).getDeclaredConstructors())if(compatible(c.getParameterTypes(),args)){c.setAccessible(true);return c.newInstance(args);}
        throw new NoSuchMethodException(name+" constructor");
    }
    private static boolean compatible(Class<?>[]types,Object[]args) {
        if(types.length!=args.length)return false;
        for(int i=0;i<types.length;i++)if(args[i]!=null && !(types[i].isInstance(args[i]) || types[i]==int.class && args[i] instanceof Integer || types[i]==boolean.class && args[i] instanceof Boolean))return false;
        return true;
    }
    private static void capture(String frame)throws Exception {
        var mc=Minecraft.getInstance();Path path=Path.of(ROOT,String.format("%d-%02d-%s-%s.png",phase,page,NAMES[page],frame));Files.createDirectories(path.getParent());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
        captures++;LOG.info("ADVENTURE_GUI_CAPTURE phase={} case={} image={}x{}",phase,NAMES[page],mc.getWindow().getWidth(),mc.getWindow().getHeight());
    }
    private static void finish() {
        finished=true;
        if(mouseXField!=null)try{mouseXField.setDouble(Minecraft.getInstance().mouseHandler,originalMouseX);mouseYField.setDouble(Minecraft.getInstance().mouseHandler,originalMouseY);}catch(Exception ignored){}
        var mc=Minecraft.getInstance();
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        mc.options.guiScale().set(originalScale);
        mc.getLanguageManager().setSelected(originalLanguage);mc.options.languageCode=originalLanguage;
        mc.setScreen(null);
        mc.getWindow().setWindowed(originalWidth,originalHeight);
        if(originalFullscreen && !mc.getWindow().isFullscreen())mc.getWindow().toggleFullScreen();
        LOG.info("ADVENTURE_GUI_{} pages={} captures={} failures={} userSettingsRestored=true",failures==0?"PASS":"FAIL",NAMES.length,captures,failures);
        dev.xyat.kineticcore.api.runtime.KineticClientRuntime.stopClient();
    }
    private static final class StressLanguage extends Language {
        private final Language delegate;
        StressLanguage(Language delegate){this.delegate=delegate;}
        @Override public String getOrDefault(String key,String fallback) {
            String text=delegate.getOrDefault(key,fallback);
            return (key.contains("adventuresystems") || key.equals("gui.done") || key.equals("gui.cancel") || key.equals("item.minecraft.emerald")) && !key.contains(".value.") && !key.endsWith(".add_mark") && !key.endsWith(".remove_mark") && !key.endsWith(".common.expand") && !key.endsWith(".common.collapse") && !key.endsWith(".shop_multi_reward_plus")?text+" - deliberately extended translation to verify text stays inside its own region":text;
        }
        @Override public boolean has(String key){return delegate.has(key);}
        @Override public boolean isDefaultRightToLeft(){return delegate.isDefaultRightToLeft();}
        @Override public net.minecraft.util.FormattedCharSequence getVisualOrder(net.minecraft.network.chat.FormattedText text){return delegate.getVisualOrder(text);}
    }
}
*///?}
