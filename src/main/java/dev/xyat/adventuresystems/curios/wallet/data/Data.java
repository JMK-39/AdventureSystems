package dev.xyat.adventuresystems.curios.wallet.data;

import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;

public final class Data {
    private static final String ROOT_KEY = "CuriosModuleCurrencyWallet";
    private static final String HUD_HIDDEN_KEY = "HudHidden";
    private static final String WALLET_ID_KEY = "WalletId";
    private static final String WALLET_BALANCES_KEY = "Balances";
    private static final String RS_BINDING_KEY = "RsControllerBinding";
    private static final String RS_BOUND_DIM_KEY = "Dimension";
    private static final String RS_BOUND_X_KEY = "X";
    private static final String RS_BOUND_Y_KEY = "Y";
    private static final String RS_BOUND_Z_KEY = "Z";
    private static volatile CurrencyCache currencyCache;
    /** The wallet's one currency: wallet coins, kept as a number in the wallet (its item only serves as the icon). */
    public static final String VIRTUAL_ID = "adventuresystems:adventure_coin";

    private Data() {
    }

    public static void invalidateCurrencyCache() {
        currencyCache = null;
    }

    /** The wallet's currencies: only the wallet coin. */
    public static List<CurrencyType> currencies() {
        return currencyCache().currencies();
    }

    public static Map<String, CurrencyType> currencyMap() {
        return currencyCache().currencyMap();
    }

    /** The coin items the wallet takes in (and the wallet coin itself), with their value in wallet coins. */
    public static Map<String, CurrencyType> coinMap() {
        return currencyCache().coinMap();
    }

