package dev.xyat.adventuresystems.curios.wallet.client;

import dev.xyat.adventuresystems.curios.util.ColorText;
import com.mojang.blaze3d.platform.InputConstants;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.wallet.client.gui.MainScreen;
import dev.xyat.adventuresystems.curios.wallet.client.gui.ShopScreen;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = CuriosModule.MODID, value = Dist.CLIENT)
public class Client {
    private static final CompoundTag EMPTY = new CompoundTag();
    private static final long SHOP_PARENT_TIMEOUT_MS = 10_000L;
    private static KeyMapping openKey;
    private static CompoundTag hudBalances = new CompoundTag();
    private static boolean hudVisible;
    private static Screen pendingShopParent;
    private static long pendingShopParentExpiresAt;

    public static void handleSync(boolean open, boolean visible, boolean equipped, CompoundTag balances) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!equipped) {
            hudVisible = false;
            hudBalances = new CompoundTag();
            if (minecraft.screen instanceof MainScreen || minecraft.screen instanceof ShopScreen) {
                minecraft.setScreen(null);
            }
            return;
        }

        hudVisible = visible;
        hudBalances = balances == null ? new CompoundTag() : balances.copy();

        if (open) {
            minecraft.setScreen(new MainScreen(hudBalances, hudVisible));
            return;
        }

        if (minecraft.screen instanceof MainScreen screen) {
            screen.updateBalances(hudBalances, hudVisible);
        } else if (minecraft.screen instanceof ShopScreen screen) {
            screen.updateBalances(hudBalances);
        }
    }

    public static void openShop(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen parent = takePendingShopParent();
        CompoundTag safeBalances = balances == null ? new CompoundTag() : balances.copy();
        CompoundTag safeShop = shop == null ? new CompoundTag() : shop.copy();
        hudBalances = safeBalances.copy();
        if (minecraft.screen instanceof ShopScreen screen) {
            screen.updateShop(safeBalances, safeShop, editorMode);
            return;
        }
        minecraft.setScreen(new ShopScreen(parent, safeBalances, safeShop, editorMode));
    }

    /** Saves the config page as the return target before requesting the server editor. */
    public static void requestOpenShopEditor() {
        Minecraft minecraft = Minecraft.getInstance();
        pendingShopParent = minecraft.screen;
        pendingShopParentExpiresAt = System.currentTimeMillis() + SHOP_PARENT_TIMEOUT_MS;
        Network.sendOpenShopEditor();
    }

    public static void refreshShopIfOpen(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        Minecraft minecraft = Minecraft.getInstance();
        CompoundTag safeBalances = balances == null ? new CompoundTag() : balances.copy();
        CompoundTag safeShop = shop == null ? new CompoundTag() : shop.copy();
        hudBalances = safeBalances.copy();
        if (minecraft.screen instanceof ShopScreen screen) {
            screen.updateShop(safeBalances, safeShop, editorMode);
        }
    }

    public static void showToast(String translationKey) {
        if (translationKey == null || translationKey.isBlank()) return;
        String[] split = translationKey.split("\\|", -1);
        String key = split.length == 0 ? translationKey : split[0];
        if ("msg.adventuresystems.curios.wallet.shop_editor_mode_no_permission".equals(key)) {
            clearPendingShopParent();
        }
        Object[] args = split.length <= 1 ? new Object[0] : unpackToastArgs(split);
        Component message;
        if ("msg.adventuresystems.curios.wallet.merchant_manual_exchange_required".equals(key) && args.length == 0) {
            message = ColorText.translatable(key, Component.literal(openKeyName()).withStyle(ChatFormatting.YELLOW));
        } else {
            message = args.length == 0 ? ColorText.translatable(key) : ColorText.translatable(key, args);
        }
        GuiOverlay.toast(
                "currency_wallet_notice",
                message,
                GuiOverlay.Position.BOTTOM_CENTER,
                3500,
                0,
                -30
        );
    }

    private static Object[] unpackToastArgs(String[] split) {
        Object[] args = new Object[split.length - 1];
        for (int i = 1; i < split.length; i++) {
            args[i - 1] = unpackToastArg(split[i]);
        }
        return args;
    }

    private static Object unpackToastArg(String value) {
        if (value == null) return "";
        if (value.startsWith("tr:")) return ColorText.translatable(value.substring(3));
        return value;
    }

    public static boolean hudVisible() {
        return hudVisible;
    }

    public static CompoundTag hudBalances() {
        return hudBalances == null ? EMPTY : hudBalances;
    }

    public static void clearClientWalletState() {
        hudVisible = false;
        hudBalances = new CompoundTag();
        clearPendingShopParent();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof MainScreen || minecraft.screen instanceof ShopScreen) {
            minecraft.setScreen(null);
        }
    }

    private static Screen takePendingShopParent() {
        Screen parent = System.currentTimeMillis() <= pendingShopParentExpiresAt
                ? pendingShopParent
                : null;
        clearPendingShopParent();
        return parent;
    }

    private static void clearPendingShopParent() {
        pendingShopParent = null;
        pendingShopParentExpiresAt = 0L;
    }


    public static String openKeyName() {
        if (openKey == null) return "U";
        return openKey.getTranslatedKeyMessage().getString();
    }

    public static void appendWalletTooltip(ItemStack walletStack, List<Component> tooltip) {
        List<CurrencyType> currencies = Data.currencies();
        if (currencies.isEmpty()) return;
        CompoundTag balances = Data.snapshot(walletStack);
        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.wallet.amounts_title"));
        for (CurrencyType currency : currencies) {
            ItemStack stack = new ItemStack(currency.item());
            long amount = Data.readAmount(balances, currency.itemId());
            tooltip.add(ColorText.translatable(
                    "tip.adventuresystems.curios.wallet.amount_line",
                    ColorText.translatable(stack.getDescriptionId()),
                    Component.literal(formatExact(amount)).withStyle(ChatFormatting.AQUA)
            ));
        }
        tooltip.add(ColorText.translatable(Data.isMagnetDisabled(walletStack) ? "tip.adventuresystems.curios.wallet.magnet_disabled" : "tip.adventuresystems.curios.wallet.magnet_enabled"));
        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.wallet.right_click_controls"));
        appendRsTooltip(walletStack, tooltip);
    }

    private static void appendRsTooltip(ItemStack walletStack, List<Component> tooltip) {
        if (!ModList.get().isLoaded("refinedstorage")) return;
        Data.RsBinding binding = Data.rsBinding(walletStack);
        if (!binding.bound()) {
            tooltip.add(ColorText.translatable("tip.adventuresystems.curios.wallet.rs_unbound"));
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        BlockPos pos = binding.pos();
        if (minecraft.level != null && minecraft.level.dimension().location().toString().equals(binding.dimension())) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(minecraft.level.getBlockState(pos).getBlock());
            if (id == null || !"refinedstorage".equals(id.getNamespace()) || !id.getPath().contains("controller")) {
                tooltip.add(ColorText.translatable("tip.adventuresystems.curios.wallet.rs_missing", pos.getX(), pos.getY(), pos.getZ()));
                return;
            }
        }
        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.wallet.rs_bound", pos.getX(), pos.getY(), pos.getZ()));
    }

    private static String formatExact(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(Math.max(0L, value));
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        clearClientWalletState();
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearClientWalletState();
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || openKey == null) return;
        while (openKey.consumeClick()) {
            Network.sendOpen();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && pendingShopParent != null
                && System.currentTimeMillis() > pendingShopParentExpiresAt) {
            clearPendingShopParent();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = CuriosModule.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            openKey = new KeyMapping(
                    "key.adventuresystems.open_currency_wallet",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_U,
                    "key.categories.adventuresystems"
            );
            event.register(openKey);
        }
    }
}
