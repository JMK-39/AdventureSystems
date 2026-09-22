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
    private static final String MAGNET_DISABLED_KEY = "MagnetDisabled";
    private static final String RS_BINDING_KEY = "RsControllerBinding";
    private static final String RS_BOUND_DIM_KEY = "Dimension";
    private static final String RS_BOUND_X_KEY = "X";
    private static final String RS_BOUND_Y_KEY = "Y";
    private static final String RS_BOUND_Z_KEY = "Z";
    private static volatile CurrencyCache currencyCache;

    private Data() {
    }

    public static void invalidateCurrencyCache() {
        currencyCache = null;
    }

    public static List<CurrencyType> currencies() {
        return currencyCache().currencies();
    }

    public static Map<String, CurrencyType> currencyMap() {
        return currencyCache().currencyMap();
    }

    public static boolean isCurrencyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = KineticRegistries.items().id(stack.getItem());
        return id != null && currencyCache().currencyMap().containsKey(id.toString());
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
        var slotResult = CuriosApi.getCuriosHelper().findFirstCurio(player, Items.CURRENCY_WALLET.get());
        if (slotResult.isEmpty()) return Optional.empty();
        ItemStack stack = slotResult.get().stack();
        return isWalletStack(stack) ? Optional.of(stack) : Optional.empty();
    }

    public static boolean hasWallet(Player player) {
        return equippedWallet(player).isPresent();
    }

    public static void ensureWalletIdentity(ItemStack walletStack) {
        if (!isWalletStack(walletStack)) return;
        CompoundTag tag = walletStack.getOrCreateTag();
        if (!tag.hasUUID(WALLET_ID_KEY)) {
            tag.putUUID(WALLET_ID_KEY, UUID.randomUUID());
        }
    }

    public static CompoundTag snapshot(ItemStack walletStack) {
        if (!isWalletStack(walletStack) || !walletStack.hasTag()) return new CompoundTag();
        CompoundTag tag = walletStack.getTag();
        if (tag == null || !tag.contains(WALLET_BALANCES_KEY, Tag.TAG_COMPOUND)) return new CompoundTag();
        return tag.getCompound(WALLET_BALANCES_KEY).copy();
    }

    public static CompoundTag snapshot(Player player) {
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(Data::snapshot).orElseGet(CompoundTag::new);
    }

    public static long amount(ItemStack walletStack, String id) {
        if (id == null || id.isEmpty()) return 0L;
        return readAmount(snapshot(walletStack), id);
    }

    public static long add(ItemStack walletStack, String id, long amount) {
        if (!isWalletStack(walletStack) || id == null || id.isEmpty() || amount <= 0L) return 0L;
        CompoundTag balances = mutableBalances(walletStack);
        long current = readAmount(balances, id);
        long space = Long.MAX_VALUE - current;
        long accepted = Math.min(space, amount);
        if (accepted <= 0L) return 0L;
        balances.putLong(id, current + accepted);
        return accepted;
    }

    public static long add(Player player, String id, long amount) {
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(itemStack -> add(itemStack, id, amount)).orElse(0L);
    }

    public static long remove(ItemStack walletStack, String id, long amount) {
        if (!isWalletStack(walletStack) || id == null || id.isEmpty() || amount <= 0L) return 0L;
        CompoundTag balances = mutableBalances(walletStack);
        long current = readAmount(balances, id);
        long removed = Math.min(current, amount);
        if (removed == 0L) return 0L;
        long next = current - removed;
        if (next > 0L) {
            balances.putLong(id, next);
        } else {
            balances.remove(id);
        }
        return removed;
    }

    public static void depositInventory(ServerPlayer player) {
        if (!CuriosConfig.enableCurrencyWallet || player == null) return;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!isCurrencyItem(stack)) continue;
            String id = currencyId(stack);
            int count = stack.getCount();
            long accepted = add(walletStack, id, count);
            if (accepted > 0L) {
                stack.shrink((int) accepted);
            }
        }
    }

    public static boolean depositStack(ServerPlayer player, ItemStack walletStack, ItemStack stack) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || !isWalletStack(walletStack) || !isCurrencyItem(stack)) return false;
        long accepted = add(walletStack, currencyId(stack), stack.getCount());
        if (accepted <= 0L) return false;
        stack.shrink((int) accepted);
        return stack.isEmpty();
    }

    public static void withdraw(ServerPlayer player, String id) {
        if (!CuriosConfig.enableCurrencyWallet || player == null) return;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType currency = currencies.get(id);
        if (currency == null || !currency.hasItem()) return;
        long stored = amount(walletStack, id);
        if (stored <= 0L) return;
        int count = (int) Math.min(64L, stored);
        Item item = currency.item();
        ItemStack stack = new ItemStack(item, count);
        long removed = remove(walletStack, id, count);
        if (removed <= 0L) return;
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    public static void convertAll(ServerPlayer player, String from, String to) {
        if (!CuriosConfig.enableCurrencyWallet || player == null) return;
        if (from == null || to == null || from.equals(to)) return;
        if (!canExchange(from, to)) return;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType source = currencies.get(from);
        CurrencyType target = currencies.get(to);
        if (source == null || target == null) return;
        long sourceStored = amount(walletStack, from);
        long targetStored = amount(walletStack, to);
        if (sourceStored <= 0L || targetStored == Long.MAX_VALUE) return;
        long sourceValue = source.value();
        long targetValue = target.value();
        if (sourceValue == targetValue) {
            long capacity = Long.MAX_VALUE - targetStored;
            long moving = Math.min(sourceStored, capacity);
            if (moving <= 0L) return;
            remove(walletStack, from, moving);
            add(walletStack, to, moving);
            return;
        }
        if (targetValue > sourceValue) {
            if (targetValue % sourceValue != 0L) return;
            long need = targetValue / sourceValue;
            long created = sourceStored / need;
            long capacity = Long.MAX_VALUE - targetStored;
            created = Math.min(created, capacity);
            if (created <= 0L) return;
            long consumed = created * need;
            remove(walletStack, from, consumed);
            add(walletStack, to, created);
            return;
        }
        if (sourceValue % targetValue != 0L) return;
        long multiplier = sourceValue / targetValue;
        long capacity = Long.MAX_VALUE - targetStored;
        long sourceByCapacity = capacity / multiplier;
        long sourceToUse = Math.min(sourceStored, sourceByCapacity);
        if (sourceToUse <= 0L) return;
        remove(walletStack, from, sourceToUse);
        add(walletStack, to, sourceToUse * multiplier);
    }

    public static void convertOne(ServerPlayer player, String from, String to) {
        if (!CuriosConfig.enableCurrencyWallet || player == null) return;
        if (from == null || to == null || from.equals(to)) return;
        if (!canExchange(from, to)) return;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType source = currencies.get(from);
        CurrencyType target = currencies.get(to);
        if (source == null || target == null) return;
        long sourceStored = amount(walletStack, from);
        long targetStored = amount(walletStack, to);
        if (sourceStored <= 0L || targetStored == Long.MAX_VALUE) return;
        long sourceValue = source.value();
        long targetValue = target.value();
        if (sourceValue == targetValue) {
            remove(walletStack, from, 1L);
            add(walletStack, to, 1L);
            return;
        }
        if (targetValue > sourceValue) {
            if (targetValue % sourceValue != 0L) return;
            long need = targetValue / sourceValue;
            if (sourceStored < need) return;
            remove(walletStack, from, need);
            add(walletStack, to, 1L);
            return;
        }
        if (sourceValue % targetValue != 0L) return;
        long output = sourceValue / targetValue;
        if (Long.MAX_VALUE - targetStored < output) return;
        remove(walletStack, from, 1L);
        add(walletStack, to, output);
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
        long extracted = Math.min(Data.amount(wallet.get(), id), amount);
        if (extracted <= 0L || simulate) return extracted;
        return remove(wallet.get(), id, extracted);
    }

    public static long countDirectForAutomaticPayment(ServerPlayer player, String id) {
        return countDirect(player, id);
    }

    public static long extractDirectForAutomaticPayment(ServerPlayer player, String id, long amount, boolean simulate) {
        return extractDirect(player, id, amount, simulate);
    }

    public static boolean hasManualExchangeSourceFor(ServerPlayer player, String targetId) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || targetId == null || targetId.isEmpty()) return false;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return false;
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType target = currencies.get(targetId);
        if (target == null) return false;
        for (CurrencyType source : currencies.values()) {
            if (source.itemId().equals(target.itemId())) continue;
            if (source.value() <= target.value()) continue;
            if (!canExchange(source.itemId(), target.itemId())) continue;
            if (amount(wallet.get(), source.itemId()) > 0L) return true;
        }
        return false;
    }


    public static long countForAutomaticPayment(ServerPlayer player, String id) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || id == null || id.isEmpty()) return 0L;
        Optional<ItemStack> wallet = equippedWallet(player);
        return wallet.map(itemStack -> countForAutomaticPayment(itemStack, id)).orElse(0L);
    }

    private static long countForAutomaticPayment(ItemStack walletStack, String id) {
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType target = currencies.get(id);
        if (target == null) return 0L;
        long total = amount(walletStack, id);
        for (CurrencyType source : currencies.values()) {
            if (canAutoConvertForAutomaticPayment(source, target)) {
                long stored = amount(walletStack, source.itemId());
                if (stored <= 0L) continue;
                long multiplier = source.value() / target.value();
                total = safeAdd(total, safeMultiply(stored, multiplier));
                if (total == Long.MAX_VALUE) return Long.MAX_VALUE;
            }
        }
        return total;
    }

    public static long extractForAutomaticPayment(ServerPlayer player, String id, long amount, boolean simulate) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || id == null || id.isEmpty() || amount <= 0L) return 0L;
        Optional<ItemStack> wallet = equippedWallet(player);
        if (wallet.isEmpty()) return 0L;
        ItemStack walletStack = wallet.get();
        long available = countForAutomaticPayment(walletStack, id);
        long targetAmount = Math.min(amount, available);
        if (targetAmount <= 0L || simulate) return targetAmount;
        long remaining = targetAmount;
        long direct = remove(walletStack, id, remaining);
        remaining -= direct;
        if (remaining <= 0L) return targetAmount;
        Map<String, CurrencyType> currencies = currencyMap();
        CurrencyType target = currencies.get(id);
        if (target == null) return targetAmount - remaining;
        for (CurrencyType source : currencies.values()) {
            if (remaining <= 0L) break;
            if (canAutoConvertForAutomaticPayment(source, target)) {
                long stored = amount(walletStack, source.itemId());
                if (stored <= 0L) continue;
                long multiplier = source.value() / target.value();
                long sourceNeeded = ceilDiv(remaining, multiplier);
                long sourceToUse = Math.min(stored, sourceNeeded);
                if (sourceToUse <= 0L) continue;
                long output = safeMultiply(sourceToUse, multiplier);
                if (output <= 0L) continue;
                remove(walletStack, source.itemId(), sourceToUse);
                add(walletStack, id, output);
                long convertedDirect = remove(walletStack, id, remaining);
                remaining -= convertedDirect;
            }
        }
        return targetAmount - remaining;
    }

    public static boolean canExchange(String from, String to) {
        if (from == null || to == null || from.equals(to)) return false;
        for (String[] rule : exchangeRules()) {
            if (from.equals(rule[0]) && to.equals(rule[1])) return true;
        }
        return false;
    }

    public static boolean isManualExchangeOnlyCurrency(String id) {
        if (id == null || id.isBlank()) return false;
        String normalized = normalizeCurrencyId(id);
        if (normalized.isEmpty()) return false;
        for (String protectedId : manualExchangeOnlyCurrencies()) {
            if (normalized.equals(protectedId)) return true;
        }
        return false;
    }

    public static List<String> manualExchangeOnlyCurrencies() {
        return currencyCache().manualExchangeOnlyCurrencies();
    }

    public static List<String[]> exchangeRules() {
        return currencyCache().exchangeRules();
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
        LinkedHashMap<String, CurrencyType> currencies = new LinkedHashMap<>();
        int limit = Math.max(1, CuriosConfig.walletMaxCurrencyTypes);
        for (String line : CuriosConfig.walletCurrencyDefinitions) {
            if (currencies.size() >= limit) break;
            Optional<CurrencyType> parsed = CurrencyType.parse(line);
            if (parsed.isEmpty()) continue;
            CurrencyType currency = parsed.get();
            currencies.putIfAbsent(currency.itemId(), currency);
        }

        LinkedHashMap<String, String[]> rules = new LinkedHashMap<>();
        for (String rule : CuriosConfig.walletExchangeRules) {
            if (rule == null) continue;
            String[] parts = rule.trim().split("->");
            if (parts.length != 2) continue;
            addExchangeRule(rules, currencies, parts[0].trim(), parts[1].trim());
        }

        LinkedHashMap<String, String> manualOnly = new LinkedHashMap<>();
        for (String value : CuriosConfig.walletManualExchangeOnlyCurrencies) {
            addManualExchangeOnlyCurrency(manualOnly, currencies, value);
        }

        return new CurrencyCache(
                List.copyOf(currencies.values()),
                Collections.unmodifiableMap(new LinkedHashMap<>(currencies)),
                List.copyOf(rules.values()),
                List.copyOf(manualOnly.values())
        );
    }

    private static void addExchangeRule(LinkedHashMap<String, String[]> rules, Map<String, CurrencyType> currencies, String from, String to) {
        if (from == null || to == null || from.equals(to)) return;
        if (!currencies.containsKey(from) || !currencies.containsKey(to)) return;
        rules.putIfAbsent(from + "->" + to, new String[]{from, to});
    }

    private static void addManualExchangeOnlyCurrency(LinkedHashMap<String, String> result, Map<String, CurrencyType> currencies, String text) {
        String id = normalizeCurrencyId(text);
        if (id.isEmpty()) return;
        if (!currencies.containsKey(id)) return;
        result.putIfAbsent(id, id);
    }

    private static String normalizeCurrencyId(String text) {
        if (text == null) return "";
        String id = text.trim();
        if (id.isEmpty()) return "";
        int pipe = id.indexOf('|');
        if (pipe >= 0) id = id.substring(0, pipe).trim();
        int arrow = id.indexOf("->");
        if (arrow >= 0) id = id.substring(0, arrow).trim();
        try {
            return KineticResourceIds.parse(id).toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    public static long readAmount(CompoundTag balances, String id) {
        if (balances == null || id == null || id.isEmpty()) return 0L;
        Tag tag = balances.get(id);
        if (tag == null) return 0L;
        if (tag instanceof NumericTag numericTag) {
            return clampAmount(numericTag.getAsLong());
        }
        return parseAmountText(tag.getAsString());
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
        return !root(player).getBoolean(HUD_HIDDEN_KEY);
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
        walletStack.getOrCreateTag().put(RS_BINDING_KEY, binding);
    }

    public static RsBinding rsBinding(ItemStack walletStack) {
        if (!isWalletStack(walletStack) || !walletStack.hasTag()) return RsBinding.none();
        CompoundTag tag = walletStack.getTag();
        if (tag == null || !tag.contains(RS_BINDING_KEY, Tag.TAG_COMPOUND)) return RsBinding.none();
        CompoundTag binding = tag.getCompound(RS_BINDING_KEY);
        String dimension = binding.getString(RS_BOUND_DIM_KEY);
        if (dimension.isBlank()) return RsBinding.none();
        return new RsBinding(true, dimension, new BlockPos(binding.getInt(RS_BOUND_X_KEY), binding.getInt(RS_BOUND_Y_KEY), binding.getInt(RS_BOUND_Z_KEY)));
    }

    public static boolean isMagnetDisabled(ItemStack walletStack) {
        if (!isWalletStack(walletStack) || !walletStack.hasTag()) return false;
        CompoundTag tag = walletStack.getTag();
        return tag != null && tag.getBoolean(MAGNET_DISABLED_KEY);
    }

    public static boolean toggleMagnetDisabled(ItemStack walletStack) {
        if (!isWalletStack(walletStack)) return false;
        ensureWalletIdentity(walletStack);
        CompoundTag tag = walletStack.getOrCreateTag();
        boolean disabled = !tag.getBoolean(MAGNET_DISABLED_KEY);
        tag.putBoolean(MAGNET_DISABLED_KEY, disabled);
        return disabled;
    }

    public static void copy(Player oldPlayer, Player newPlayer) {
        CompoundTag oldRoot = oldPlayer.getPersistentData().getCompound(ROOT_KEY);
        if (oldRoot.contains(HUD_HIDDEN_KEY, Tag.TAG_BYTE)) {
            root(newPlayer).putBoolean(HUD_HIDDEN_KEY, oldRoot.getBoolean(HUD_HIDDEN_KEY));
        }
    }

    private static CompoundTag mutableBalances(ItemStack walletStack) {
        ensureWalletIdentity(walletStack);
        CompoundTag tag = walletStack.getOrCreateTag();
        if (!tag.contains(WALLET_BALANCES_KEY, Tag.TAG_COMPOUND)) {
            tag.put(WALLET_BALANCES_KEY, new CompoundTag());
        }
        return tag.getCompound(WALLET_BALANCES_KEY);
    }

    private static boolean canAutoConvertForAutomaticPayment(CurrencyType source, CurrencyType target) {
        if (source == null || target == null) return false;
        if (source.itemId().equals(target.itemId())) return false;
        if (isManualExchangeOnlyCurrency(source.itemId())) return false;
        if (isManualExchangeOnlyCurrency(target.itemId())) return false;
        if (source.value() <= target.value()) return false;
        if (target.value() <= 0L || source.value() % target.value() != 0L) return false;
        return canExchange(source.itemId(), target.itemId());
    }

    private static long safeAdd(long a, long b) {
        if (a <= 0L) return Math.max(0L, b);
        if (b <= 0L) return a;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        if (a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    private static long ceilDiv(long value, long divisor) {
        if (value <= 0L || divisor <= 0L) return 0L;
        long result = value / divisor;
        if (value % divisor != 0L) result++;
        return result;
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_KEY, new CompoundTag());
        }
        return persistent.getCompound(ROOT_KEY);
    }


    private record CurrencyCache(List<CurrencyType> currencies, Map<String, CurrencyType> currencyMap, List<String[]> exchangeRules, List<String> manualExchangeOnlyCurrencies) {
    }

    public record RsBinding(boolean bound, String dimension, BlockPos pos) {
        public static RsBinding none() {
            return new RsBinding(false, "", BlockPos.ZERO);
        }
    }
}

