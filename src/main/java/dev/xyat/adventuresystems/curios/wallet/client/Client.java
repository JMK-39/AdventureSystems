package dev.xyat.adventuresystems.curios.wallet.client;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.adventuresystems.curios.wallet.client.gui.MainScreen;
import dev.xyat.adventuresystems.curios.wallet.client.gui.ShopScreen;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.input.KineticKeyBindings;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class Client {
    private static final CompoundTag EMPTY = new CompoundTag();
    private static final long SHOP_PARENT_TIMEOUT_MS = 10_000L;
    private static KineticKeyBindings.Binding openKey;
    private static CompoundTag hudBalances = new CompoundTag();
    private static boolean hudVisible;
    private static Screen pendingShopParent;
    private static long pendingShopParentExpiresAt;

    private Client() {
    }

    public static void install() {
        if (openKey == null) {
            openKey = KineticKeyBindings.builder("key.adventuresystems.open_currency_wallet")
                    .category("key.categories.adventuresystems")
                    .context(KineticKeyBindings.Context.IN_GAME)
                    .keyboard(KineticKeyBindings.Key.U)
                    .enabledWhen(() -> KineticClientRuntime.localPlayer() != null)
                    .onPressed(() -> {
                        Network.sendOpen();
                        return true;
                    })
                    .register();
        }
        KineticClientEvents.onLogin(Client::clearClientWalletState);
        KineticClientEvents.onLogout(Client::clearClientWalletState);
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, Client::onClientTick);
    }

    public static void handleSync(boolean open, boolean visible, boolean equipped, CompoundTag balances) {
        Screen current = KineticClientRuntime.currentScreen();

        if (!equipped) {
            hudVisible = false;
            hudBalances = new CompoundTag();
            if (current instanceof MainScreen || current instanceof ShopScreen) {
                KineticClientRuntime.openScreen(null);
            }
            return;
        }

        hudVisible = visible;
        hudBalances = balances == null ? new CompoundTag() : balances.copy();

        if (open) {
            KineticClientRuntime.openScreen(new MainScreen(hudBalances, hudVisible));
            return;
        }

        if (current instanceof MainScreen screen) {
            screen.updateBalances(hudBalances, hudVisible);
        } else if (current instanceof ShopScreen screen) {
            screen.updateBalances(hudBalances);
        }
    }

    public static void openShop(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        Screen current = KineticClientRuntime.currentScreen();
        Screen parent = takePendingShopParent();
        if (parent == null && current instanceof MainScreen) parent = current;
        CompoundTag safeBalances = balances == null ? new CompoundTag() : balances.copy();
        CompoundTag safeShop = shop == null ? new CompoundTag() : shop.copy();
        hudBalances = safeBalances.copy();
        if (current instanceof ShopScreen screen) {
            screen.updateShop(safeBalances, safeShop, editorMode);
            return;
        }
        KineticClientRuntime.openScreen(new ShopScreen(parent, safeBalances, safeShop, editorMode));
    }

    public static void requestOpenShopEditor() {
        pendingShopParent = KineticClientRuntime.currentScreen();
        pendingShopParentExpiresAt = System.currentTimeMillis() + SHOP_PARENT_TIMEOUT_MS;
        Network.sendOpenShopEditor();
    }

    public static void refreshShopIfOpen(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        CompoundTag safeBalances = balances == null ? new CompoundTag() : balances.copy();
        CompoundTag safeShop = shop == null ? new CompoundTag() : shop.copy();
        hudBalances = safeBalances.copy();
        if (KineticClientRuntime.currentScreen() instanceof ShopScreen screen) {
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
            message = KineticI18n.translatable(key, Component.literal(openKeyName()));
        } else {
            message = args.length == 0 ? KineticI18n.translatable(key) : KineticI18n.translatable(key, args);
        }
        KineticOverlays.toast(
                "currency_wallet_notice",
                message,
                KineticOverlays.Position.BOTTOM_CENTER,
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
        if (value.startsWith("tr:")) return KineticI18n.translatable(value.substring(3));
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
        Screen current = KineticClientRuntime.currentScreen();
        if (current instanceof MainScreen || current instanceof ShopScreen) {
            KineticClientRuntime.openScreen(null);
        }
    }

    private static Screen takePendingShopParent() {
        Screen parent = System.currentTimeMillis() <= pendingShopParentExpiresAt ? pendingShopParent : null;
        clearPendingShopParent();
        return parent;
    }

    private static void clearPendingShopParent() {
        pendingShopParent = null;
        pendingShopParentExpiresAt = 0L;
    }

    public static String openKeyName() {
        return openKey == null ? "U" : openKey.translatedKeyMessage().getString();
    }

    public static void appendWalletTooltip(ItemStack walletStack, List<Component> tooltip) {
        List<CurrencyType> currencies = Data.currencies();
        if (currencies.isEmpty()) return;
        CompoundTag balances = Data.snapshot(walletStack);
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.wallet.amounts_title"));
        for (CurrencyType currency : currencies) {
            ItemStack stack = new ItemStack(currency.item());
            long amount = Data.readAmount(balances, currency.itemId());
            tooltip.add(KineticI18n.translatable(
                    "tip.adventuresystems.curios.wallet.amount_line",
                    KineticI18n.translatable(stack.getDescriptionId()),
                    Component.literal(formatExact(amount))
            ));
        }
        tooltip.add(KineticI18n.translatable(Data.isMagnetDisabled(walletStack)
                ? "tip.adventuresystems.curios.wallet.magnet_disabled"
                : "tip.adventuresystems.curios.wallet.magnet_enabled"));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.wallet.right_click_controls"));
        appendRsTooltip(walletStack, tooltip);
    }

    private static void appendRsTooltip(ItemStack walletStack, List<Component> tooltip) {
        if (!KineticPlatform.isModLoaded("refinedstorage")) return;
        Data.RsBinding binding = Data.rsBinding(walletStack);
        if (!binding.bound()) {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.wallet.rs_unbound"));
            return;
        }
        var level = KineticClientRuntime.currentLevel();
        BlockPos pos = binding.pos();
        if (level != null && level.dimension().location().toString().equals(binding.dimension())) {
            ResourceLocation id = KineticRegistries.blocks().id(level.getBlockState(pos).getBlock());
            if (id == null || !"refinedstorage".equals(id.getNamespace()) || !id.getPath().contains("controller")) {
                tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.wallet.rs_missing", pos.getX(), pos.getY(), pos.getZ()));
                return;
            }
        }
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.wallet.rs_bound", pos.getX(), pos.getY(), pos.getZ()));
    }

    private static String formatExact(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(Math.max(0L, value));
    }

    private static void onClientTick() {
        if (pendingShopParent != null && System.currentTimeMillis() > pendingShopParentExpiresAt) {
            clearPendingShopParent();
        }
    }
}
