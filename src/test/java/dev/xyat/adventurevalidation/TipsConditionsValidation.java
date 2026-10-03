//? if >=1.21 {
/*package dev.xyat.adventurevalidation;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.util.TipsStructureUtil;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TipsConditionsValidation {
    private static final Logger LOG=LoggerFactory.getLogger(TipsConditionsValidation.class);
    private static final String VILLAGE="minecraft:village_plains";
    private static ServerPlayer player;
    private static net.minecraft.server.level.ServerLevel originalLevel;
    private static ListTag originalInventory;
    private static int originalSlot;
    private static Vec3 originalPosition;
    private static float originalYaw,originalPitch;
    private static BlockPos villagePosition,outsidePosition;
    private static List<HelpTip.JsonModel.Entry> originalEntries;
    private static HelpTip.JsonModel.Entry swordEntry,villageEntry,combinedEntry;
    private static volatile boolean prepared,restored;
    private static volatile Throwable serverError;
    private static int scenario,state,checks,failures,frames;
    private static long deadline,scenarioOpened;
    private static final java.util.Set<String> combinationShown=new java.util.HashSet<>();
    private static boolean started,finishing;
    private static final String[] NAMES={"empty-outside","sword-only-outside","damaged-sword-outside","village-empty","village-and-sword","leave-village-empty"};
    private static final int[] SWORD_DAMAGE={-1,0,1,-1,0,-1};

    public static void start() {
        if(started)return;started=true;
        var mc=Minecraft.getInstance();
        var server=mc.getSingleplayerServer();
        if(server==null){throw new IllegalStateException("Existing integrated server required");}
        player=server.getPlayerList().getPlayer(mc.player.getUUID());
        originalEntries=ConfigLoader.getRawEntriesForLanguage(dev.xyat.kineticcore.api.runtime.KineticClientRuntime.selectedLanguage());
        swordEntry=originalEntries.stream().filter(e->e.conditions!=null && e.conditions.items!=null &&
                e.conditions.items.stream().anyMatch(i->"minecraft:netherite_sword".equals(i.id))).findFirst().orElseThrow();
        villageEntry=originalEntries.stream().filter(e->e.conditions!=null && VILLAGE.equals(e.conditions.structure)).findFirst().orElseThrow();
        combinedEntry=new HelpTip.JsonModel.Entry();
        combinedEntry.stage="game";combinedEntry.text="§6[组合条件验证] §f在村庄内持有未损坏的下界合金剑。";
        combinedEntry.conditions=new HelpTip.JsonModel.Conditions();
        combinedEntry.conditions.structure=VILLAGE;
        combinedEntry.conditions.items=swordEntry.conditions.items;
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END,TipsConditionsValidation::tick);
        KineticClientEvents.onScreenRenderAfter((screen,g,x,y,t)->{if(screen instanceof PauseScreen)frames++;});
        mc.setScreen(null);deadline=System.currentTimeMillis()+60000;
        server.execute(()->{
            try {
                originalLevel=player.serverLevel();originalInventory=player.getInventory().save(new ListTag()).copy();
                originalSlot=player.getInventory().selected;originalPosition=player.position();originalYaw=player.getYRot();originalPitch=player.getXRot();
                locateVillage();
                setupScenario();
            } catch(Throwable error){serverError=error;}
        });
    }

    private static void locateVillage() {
        var level=player.server.overworld();
        var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var holder=registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.parse(VILLAGE)));
        var located=level.getChunkSource().getGenerator().findNearestMapStructure(level,HolderSet.direct(holder),player.blockPosition(),4,false);
        require(located!=null,"real plains village found");
        level.getChunkAt(located.getFirst());
        for(var start:level.structureManager().startsForStructure(new net.minecraft.world.level.ChunkPos(located.getFirst()),
                structure->ResourceLocation.parse(VILLAGE).equals(registry.getKey(structure)))) {
            if(!start.isValid())continue;
            for(var piece:start.getPieces()) {
                var box=piece.getBoundingBox();
                for(int y=box.minY();y<box.maxY();y++) for(int x=box.minX()+1;x<box.maxX();x++) for(int z=box.minZ()+1;z<box.maxZ();z++) {
                    var pos=new BlockPos(x,y,z);
                    if(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                            && !level.getBlockState(pos.below()).isAir() && TipsStructureUtil.getStructuresAt(level,pos).contains(VILLAGE)) {
                        villagePosition=pos;break;
                    }
                }
                if(villagePosition!=null)break;
            }
            if(villagePosition!=null)break;
        }
        require(villagePosition!=null,"standable position inside actual village piece found");
        for(int offset=0;offset<8;offset++) {
            int x=originalPosition==null?0:(int)originalPosition.x+offset*128;
            int z=originalPosition==null?0:(int)originalPosition.z;
            var pos=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            if(TipsStructureUtil.getStructuresAt(level,pos).isEmpty()){outsidePosition=pos;break;}
        }
        require(outsidePosition!=null,"outside position with no structure found");
        LOG.info("ADVENTURE_CONDITION_LOCATIONS village={} outside={}",villagePosition,outsidePosition);
    }

    private static boolean village(){return scenario==3 || scenario==4;}
    private static void setupScenario() {
        prepared=false;
        var pos=village()?villagePosition:outsidePosition;
        player.getInventory().clearContent();player.getInventory().selected=0;
        if(SWORD_DAMAGE[scenario]>=0) {
            var sword=new ItemStack(Items.NETHERITE_SWORD);sword.setDamageValue(SWORD_DAMAGE[scenario]);
            player.getInventory().setItem(0,sword);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        player.teleportTo(player.server.overworld(),pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);
        var actual=TipsStructureUtil.getStructuresAt(player.serverLevel(),player.blockPosition());
        require(village()?actual.contains(VILLAGE):actual.isEmpty(),"server environment matches scenario "+NAMES[scenario]);
        LOG.info("ADVENTURE_CONDITION_SETUP name={} pos={} structure={} mainhand={} damage={}",
                NAMES[scenario],pos,actual,player.getMainHandItem(),player.getMainHandItem().getDamageValue());
        prepared=true;
    }

    private static void tick() {
        if(!started)return;
        try {
            if(serverError!=null){throw new IllegalStateException("Server setup failed",serverError);}
            if(finishing) {
                if(!restored)return;
                LOG.info("ADVENTURE_CONDITIONS_{} checks={} failures={} restored=true",failures==0?"PASS":"FAIL",checks,failures);
                started=false;
                dev.xyat.kineticcore.api.runtime.KineticClientRuntime.stopClient();
                return;
            }
            require(System.currentTimeMillis()<deadline,"condition scenario timed out: "+NAMES[scenario]+" state="+state);
            var mc=Minecraft.getInstance();
            if(mc.player==null || !prepared)return;
            if(state==0) {
                var pos=village()?villagePosition:outsidePosition;
                var held=mc.player.getMainHandItem();
                boolean inventoryReady=SWORD_DAMAGE[scenario]<0 ? held.isEmpty()
                        : held.is(Items.NETHERITE_SWORD) && held.getDamageValue()==SWORD_DAMAGE[scenario];
                if(!inventoryReady || !mc.player.blockPosition().equals(pos))return;
                TipCache.TIP_MANAGER.replaceServerEntries(List.of(swordEntry,villageEntry,combinedEntry));
                frames=0;mc.setScreen(new PauseScreen(true));state=1;
                return;
            }
            if(state==1) {
                String expected=village()?VILLAGE:"";
                if(!expected.equals(TipCache.currentStructure))return;
                boolean sword=SWORD_DAMAGE[scenario]==0;
                assertSelection(swordEntry,sword,"native-undamaged-sword");
                assertSelection(villageEntry,village(),"village");
                assertSelection(combinedEntry,sword&&village(),"village-and-native-sword");
                if(scenario==4){
                    TipCache.TIP_MANAGER.replaceServerEntries(List.of(swordEntry,villageEntry));
                } else if(village() || sword) {
                    TipCache.TIP_MANAGER.replaceServerEntries(List.of(village()?villageEntry:swordEntry));
                } else TipCache.TIP_MANAGER.replaceServerEntries(List.of(swordEntry,villageEntry,combinedEntry));
                TipRenderer.refresh(mc.screen);frames=0;scenarioOpened=System.currentTimeMillis();state=2;
                if(scenario==4)deadline=System.currentTimeMillis()+40000;
                return;
            }
            if(state==2 && frames>=30) {
                var field=TipRenderer.class.getDeclaredField("currentTip");field.setAccessible(true);
                var tip=(HelpTip)field.get(null);
                if(scenario==4) {
                    require(tip!=null,"combined scene selects conditional tips");
                    String swordText=dev.xyat.adventuresystems.text.AdventureText.literal(swordEntry.text).getString();
                    String villageText=dev.xyat.adventuresystems.text.AdventureText.literal(villageEntry.text).getString();
                    String text=tip.getText().getString();
                    require(text.equals(swordText)||text.equals(villageText),"combined scene only shows matched original condition tips");
                    if(combinationShown.add(text)) {
                        String kind=text.equals(swordText)?"sword":"village";
                        try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                            image.writeToFile(java.nio.file.Path.of("D:/IDEAWork/AdventureSystems/.gradle/migration/tips-condition-combined-"+kind+".png"));
                        }
                        LOG.info("ADVENTURE_CONDITION_COMBINED_SHOWN kind={} text={}",kind,text);
                    }
                    if(System.currentTimeMillis()-scenarioOpened<16000 || combinationShown.size()<2)return;
                    checks++;require(combinationShown.contains(swordText)&&combinationShown.contains(villageText),"real paused menu rotates both matching conditions");
                    LOG.info("ADVENTURE_CONDITION_COMBINED_ROTATION_PASS distinctTips={} frames={}",combinationShown.size(),frames);
                }
                if(village() || SWORD_DAMAGE[scenario]==0) {
                    require(tip!=null,"real conditional pause tip selected");
                    try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                        image.writeToFile(java.nio.file.Path.of("D:/IDEAWork/AdventureSystems/.gradle/migration/tips-condition-"+NAMES[scenario]+".png"));
                    }
                    LOG.info("ADVENTURE_CONDITION_SCREEN name={} text={} structure={} frames={}",
                            NAMES[scenario],tip.getText().getString(),TipCache.currentStructure,frames);
                } else require(tip==null,"unmatched conditions must not render");
                LOG.info("ADVENTURE_CONDITION_SCENARIO_PASS {}",NAMES[scenario]);
                mc.setScreen(null);
                if(++scenario==NAMES.length){finish();return;}
                prepared=false;state=0;deadline=System.currentTimeMillis()+15000;
                player.server.execute(()->{try{setupScenario();}catch(Throwable error){serverError=error;}});
            }
        } catch(Throwable error){failures++;LOG.error("ADVENTURE_CONDITION_FAIL scenario="+scenario,error);finish();}
    }

    private static void assertSelection(HelpTip.JsonModel.Entry entry,boolean expected,String label) {
        TipCache.TIP_MANAGER.replaceServerEntries(List.of(entry));
        boolean actual=TipCache.TIP_MANAGER.getValidTip(Minecraft.getInstance().screen)!=null;
        checks++;require(actual==expected,NAMES[scenario]+" "+label+" expected="+expected+" actual="+actual);
        LOG.info("ADVENTURE_CONDITION_CHECK_PASS scenario={} condition={} matches={}",NAMES[scenario],label,actual);
    }

    private static void finish() {
        if(finishing)return;finishing=true;
        Minecraft.getInstance().setScreen(null);
        TipCache.TIP_MANAGER.replaceServerEntries(originalEntries);
        player.server.execute(()->{
            try {
                if(originalInventory!=null){
                    player.getInventory().load(originalInventory.copy());player.getInventory().selected=originalSlot;
                    player.getInventory().setChanged();player.inventoryMenu.broadcastChanges();player.containerMenu.broadcastChanges();
                    player.teleportTo(originalLevel,originalPosition.x,originalPosition.y,originalPosition.z,originalYaw,originalPitch);
                    LOG.info("ADVENTURE_CONDITION_RESTORED pos={} inventory={}",originalPosition,player.getInventory().save(new ListTag()).equals(originalInventory));
                }
            }catch(Throwable error){failures++;LOG.error("ADVENTURE_CONDITION_RESTORE_FAIL",error);}
            restored=true;
        });
    }

    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
*///?}