    /** Whether the stack is a coin item the wallet takes in. */
    public static boolean isCurrencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = KineticRegistries.items().id(stack.getItem());
        return id != null && currencyCache().coinMap().containsKey(id.toString());
    }

    public static String currencyId(ItemStack stack) {
        ResourceLocation id = KineticRegistries.items().id(stack.getItem());
        return id == null ? "" : id.toString();
    }

    public static boolean isWalletStack(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.CURRENCY_WALLET.get());
    }

    public static Optional<ItemStack> equippedWallet(Player player) {
        if (player == null) return Optional.empty();
//? if >=1.21 {
        /*var slotResult = CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.findFirstCurio(Items.CURRENCY_WALLET.get()));
        *///?} else {
        var slotResult = dev.xyat.adventuresystems.curios.common.util.CuriosAccess.findFirst(player, Items.CURRENCY_WALLET.get());
        //?}
        if (slotResult.isEmpty()) return Optional.empty();
        ItemStack stack = slotResult.get().stack();
        return isWalletStack(stack) ? Optional.of(stack) : Optional.empty();
    }

    public static boolean hasWallet(Player player) {
        return equippedWallet(player).isPresent();
    }

    public static void ensureWalletIdentity(ItemStack walletStack) {
        if (!isWalletStack(walletStack)) return;
//? if >=1.21 {
        /*dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(walletStack, tag -> {
            if (!dev.xyat.adventuresystems.data.Nbt.hasUuid(tag, WALLET_ID_KEY)) dev.xyat.adventuresystems.data.Nbt.putUuid(tag, WALLET_ID_KEY, UUID.randomUUID());
        });
        *///?} else {
        CompoundTag tag = walletStack.getOrCreateTag();
        if (!dev.xyat.adventuresystems.data.Nbt.hasUuid(tag, WALLET_ID_KEY)) {
            dev.xyat.adventuresystems.data.Nbt.putUuid(tag, WALLET_ID_KEY, UUID.randomUUID());
        }
        //?}
    }

    public static CompoundTag snapshot(ItemStack walletStack) {
//? if >=1.21 {
        /*if (!isWalletStack(walletStack)) return new CompoundTag();
        CompoundTag tag = dev.xyat.adventuresystems.data.AdventureItemData.customData(walletStack);
        *///?} else {
        if (!isWalletStack(walletStack) || !walletStack.hasTag()) return new CompoundTag();
        CompoundTag tag = walletStack.getTag();
        //?}
        if (tag == null || !tag.contains(WALLET_BALANCES_KEY, Tag.TAG_COMPOUND)) return new CompoundTag();
        return tag.getCompound(WALLET_BALANCES_KEY).copy();
    }

    public static CompoundTag snapshot(Player player) {
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(Data::snapshot).orElseGet(CompoundTag::new);
    }

    /** The wallet's balance in wallet coins. */
    public static long balance(ItemStack walletStack) {
        return readAmount(snapshot(walletStack), VIRTUAL_ID);
    }

    /** The wallet coins one unit of a currency (the wallet coin or a coin item) is worth, or 0 for anything else. */
    public static long unitValue(String id) {
        if (id == null || id.isEmpty()) return 0L;
        CurrencyType coin = currencyCache().coinMap().get(id);
        return coin == null ? 0L : coin.value();
    }

    /** How many whole units of a currency the wallet's balance covers. */
    public static long amount(ItemStack walletStack, String id) {
        long value = unitValue(id);
        return value <= 0L ? 0L : balance(walletStack) / value;
    }

    /** Adds units of a currency as wallet coins; returns how many units were added. */
    public static long add(ItemStack walletStack, String id, long amount) {
        long value = unitValue(id);
        if (!isWalletStack(walletStack) || value <= 0L || amount <= 0L) return 0L;
        CompoundTag balances = mutableBalances(walletStack);
        long current = readAmount(balances, VIRTUAL_ID);
        long accepted = Math.min(amount, (Long.MAX_VALUE - current) / value);
        if (accepted <= 0L) return 0L;
        balances.putLong(VIRTUAL_ID, current + accepted * value);
        //? if >=1.21 {
        /*dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(walletStack, tag -> tag.put(WALLET_BALANCES_KEY, balances));
        *///?}
        return accepted;
    }

    public static long add(Player player, String id, long amount) {
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(itemStack -> add(itemStack, id, amount)).orElse(0L);
    }

    /** Takes whole units of a currency out of the wallet's balance; returns how many units were taken. */
    public static long remove(ItemStack walletStack, String id, long amount) {
        long value = unitValue(id);
        if (!isWalletStack(walletStack) || value <= 0L || amount <= 0L) return 0L;
        CompoundTag balances = mutableBalances(walletStack);
        long current = readAmount(balances, VIRTUAL_ID);
        long removed = Math.min(current / value, amount);
        if (removed <= 0L) return 0L;
        long next = current - removed * value;
        if (next > 0L) {
            balances.putLong(VIRTUAL_ID, next);
        } else {
            balances.remove(VIRTUAL_ID);
        }
        //? if >=1.21 {
        /*dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(walletStack, tag -> tag.put(WALLET_BALANCES_KEY, balances));
        *///?}
        return removed;
    }

    /**
     * Quick deposit: turns every coin item in the player's inventory into wallet coins. Returns the wallet coins added
     * (0 when nothing was deposited).
     */
    public static long depositInventory(ServerPlayer player) {
        if (!CuriosConfig.enableCurrencyWallet || player == null) return 0L;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return 0L;
        long before = balance(wallet.get());
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!isCurrencyItem(stack)) continue;
            long accepted = add(wallet.get(), currencyId(stack), stack.getCount());
            if (accepted > 0L) stack.shrink((int) accepted);
        }
        return balance(wallet.get()) - before;
    }

    public static boolean depositStack(ServerPlayer player, ItemStack walletStack, ItemStack stack) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || !isWalletStack(walletStack) || !isCurrencyItem(stack)) return false;
        long accepted = add(walletStack, currencyId(stack), stack.getCount());
        if (accepted <= 0L) return false;
        stack.shrink((int) accepted);
        return stack.isEmpty();
    }

    public static long countDirect(ServerPlayer player, String id) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || id == null || id.isEmpty()) return 0L;
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(itemStack -> amount(itemStack, id)).orElse(0L);
    }

    public static long extractDirect(ServerPlayer player, String id, long amount, boolean simulate) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || id == null || id.isEmpty() || amount <= 0L) return 0L;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return 0L;
        long extracted = Math.min(amount(wallet.get(), id), amount);
        if (extracted <= 0L || simulate) return extracted;
        return remove(wallet.get(), id, extracted);
    }

    // Villager trades, the shop and FTB tasks pay from the one balance: a coin item costs its value in wallet coins.
    public static long countForAutomaticPayment(ServerPlayer player, String id) {
        return countDirect(player, id);
    }

    public static long extractForAutomaticPayment(ServerPlayer player, String id, long amount, boolean simulate) {
        return extractDirect(player, id, amount, simulate);
    }

    private static CurrencyCache currencyCache() {
        CurrencyCache cache = currencyCache;
        if (cache != null) return cache;
        synchronized (Data.class) {
            cache = currencyCache;
            if (cache == null) {
                cache = buildCurrencyCache();
                currencyCache = cache;
            }
            return cache;
        }
    }

    private static CurrencyCache buildCurrencyCache() {
        CurrencyType virtual = new CurrencyType(VIRTUAL_ID, KineticResourceIds.parse(VIRTUAL_ID), 1L);
        LinkedHashMap<String, CurrencyType> coins = new LinkedHashMap<>();
        coins.put(VIRTUAL_ID, virtual);
        for (String line : CuriosConfig.walletCurrencyDefinitions) {
            CurrencyType.parse(line).ifPresent(coin -> coins.putIfAbsent(coin.itemId(), coin));
        }
        return new CurrencyCache(List.of(virtual), Map.of(VIRTUAL_ID, virtual), Collections.unmodifiableMap(coins));
    }

    public static long readAmount(CompoundTag balances, String id) {
        if (balances == null || id == null || id.isEmpty()) return 0L;
        Tag tag = balances.get(id);
        if (tag == null) return 0L;
        if (tag instanceof NumericTag numericTag) {
            return clampAmount(dev.xyat.adventuresystems.data.Nbt.asLong(numericTag));
        }
        return parseAmountText(dev.xyat.adventuresystems.data.Nbt.asString(tag));
    }

    private static long parseAmountText(String text) {
        if (text == null) return 0L;
        String value = text.trim().replace(",", "").replace("_", "");
        if (value.endsWith("L") || value.endsWith("l")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) return 0L;
        try {
            BigInteger parsed = new BigInteger(value);
            if (parsed.signum() <= 0) return 0L;
            BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
            if (parsed.compareTo(max) > 0) return Long.MAX_VALUE;
            return parsed.longValue();
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static long clampAmount(long value) {
        return Math.max(0L, value);
    }

    public static boolean isHudVisible(Player player) {
        return !dev.xyat.adventuresystems.data.Nbt.bool(root(player), HUD_HIDDEN_KEY);
    }

    public static boolean toggleHudHidden(ServerPlayer player) {
        CompoundTag root = root(player);
        boolean hidden = !root.getBoolean(HUD_HIDDEN_KEY);
        root.putBoolean(HUD_HIDDEN_KEY, hidden);
        return hidden;
    }


    public static void bindRsController(ItemStack walletStack, Level level, BlockPos pos) {
        if (!isWalletStack(walletStack) || level == null || pos == null) return;
        ensureWalletIdentity(walletStack);
        CompoundTag binding = new CompoundTag();
        binding.putString(RS_BOUND_DIM_KEY, level.dimension().location().toString());
        binding.putInt(RS_BOUND_X_KEY, pos.getX());
        binding.putInt(RS_BOUND_Y_KEY, pos.getY());
        binding.putInt(RS_BOUND_Z_KEY, pos.getZ());
//? if >=1.21 {
        /*dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(walletStack, tag -> tag.put(RS_BINDING_KEY, binding));
        *///?} else {
        walletStack.getOrCreateTag().put(RS_BINDING_KEY, binding);
        //?}
    }

    public static RsBinding rsBinding(ItemStack walletStack) {
//? if >=1.21 {
        /*if (!isWalletStack(walletStack)) return RsBinding.none();
        CompoundTag tag = dev.xyat.adventuresystems.data.AdventureItemData.customData(walletStack);
        *///?} else {
        if (!isWalletStack(walletStack) || !walletStack.hasTag()) return RsBinding.none();
        CompoundTag tag = walletStack.getTag();
        //?}
        if (tag == null || !tag.contains(RS_BINDING_KEY, Tag.TAG_COMPOUND)) return RsBinding.none();
        CompoundTag binding = tag.getCompound(RS_BINDING_KEY);
        String dimension = dev.xyat.adventuresystems.data.Nbt.string(binding, RS_BOUND_DIM_KEY);
        if (dimension.isBlank()) return RsBinding.none();
        return new RsBinding(true, dimension, new BlockPos(dev.xyat.adventuresystems.data.Nbt.intValue(binding, RS_BOUND_X_KEY), dev.xyat.adventuresystems.data.Nbt.intValue(binding, RS_BOUND_Y_KEY), dev.xyat.adventuresystems.data.Nbt.intValue(binding, RS_BOUND_Z_KEY)));
    }

    public static void copy(Player oldPlayer, Player newPlayer) {
        CompoundTag oldRoot = oldPlayer.getPersistentData().getCompound(ROOT_KEY);
        if (dev.xyat.adventuresystems.data.Nbt.hasByte(oldRoot, HUD_HIDDEN_KEY)) {
            root(newPlayer).putBoolean(HUD_HIDDEN_KEY, dev.xyat.adventuresystems.data.Nbt.bool(oldRoot, HUD_HIDDEN_KEY));
        }
    }

    private static CompoundTag mutableBalances(ItemStack walletStack) {
        ensureWalletIdentity(walletStack);
//? if >=1.21 {
        /*return snapshot(walletStack);
        *///?} else {
        CompoundTag tag = walletStack.getOrCreateTag();
        if (!tag.contains(WALLET_BALANCES_KEY, Tag.TAG_COMPOUND)) {
            tag.put(WALLET_BALANCES_KEY, new CompoundTag());
        }
        return tag.getCompound(WALLET_BALANCES_KEY);
        //?}
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_KEY, new CompoundTag());
        }
        return dev.xyat.adventuresystems.data.Nbt.compound(persistent, ROOT_KEY);
    }


    private record CurrencyCache(List<CurrencyType> currencies, Map<String, CurrencyType> currencyMap, Map<String, CurrencyType> coinMap) {
    }

    public record RsBinding(boolean bound, String dimension, BlockPos pos) {
        public static RsBinding none() {
            return new RsBinding(false, "", BlockPos.ZERO);
        }
    }
}

