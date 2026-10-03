//? if >=1.21 {
/*package dev.xyat.adventurevalidation;
@net.neoforged.fml.common.Mod("adventuresystems_validation")
public final class RuntimeValidation {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(RuntimeValidation.class);
    private static int failures, checks;
    private boolean tested;
    private static boolean clientTested;
    private static int clientTicks;
    private static long lastDiagnostic;
    public RuntimeValidation(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::login);
        dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> () -> {
            dev.xyat.kineticcore.api.client.event.KineticClientEvents.onTick(dev.xyat.kineticcore.api.client.event.KineticClientEvents.TickPhase.END, () -> {
                var level=dev.xyat.kineticcore.api.runtime.KineticClientRuntime.currentLevel();
                if(level==null || clientTested || ++clientTicks<100)return;clientTested=true;
                run("ftb-client-enchantment-registry",()-> {
                    var enchantment=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
                    var enchantments=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    enchantments.set(enchantment,2);
                    var book=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
                    book.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS,enchantments.toImmutable());
                    var encoded=dev.xyat.adventuresystems.ftb.data.FTBItemComponents.encode(book);
                    require(encoded.contains("minecraft:stored_enchantments"),"client enchantments retained");
                });
                run("styled-public-key-runtime-mixin",()-> {
                    var message=dev.xyat.adventuresystems.text.AdventureText.translatable("tip.adventuresystems.curios.global.bound_to","Player");
                    require(message.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents && contents.getKey().equals("tip.adventuresystems.curios.global.bound_to"),"wire uses public key");
                    require(!message.getString().contains("§") && message.getString().contains("Player"),"runtime Mixin decomposes colored public template");
                    String json=net.minecraft.network.chat.Component.Serializer.toJson(message,level.registryAccess());
                    require(!json.contains(".formatted"),"no helper translation keys transmitted");
                    var received=net.minecraft.network.chat.Component.Serializer.fromJson(json,level.registryAccess());
                    require(received!=null && received.getString().equals(message.getString()),"received native public-key translation rendered");
                });
                run("pause-tips-eligible-and-rendered",()-> {
                    var manager=dev.xyat.adventuresystems.tips.client.TipCache.TIP_MANAGER;
                    var screen=new net.minecraft.client.gui.screens.PauseScreen(true);screen.width=640;screen.height=360;
                    var original=dev.xyat.adventuresystems.tips.config.ConfigLoader.getRawEntriesForLanguage(dev.xyat.kineticcore.api.runtime.KineticClientRuntime.selectedLanguage());
                    LOG.info("ADVENTURE_TIPS_ELIGIBILITY originalEntries={} eligible={} structure={}",original.size(),manager.getValidTip(screen)!=null,dev.xyat.adventuresystems.tips.client.TipCache.currentStructure);
                    var entry=new dev.xyat.adventuresystems.tips.api.HelpTip.JsonModel.Entry();entry.stage="game";entry.text="§aPAUSE_TIP_RENDER_PROBE";
                    var probe=new ProbeGraphics();
                    var textCalls=probe.textCalls;
                    var graphics=dev.xyat.kineticcore.internal.client.gui.render.GuiGraphicsAdapter.wrap(probe);
                    try {
                        manager.replaceServerEntries(java.util.List.of(entry));dev.xyat.adventuresystems.tips.client.TipRenderer.refresh(screen);
                        var render=dev.xyat.adventuresystems.tips.client.TipRenderer.class.getDeclaredMethod("onScreenRender",net.minecraft.client.gui.screens.Screen.class,dev.xyat.kineticcore.api.client.gui.render.KineticGraphics.class,int.class,int.class,float.class);
                        render.setAccessible(true);render.invoke(null,screen,graphics,0,0,0f);
                        require(textCalls.contains("PAUSE_TIP_RENDER_PROBE"),"eligible game tip produced visible lower-left text");
                    } finally {manager.replaceServerEntries(original);dev.xyat.adventuresystems.tips.client.TipRenderer.refresh(screen);}
                });
                LOG.info("ADVENTURE_CLIENT_VALIDATION_{} checks={} failures={}",failures==0?"PASS":"FAIL",checks,failures);
                dev.xyat.kineticcore.api.runtime.KineticClientRuntime.execute(dev.xyat.kineticcore.api.runtime.KineticClientRuntime::stopClient);
            });
            dev.xyat.kineticcore.api.client.event.KineticClientEvents.onScreenRenderAfter((screen,graphics,x,y,tick)-> {
                if(!(screen instanceof net.minecraft.client.gui.screens.PauseScreen) || System.currentTimeMillis()-lastDiagnostic<5000)return;
                lastDiagnostic=System.currentTimeMillis();
                var tip=dev.xyat.adventuresystems.tips.client.TipCache.TIP_MANAGER.getValidTip(screen);
                LOG.info("ADVENTURE_TIPS_DIAGNOSTIC enabled={} structure={} eligible={}",dev.xyat.adventuresystems.tips.config.GeneralConfig.isEnabled(),dev.xyat.adventuresystems.tips.client.TipCache.currentStructure,tip==null?"none":tip.getText().getString());
            });
        });
    }
    private void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if(tested || !(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player))return;tested=true;failures=0;checks=0;
        for(var type:java.util.List.of(AccessoryComponentsTest.class,WalletComponentsTest.class)){
            try{var ctor=type.getDeclaredConstructor();ctor.setAccessible(true);var obj=ctor.newInstance();
                for(var method:type.getDeclaredMethods())if(method.isAnnotationPresent(org.junit.jupiter.api.Test.class))run(method.getName(),()->{method.setAccessible(true);method.invoke(obj);});
            }catch(Throwable error){failures++;LOG.error("ADVENTURE_CHECK_FAIL test-class",error);}
        }
        run("ftb-native-component-bindings",FTBItemComponentsTest::verify);
        run("ftb-submission-permission-gates",FTBSubmitPermissionTest::run);
        run("tips-native-component-matching-and-schema",TipsComponentTest::verify);
        run("wallet-balance-identity-magnet-binding-native-save",()->{
            var wallet=new net.minecraft.world.item.ItemStack(dev.xyat.adventuresystems.curios.init.Items.CURRENCY_WALLET.get());
            var pos=player.blockPosition();
            dev.xyat.adventuresystems.curios.wallet.data.Data.ensureWalletIdentity(wallet);
            var identity=dev.xyat.adventuresystems.data.AdventureItemData.customData(wallet).getUUID("WalletId");
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.add(wallet,"minecraft:emerald",Long.MAX_VALUE)==Long.MAX_VALUE,"full long balance accepted");
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.add(wallet,"minecraft:emerald",1)==0,"balance overflow prevented");
            var copy=wallet.copy();dev.xyat.adventuresystems.curios.wallet.data.Data.remove(copy,"minecraft:emerald",5);
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.amount(wallet,"minecraft:emerald")==Long.MAX_VALUE,"wallet copy independent");
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.toggleMagnetDisabled(wallet),"magnet toggle");
            dev.xyat.adventuresystems.curios.wallet.data.Data.bindRsController(wallet,player.serverLevel(),pos);
            var loaded=net.minecraft.world.item.ItemStack.parseOptional(player.registryAccess(),(net.minecraft.nbt.CompoundTag)wallet.save(player.registryAccess()));
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.amount(loaded,"minecraft:emerald")==Long.MAX_VALUE,"native balance saved");
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.isMagnetDisabled(loaded),"native magnet saved");
            require(dev.xyat.adventuresystems.curios.wallet.data.Data.rsBinding(loaded).pos().equals(pos),"native binding saved");
            require(dev.xyat.adventuresystems.data.AdventureItemData.customData(loaded).getUUID("WalletId").equals(identity),"wallet identity preserved");
        });
        run("curios-inventory-and-accessory-owner-binding",()->{
            require(top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).isPresent(),"curios capability available");
            var context=new top.theillusivec4.curios.api.SlotContext("curio",player,0,false,true);
            var heart=new net.minecraft.world.item.ItemStack(dev.xyat.adventuresystems.curios.init.Items.HEART_OF_STEEL.get());
            ((dev.xyat.adventuresystems.curios.heartofsteel.item.HeartOfSteelItem)heart.getItem()).onEquip(context,net.minecraft.world.item.ItemStack.EMPTY,heart);
            require(dev.xyat.adventuresystems.data.AdventureItemData.customData(heart).getUUID("adventuresystems_owner_id").equals(player.getUUID()),"heart owner component stored");
            var paradise=new net.minecraft.world.item.ItemStack(dev.xyat.adventuresystems.curios.init.Items.PARADISE_LOST.get());
            ((dev.xyat.adventuresystems.curios.paradiselost.item.ParadiseLostItem)paradise.getItem()).onEquip(context,net.minecraft.world.item.ItemStack.EMPTY,paradise);
            require(dev.xyat.adventuresystems.data.AdventureItemData.customData(paradise).getUUID("adventuresystems_owner_id").equals(player.getUUID()),"paradise owner component stored");
        });
        LOG.info("ADVENTURE_VALIDATION_{} checks={} failures={}",failures==0?"PASS":"FAIL",checks,failures);
    }
    private static final class ProbeGraphics extends net.minecraft.client.gui.GuiGraphics {
        final java.util.List<String> textCalls=new java.util.ArrayList<>();
        ProbeGraphics(){super(net.minecraft.client.Minecraft.getInstance(),net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource());}
        private int record(String text,float x,float y){require(x<100 && y>250 && y<360,"tip inside lower-left viewport");textCalls.add(text);return 0;}
        @Override public int drawString(net.minecraft.client.gui.Font font,String text,int x,int y,int color,boolean shadow){return record(text,x,y);}
        @Override public int drawString(net.minecraft.client.gui.Font font,net.minecraft.network.chat.Component text,int x,int y,int color,boolean shadow){return record(text.getString(),x,y);}
        @Override public int drawString(net.minecraft.client.gui.Font font,net.minecraft.util.FormattedCharSequence text,int x,int y,int color,boolean shadow){var value=new StringBuilder();text.accept((i,style,cp)->{value.appendCodePoint(cp);return true;});return record(value.toString(),x,y);}
    }
    private interface Checked{void run() throws Throwable;}
    private static void run(String name,Checked check){checks++;try{check.run();LOG.info("ADVENTURE_CHECK_PASS {}",name);}catch(Throwable error){failures++;LOG.error("ADVENTURE_CHECK_FAIL "+name,error);}}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}

*///?}
