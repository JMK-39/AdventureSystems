//? if <26 {
package dev.xyat.adventureguivalidation;

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
import dev.xyat.adventuresystems.curios.wallet.client.gui.MainScreen;
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

/*** Uses the existing client and unsaved page drafts. Never clicks or saves editor changes. */
public final class GuiLongTextValidation {
    private static final Logger LOG=LoggerFactory.getLogger(GuiLongTextValidation.class);
    private static final String ROOT=System.getProperty("adventuresystems.guiValidation.output", "gui-validation");
    private static final String[] NAMES={"shop-empty","shop-buy","shop-sell","shop-locked","shop-gacha","shop-choice-overlay","shop-quest-picker","shop-reward-picker","entry-buy","entry-sell","entry-gacha","entry-choice","commands-empty","commands","command-picker","currency-picker","wallet","quests-empty","quests","quest-picker","rewards-empty","rewards-gacha","rewards-choice","rewards-sell","ftb-binding-empty","ftb-binding","ftb-submit","ftb-select","ftb-item-blacklist","ftb-task-blacklist","tips-empty","tips","tip-time","tip-registry","tip-pause","tip-short","wallet-hud"};
    private static boolean installed,started,screenshot,finished,originalFullscreen;
    private static String originalLanguage;
    private static int originalScale,originalWidth,originalHeight,phase=-1,page=-1,captures,failures;
    private static int walletStage;
    private static long walletDeadline;
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
                boolean menuOnly=Boolean.getBoolean("adventuresystems.guiValidation.menuOnly");
                if(!menuOnly && (mc.player==null || mc.level==null || mc.getSingleplayerServer()==null))return;
                if(menuOnly && ((mc.screen==null && mc.player==null) || mc.getOverlay()!=null))return;
                started=true;originalLanguage=mc.getLanguageManager().getSelected();originalScale=mc.options.guiScale().get();
                originalWidth=mc.getWindow().getWidth();originalHeight=mc.getWindow().getHeight();originalFullscreen=mc.getWindow().isFullscreen();
                mc.options.guiScale().set(0);
                if(originalFullscreen)mc.getWindow().toggleFullScreen();
                if(!menuOnly)dev.xyat.adventurevalidation.TipsRuntimeChecks.verifyFiles();
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
            if(mc.getOverlay()!=null){due=now+300;return;}
            if(!screenshot && now>=due) {
                if(page==7)verifyRewardPickerClip();
                if(page==36)verifyWalletHud();
                if (page == 16 && walletStage == -1) {
                    verifyWalletExpansion();
                    walletStage=0;due=now+350;return;
                }
                capture("start");
                if (page == 16) {
                    var list = (dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList<?>) field(fixturePage, "list");
                    if (list.items().size() <= list.visibleRows()) throw new IllegalStateException("Wallet fixture needs an overflowing coin list");
                    invoke(list, "onMouseScroll", new dev.xyat.kineticcore.api.client.gui.input.ScrollInput(list.controlX()+4,list.controlY()+4,0,-10000));
                    walletStage = 1;
                    walletDeadline = now + 20_000;
                }
                if ((phase == 4 || Boolean.getBoolean("adventuresystems.guiValidation.tipsLayout")) && page == 31) {
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
                if (page == 16) {
                    var walletList=(dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList<?>)field(fixturePage,"list");
                    if (walletList.rowTop(walletList.items().size()-1)+walletList.rowHeight()!=walletList.controlY()+walletList.controlHeight()
                            && now < walletDeadline) { due=now+150;return; }
                    validateWalletEnd();
                    capture(walletStage == 1 ? "wheel-bottom" : "drag-bottom");
                    if (walletStage == 1) {
                        var list = (dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList<?>) field(fixturePage,"list");
                        list.setScrollOffset(0);
                        int x=list.controlX()+list.controlWidth()-2, y=list.controlY();
                        var left=dev.xyat.kineticcore.api.client.gui.input.MouseButton.LEFT;
                        if (!Boolean.TRUE.equals(invoke(list,"onMouseClick",new dev.xyat.kineticcore.api.client.gui.input.MouseInput(x,y+2,left,0,0)))) throw new IllegalStateException("Wallet scrollbar thumb is not clickable");
                        if (!Boolean.TRUE.equals(invoke(list,"onMouseDrag",new dev.xyat.kineticcore.api.client.gui.input.MouseDragInput(x,y+list.controlHeight(),left,0,0,list.controlHeight())))) throw new IllegalStateException("Wallet scrollbar thumb did not drag");
                        invoke(list,"onMouseRelease",new dev.xyat.kineticcore.api.client.gui.input.MouseInput(x,y+list.controlHeight(),left,0,0));
                        walletStage=2;due=now+650;walletDeadline=now+20_000;return;
                    }
                }
                if(phase==4 || Boolean.getBoolean("adventuresystems.guiValidation.tipsLayout"))capture("scroll");
                if(page==31 && Boolean.getBoolean("adventuresystems.guiValidation.tipsLayout"))validateScrolledTipActions();
                if(page==32) {
                    Object timePage=fixturePage;
                    invoke(timePage,"save");
                    if(KineticGui.currentPage()==timePage)throw new AssertionError("Time dialog Save did not return to its parent");
                    LOG.info("ADVENTURE_TIME_SAVE_PASS returnedToParent=true");
                }
                nextPage();
            }
        } catch(Throwable error) {
            failures++;LOG.error("ADVENTURE_GUI_FAIL phase="+phase+" page="+page,error);
            finish();
        }
    }
    private static void validateWalletEnd() throws Exception {
        requireFixturePage("wallet end validation");
        var list=(dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList<?>)field(fixturePage,"list");
        int last=list.items().size()-1;
        if (list.rowTop(last)+list.rowHeight()!=list.controlY()+list.controlHeight()) throw new IllegalStateException("Wallet final row is clipped or leaves blank space: last="+list.rowTop(last)+" height="+list.rowHeight()+" bottom="+(list.controlY()+list.controlHeight())+" offset="+list.scrollOffset()+" rows="+list.items().size());
        int offset=list.scrollOffset();
        invoke(list,"onMouseScroll",new dev.xyat.kineticcore.api.client.gui.input.ScrollInput(list.controlX()+4,list.controlY()+4,0,-10000));
        if (offset!=list.scrollOffset()) throw new IllegalStateException("Wallet scrolls beyond its final row");
    }
    private static void validateScrolledTipActions() throws Exception {
        int top=(Integer)invoke(fixturePage,"conditionTop"), bottom=(Integer)invoke(fixturePage,"conditionBottom");
        int offset=(Integer)field(fixturePage,"renderedConditionOffset");
        Object layout=invoke(fixturePage,"conditionLayout");
        for(Object cell:(List<?>)invoke(layout,"cells")) {
            int x=(Integer)invoke(cell,"x"), y=top+(Integer)invoke(cell,"y")-offset;
            if(y<top || y+18>bottom)continue;
            List<?> source=(List<?>)invoke(cell,"source");
            Object check=invoke(cell,"check");int count=source.size();
            // The lower/right two pixels used to highlight but reject both actions.
            int px=x+17, py=y+17;
            if(((ItemStack)invoke(fixturePage,"hoveredConditionStack",px,py)).isEmpty())throw new IllegalStateException("Scrolled cell tooltip did not match its drawn slot");
            var left=new dev.xyat.kineticcore.api.client.gui.input.MouseInput(px,py,dev.xyat.kineticcore.api.client.gui.input.MouseButton.LEFT,0,0);
            if(!Boolean.TRUE.equals(invoke(fixturePage,"onMouseClick",left)) || !"WEAK".equals(field(check,"nbtMode")))throw new IllegalStateException("Scrolled cell left click missed its drawn edge");
            var right=new dev.xyat.kineticcore.api.client.gui.input.MouseInput(px,py,dev.xyat.kineticcore.api.client.gui.input.MouseButton.RIGHT,1,0);
            if(!Boolean.TRUE.equals(invoke(fixturePage,"onMouseClick",right)) || source.size()!=count-1 || source.contains(check))throw new IllegalStateException("Scrolled cell removed a different condition");
            LOG.info("ADVENTURE_GUI_CONDITION_ACTIONS tooltip=true left=true right=true offset={}",offset);
            return;
        }
        throw new IllegalStateException("No fully visible condition after scrolling");
    }
    private static void nextPhase() {
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        phase++;page=-1;
        String selectedPhases=System.getProperty("adventuresystems.guiValidation.phases", "1");
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
        LOG.info("ADVENTURE_GUI_OPEN phase={} case={} page={}",phase,NAMES[page],KineticGui.currentPage()!=null?KineticGui.currentPage().getClass().getName():String.valueOf(Minecraft.getInstance().screen));
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
            case 16 -> {
                // Ten coin items with holdings, so the coin list overflows and scrolls.
                dev.xyat.adventuresystems.curios.wallet.client.Client.handleSync(false,true,true,walletWithCoins());
                p=new MainScreen(balances(),true) {
                    // This unsaved fixture has synthetic balances, not a server-equipped wallet.
                    @Override protected void onTick() { }
                };
                walletStage=-1;
            }
            case 17,18 -> p=(KineticPage)construct(WALLET+"QuestManageScreen",draft);
            // The quest picker is part of the quest page since 2026-10-26.
            case 19 -> p=(KineticPage)construct(WALLET+"QuestManageScreen",draft);
            case 20,21,22,23 -> p=(KineticPage)construct(WALLET+"RewardPoolScreen",draft);
            case 24,25 -> p=new FTBItemBindingEditorScreen();
            case 26 -> {
                var file=questFile();var chapter=new Chapter(0x7000,file,null);var quest=new Quest(0x7001,chapter);
                var task=new ItemTask(0x7002,quest).setStackAndCount(new ItemStack(Items.EMERALD),32);
                p=new FTBSubmitCountScreen(task);
            }
            case 27 -> p=new SelectScreenFTB(namedStack(),refs());
            case 28 -> p=new ItemBlacklistScreenFTB();
            case 29 -> p=new FTBBlacklistScreen();
            case 31 -> p=Boolean.getBoolean("adventuresystems.guiValidation.tipsLayout")?tipLayoutSample():phase==4?overflowingTipEditor():new TipEditorScreen(Minecraft.getInstance().getLanguageManager().getSelected(),dev.xyat.adventuresystems.tips.config.ConfigLoader.getRawEntriesForLanguage(Minecraft.getInstance().getLanguageManager().getSelected()));
            case 30 -> {var tip=new HelpTip.JsonModel.Entry();tip.text=phase==4?LONG_NAME:"§6作物轮作§r\n小麦与胡萝卜隔行种\n可避开同类密植减速";tip.conditions=new HelpTip.JsonModel.Conditions();tip.conditions.biome="minecraft:plains";tip.conditions.structure="minecraft:village_plains";tip.conditions.dimension="minecraft:overworld";tip.conditions.advancement="minecraft:story/enter_the_nether";tip.conditions.curios=List.of();p=new TipEditorScreen("en_us",index==30?List.of():List.of(tip));}
            case 34 -> {dev.xyat.adventurevalidation.TipsRuntimeChecks.pause();return;}
            case 35 -> {dev.xyat.adventurevalidation.TipsRuntimeChecks.pause(14);return;}
            case 32 -> p=new TimeEditScreen(5000,(java.util.function.Consumer<Integer>)v->{});
            case 33 -> p=new TipSelectors.RegistrySelectorScreen("biomes",(java.util.function.Consumer<String>)v->{});
            case 36 -> p=new dev.xyat.adventuresystems.curios.wallet.client.hud.WalletHudEditorScreen();
            default -> throw new IllegalArgumentException("Unknown fixture page "+index);
        }
        KineticGui.open(p);
        if(index==13){setField(p,"selectedIndex",0);invoke(p,"selectCommand",0);}
        if(index==21 || index==22 || index==23)invoke(p,"selectReward",0);
        if(index==25){((List<RefFTB>)field(p,"visibleTasks")).addAll(refs());((List<RefFTB>)field(p,"allTasks")).addAll(refs());((List<RefFTB>)field(p,"boundTasks")).addAll(refs());for(RefFTB r:refs())((java.util.Set<Long>)field(p,"selectedQuestIds")).add(r.id());setField(p,"selectedStack",namedStack());setField(p,"dirty",true);var bound=(List<dev.xyat.adventuresystems.ftb.data.ItemBindingEntryFTB>)field(p,"explicitEntries");for(String id:new String[]{"minecraft:diamond_sword","minecraft:iron_pickaxe","minecraft:bread","minecraft:emerald","minecraft:bow","minecraft:shield","minecraft:golden_apple","minecraft:compass","minecraft:map","minecraft:book","minecraft:torch","minecraft:oak_log","minecraft:stone","minecraft:diamond","minecraft:iron_ingot","minecraft:gold_ingot","minecraft:apple","minecraft:carrot","minecraft:potato","minecraft:wheat","minecraft:arrow","minecraft:string","minecraft:feather","minecraft:leather","minecraft:bone","minecraft:coal"})bound.add(new dev.xyat.adventuresystems.ftb.data.ItemBindingEntryFTB(id));invoke(p,"updateItemScroll");invoke(p,"refreshRows");invoke(p,"updateSaveButton");}
    }
    private static CompoundTag walletWithCoins(){
        var tag=balances();var storage=new CompoundTag();
        String[] ids={"minecraft:gold_nugget","minecraft:gold_ingot","minecraft:gold_block","minecraft:diamond","minecraft:diamond_block","minecraft:emerald","minecraft:emerald_block","minecraft:netherite_scrap","minecraft:netherite_ingot","minecraft:netherite_block"};
        for(int i=0;i<ids.length;i++){var entry=new CompoundTag();entry.putLong("value",(long)Math.pow(10,i));entry.putLong("inventory",64L*i+3);entry.putIntArray("stacks",new int[]{64,64,i+3});storage.put(ids[i],entry);}
        tag.put(dev.xyat.adventuresystems.curios.wallet.network.Network.STORAGE_KEY,storage);return tag;
    }
    private static CompoundTag balances(){var tag=new CompoundTag();for(var currency:dev.xyat.adventuresystems.curios.wallet.data.Data.currencies())tag.putLong(currency.itemId(),123456789L);return tag;}
    //? if >=1.21 {
    /*private static ClientQuestFile questFile(){return ClientQuestFile.getInstance();}
    *///?} else {
    private static ClientQuestFile questFile(){return ClientQuestFile.INSTANCE;}
    //?}
    //? if >=1.21 {
    /*private static ItemStack namedStack(){var stack=new ItemStack(Items.DIAMOND_SWORD);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,dev.xyat.adventuresystems.text.AdventureText.literal(LONG_NAME));return stack;}
    *///?} else {
    private static ItemStack namedStack(){var stack=new ItemStack(Items.DIAMOND_SWORD);stack.setHoverName(dev.xyat.adventuresystems.text.AdventureText.literal(LONG_NAME));return stack;}
    //?}
    /** A tip with every condition type, so the stress pass can always scroll the conditions area. */
    private static KineticPage overflowingTipEditor(){var tip=new HelpTip.JsonModel.Entry();tip.text=LONG_NAME;tip.conditions=new HelpTip.JsonModel.Conditions();tip.conditions.biome="minecraft:plains";tip.conditions.structure="minecraft:village_plains";tip.conditions.dimension="minecraft:overworld";tip.conditions.advancement="minecraft:story/enter_the_nether";var item=new HelpTip.JsonModel.ItemCheck();item.id="minecraft:emerald";tip.conditions.items=List.of(item);tip.conditions.curios=List.of();return new TipEditorScreen("en_us",List.of(tip));}
    private static KineticPage tipLayoutSample() throws Exception {
        var codec=Class.forName("dev.xyat.textstudio.font.client.parser.CompactTagCodec");
        var encode=codec.getMethod("encodePreset",int.class,String.class);
        var entries=new ArrayList<HelpTip.JsonModel.Entry>();
        for(int i=0;i<32;i++) {
            var tip=new HelpTip.JsonModel.Entry();
            tip.text=(String)encode.invoke(null,1+i%8,"§b炫彩预览 §l"+(i+1));
            entries.add(tip);
        }
        var conditions=new HelpTip.JsonModel.Conditions();
        conditions.structure="minecraft:village_plains";
        conditions.biome="minecraft:plains";
        conditions.dimension="minecraft:overworld";
        conditions.advancement="minecraft:story/enter_the_nether";
        conditions.items=new ArrayList<>();conditions.curios=new ArrayList<>();
        for(int i=0;i<128;i++) {
            var item=new HelpTip.JsonModel.ItemCheck();item.id=i%2==0?"minecraft:emerald":"minecraft:diamond";
            conditions.items.add(item);
        }
        for(int i=0;i<96;i++) {
            var item=new HelpTip.JsonModel.ItemCheck();item.id="minecraft:iron_ingot";
            conditions.curios.add(item);
        }
        entries.get(0).conditions=conditions;
        return new TipEditorScreen("zh_cn",entries);
    }
    private static List<RefFTB> refs(){return List.of(new RefFTB(1,"0000000000000001",LONG_NAME,LONG_NAME,"local",LONG_NAME),new RefFTB(2,"0000000000000002","Short task","Short chapter","local","Short task"));}
    private static Shop.Entry entry(Shop.Mode mode,boolean gacha,boolean choice,boolean locked){
        var firstReward=namedStack();
        var secondReward=new ItemStack(Items.EMERALD);
        int rewardCount=Integer.getInteger("adventuresystems.guiValidation.rewardCount",1);
        firstReward.setCount(rewardCount);secondReward.setCount(rewardCount);
        var rewards=List.of(new Shop.Reward(firstReward,10,50.0D,LONG_NAME,""),new Shop.Reward(secondReward,10,50.0D,"Short reward",""));
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
        requireFixturePage("capture "+frame);
        var mc=Minecraft.getInstance();Path path=Path.of(ROOT,String.format("%d-%02d-%s-%s.png",phase,page,NAMES[page],frame));Files.createDirectories(path.getParent());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
        captures++;LOG.info("ADVENTURE_GUI_CAPTURE phase={} case={} image={}x{}",phase,NAMES[page],mc.getWindow().getWidth(),mc.getWindow().getHeight());
    }
    private static void verifyRewardPickerClip() throws Exception {
        requireFixturePage("reward picker clipping");
        int x=(int)invoke(fixturePage,"rewardPickerX"),y=(int)invoke(fixturePage,"rewardPickerY");
        int width=(int)invoke(fixturePage,"rewardPickerWidth");
        for(int[] point:new int[][]{{x+4,y+4},{x,y+20},{x+width-1,y+20}}) {
            if((int)invoke(fixturePage,"rewardPickerIndexAt",point[0],point[1])!=-1)
                throw new AssertionError("Reward picker title/border resolved a reward");
            if(!((ItemStack)invoke(fixturePage,"hoveredRewardPickerItem",point[0],point[1])).isEmpty())
                throw new AssertionError("Clipped reward picker area returned an item tooltip");
        }
        if((int)invoke(fixturePage,"rewardPickerIndexAt",x+2,y+18)!=0)
            throw new AssertionError("First visible reward pixel is not selectable");
        LOG.info("ADVENTURE_REWARD_PICKER_CLIP_PASS titleSafe=true borderSafe=true firstRow=true");
    }
    private static void verifyWalletHud() throws Exception {
        requireFixturePage("wallet HUD preview");
        var hud=fixturePage;
        int savedX=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudOffsetX;
        int savedY=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudOffsetY;
        double savedScale=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudScale;
        Object editor=field(hud,"editor"), snapshot=invoke(editor,"snapshot");
        var left=dev.xyat.kineticcore.api.client.gui.input.MouseButton.LEFT;
        try {
            if(hud.layout()!=dev.xyat.kineticcore.api.client.gui.page.PageLayout.NATIVE)throw new AssertionError("Wallet HUD must use native coordinates");
            invoke(hud,"resetLayout");
            var initial=(dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage.HudLayout)invoke(hud,"currentLayout");
            if(!Boolean.TRUE.equals(invoke(hud,"onMouseClickCapture",new dev.xyat.kineticcore.api.client.gui.input.MouseInput(initial.x()+1,initial.y()+1,left,0,0))))throw new AssertionError("Synthetic HUD drag did not start");
            if(!Boolean.TRUE.equals(invoke(hud,"onMouseDrag",new dev.xyat.kineticcore.api.client.gui.input.MouseDragInput(hud.width()+64,hud.height()+64,left,0,64,64))))throw new AssertionError("Synthetic HUD drag was not handled");
            invoke(hud,"onMouseRelease",new dev.xyat.kineticcore.api.client.gui.input.MouseInput(hud.width()+64,hud.height()+64,left,0,0));
            var moved=(dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage.HudLayout)invoke(hud,"currentLayout");
            if(moved.x()!=Math.max(0,hud.width()-(int)invoke(hud,"scaledElementWidth")) || moved.y()!=Math.max(0,hud.height()-(int)invoke(hud,"scaledElementHeight")))throw new AssertionError("Wallet HUD drag exceeded the screen");
            for(int i=0;i<20;i++) {
                var current=(dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage.HudLayout)invoke(hud,"currentLayout");
                if(!Boolean.TRUE.equals(invoke(hud,"onMouseScroll",new dev.xyat.kineticcore.api.client.gui.input.ScrollInput(current.x()+1,current.y()+1,0,-1))))throw new AssertionError("Wallet HUD scale wheel was not handled");
            }
            var minimum=(dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage.HudLayout)invoke(hud,"currentLayout");
            if(minimum.scale()!=0.1D)throw new AssertionError("Wallet HUD did not retain its ten-percent minimum scale");
            invoke(hud,"resetLayout");
            var reset=(dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage.HudLayout)invoke(hud,"currentLayout");
            if(reset.scale()!=0.75D || reset.x()!=0 || reset.y()!=Math.max(0,hud.height()-(int)invoke(hud,"scaledElementHeight")))throw new AssertionError("Wallet HUD reset lost its default bottom-left layout");
            if(savedX!=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudOffsetX || savedY!=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudOffsetY || savedScale!=dev.xyat.adventuresystems.curios.config.CuriosConfig.walletHudScale)throw new AssertionError("HUD preview actions saved configuration");
            LOG.info("ADVENTURE_WALLET_HUD_PASS syntheticInput=true dragClamped=true minimumScale=true reset=true savedConfiguration=false osCursorMoved=false");
        } finally {
            invoke(editor,"restore",snapshot);
        }
    }
    private static void verifyWalletExpansion() throws Exception {
        requireFixturePage("wallet coin list");
        var list=(dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList<?>)field(fixturePage,"list");
        int coins=dev.xyat.adventuresystems.data.Nbt.keys(dev.xyat.adventuresystems.curios.wallet.client.Client.holdings()).size();
        if(list.items().size()!=coins)throw new IllegalStateException("Wallet must list every coin item it takes: rows="+list.items().size()+" coins="+coins);
        capture("balance");
    }
    private static void requireFixturePage(String operation) {
        if (KineticGui.currentPage()!=fixturePage) throw new IllegalStateException("Fixture page is no longer displayed during "+operation
                +": phase="+phase+" case="+NAMES[page]+" expected="+fixturePage+" actual="+KineticGui.currentPage()+" screen="+Minecraft.getInstance().screen);
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
        try {
            var output=Path.of(ROOT);Files.createDirectories(output);
            Files.writeString(output.resolve("result.txt"),(failures==0?"PASS":"FAIL")+" captures="+captures+" failures="+failures+System.lineSeparator());
        } catch(java.io.IOException error) { LOG.error("ADVENTURE_GUI_RESULT_FAIL",error); }
        if(failures==0 && Boolean.getBoolean("adventuresystems.guiValidation.thenTooltip")) {
            try {
                Class.forName("dev.xyat.contentstudiovalidation.TooltipNavigationValidation").getMethod("install").invoke(null);
                return;
            } catch(ReflectiveOperationException error) {
                LOG.error("ADVENTURE_GUI_CHAIN_FAIL",error);
            }
        }
        String nextValidation=System.getProperty("adventuresystems.guiValidation.thenValidation", "");
        if(failures==0 && !nextValidation.isBlank()) {
            try {
                Class.forName(nextValidation).getMethod("install").invoke(null);
                return;
            } catch(ReflectiveOperationException error) {
                LOG.error("ADVENTURE_GUI_CHAIN_FAIL",error);
            }
        }
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
//?}
