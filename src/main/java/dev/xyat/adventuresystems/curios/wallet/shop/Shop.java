package dev.xyat.adventuresystems.curios.wallet.shop;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.ftb.api.FTBQuestApi;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.storage.MaterialStorage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

public final class Shop {
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("kineticcore");
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("currency_wallet_shop.toml");
    private static final String EDIT_KEY = "adventuresystems_currency_wallet_shop_editor";
    private static final String USAGE_KEY = "adventuresystems_currency_wallet_shop_usage";
    private static final String TIMED_KEY = "Timed";
    private static final String TOTAL_KEY = "Total";
    private static final String SELL_PROGRESS_KEY = "adventuresystems_currency_wallet_shop_sell_progress";
    private static final String USE_BACKPACK_KEY = "adventuresystems_currency_wallet_shop_use_backpack";
    private static final String USE_RS_KEY = "adventuresystems_currency_wallet_shop_use_rs";
    private static final int TICKS_PER_SECOND = 20;
    private static final int SELECTED_REWARD_AMOUNT_FACTOR = 1000;
    private static CommentedFileConfig configData;
    private static List<String> buyLines = new ArrayList<>();
    private static List<String> sellLines = new ArrayList<>();

    static {
        load();
    }

    private Shop() {
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            Config old = Config.inMemory();
            if (Files.exists(CONFIG_PATH)) {
                try (FileConfig oldFile = FileConfig.of(CONFIG_PATH)) {
                    oldFile.load();
                    old.putAll(oldFile);
                }
            }
            configData = CommentedFileConfig.builder(CONFIG_PATH).sync().preserveInsertionOrder().writingMode(WritingMode.REPLACE).build();
            configData.set("shop.buyEntries", old.getOrElse("shop.buyEntries", List.of()));
            configData.setComment("shop.buyEntries", "购买道具列表。格式: 展示物品ID*数量|支付物品ID或货币ID|价格。支付物品是配置货币时扣钱包余额，普通物品时扣玩家物品。扩展: page=分页名|time=冷却秒|limit=总限购|quests=任务ID|questNeed=需要完成数量|name=显示名|command=服务器权限执行指令。抽卡: gacha=true|rewards=物品ID*数量@10,empty@60。自选: choice=true|rewards=物品ID*数量@1,物品ID*数量@1。");
            configData.set("shop.sellEntries", old.getOrElse("shop.sellEntries", List.of()));
            configData.setComment("shop.sellEntries", "出售道具列表。格式: 物品ID*数量|货币ID|收入|page=分页名。带NBT物品会自动保存为 物品ID*数量#stack64:完整物品数据。多物品出售格式: 物品ID*数量|货币ID|收入|page=材料|choice=true|rewards=物品ID*数量@1,物品ID*数量@1。");
            configData.save();
            readValues();
        } catch (Exception e) {
            CuriosModule.LOGGER.error("Failed to load currency wallet shop config", e);
        }
    }

    public static void save() {
        if (configData == null) return;
        configData.set("shop.buyEntries", buyLines);
        configData.set("shop.sellEntries", sellLines);
        configData.save();
        readValues();
    }

    private static void readValues() {
        if (configData == null) return;
        buyLines = new ArrayList<>(configData.getOrElse("shop.buyEntries", List.of()));
        sellLines = new ArrayList<>(configData.getOrElse("shop.sellEntries", List.of()));
    }

    public static boolean isEditMode(ServerPlayer player) {
        return canEdit(player) && player.getPersistentData().getBoolean(EDIT_KEY);
    }

    public static boolean canEdit(ServerPlayer player) {
        return player != null && player.hasPermissions(2);
    }

    public static void setEditMode(ServerPlayer player, boolean value) {
        if (!canEdit(player)) return;
        player.getPersistentData().putBoolean(EDIT_KEY, value);
    }

    public static boolean toggleEditMode(ServerPlayer player) {
        if (!canEdit(player)) return false;
        boolean enabled = !isEditMode(player);
        setEditMode(player, enabled);
        return enabled;
    }

    public static boolean usesBackpack(ServerPlayer player) {
        return player != null && player.getPersistentData().getBoolean(USE_BACKPACK_KEY);
    }

    public static boolean usesRs(ServerPlayer player) {
        return player != null && player.getPersistentData().getBoolean(USE_RS_KEY);
    }

    public static boolean toggleBackpack(ServerPlayer player) {
        if (player == null || !Data.hasWallet(player)) return false;
        boolean enabled = !usesBackpack(player);
        player.getPersistentData().putBoolean(USE_BACKPACK_KEY, enabled);
        return enabled;
    }

    public static boolean toggleRs(ServerPlayer player) {
        if (player == null || !Data.hasWallet(player)) return false;
        boolean enabled = !usesRs(player);
        player.getPersistentData().putBoolean(USE_RS_KEY, enabled);
        return enabled;
    }

    public static List<Entry> entries(Mode mode, ServerPlayer player) {
        List<String> source = mode == Mode.BUY ? buyLines : sellLines;
        List<Entry> result = new ArrayList<>();
        EntryBuildContext context = new EntryBuildContext(player);
        for (int i = 0; i < source.size(); i++) {
            Entry entry = parseEntry(mode, i, source.get(i), player, context);
            if (entry != null) result.add(entry);
        }
        return result;
    }

    public static CompoundTag clientTag(ServerPlayer player) {
        CompoundTag root = new CompoundTag();
        root.put("Buy", entriesTag(Mode.BUY, player));
        root.put("Sell", entriesTag(Mode.SELL, player));
        root.putBoolean("UseBackpack", usesBackpack(player));
        root.putBoolean("UseRs", usesRs(player));
        root.putBoolean("CanEdit", canEdit(player));
        return root;
    }

    public static List<Entry> entriesFromTag(CompoundTag root, Mode mode) {
        List<Entry> result = new ArrayList<>();
        if (root == null) return result;
        String key = mode == Mode.BUY ? "Buy" : "Sell";
        if (!root.contains(key, Tag.TAG_LIST)) return result;
        ListTag list = root.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            Entry entry = entryFromTag(mode, tag);
            if (entry != null) result.add(entry);
        }
        return result;
    }

    public static boolean saveEntry(ServerPlayer player, Mode mode, int index, String itemId, String currencyId, long price, int count, int dailyLimit, int totalLimit, String questText, boolean gacha, String rewardsText) {
        if (!isEditMode(player) || mode == null) return false;
        List<String> source = mode == Mode.BUY ? buyLines : sellLines;
        String uid = index >= 0 && index < source.size() ? entryUid(source.get(index)) : "";
        if (uid.isBlank()) uid = UUID.randomUUID().toString();
        ItemStack stack = stackFromId(itemId, count);
        String line = createLine(mode, stack, currencyId, price, count, dailyLimit, totalLimit, questText, gacha, rewardsText, uid);
        if (line == null) return false;
        if (index >= 0 && index < source.size()) source.set(index, line);
        else source.add(line);
        save();
        return true;
    }

    public static boolean remove(ServerPlayer player, Mode mode, int index) {
        if (!isEditMode(player)) return false;
        List<String> source = mode == Mode.BUY ? buyLines : sellLines;
        if (index < 0 || index >= source.size()) return false;
        source.remove(index);
        save();
        return true;
    }

    public static boolean move(ServerPlayer player, Mode mode, int fromIndex, int toIndex) {
        if (!isEditMode(player) || mode == null) return false;
        List<String> source = mode == Mode.BUY ? buyLines : sellLines;
        if (fromIndex < 0 || fromIndex >= source.size() || source.size() < 2) return false;
        source.replaceAll(Shop::withEntryUid);
        int safeTarget = Math.max(0, Math.min(toIndex, source.size() - 1));
        if (fromIndex == safeTarget) return true;
        String moving = source.remove(fromIndex);
        source.add(Math.min(safeTarget, source.size()), moving);
        save();
        return true;
    }

    public static BuyResult buyDetailed(ServerPlayer player, int index, int amount) {
        if (player == null || amount <= 0 || !Data.hasWallet(player)) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_invalid");
        int requested = unpackRequestedAmount(amount);
        int selectedRewardIndex = unpackSelectedRewardIndex(amount);
        Entry entry = entry(Mode.BUY, index, player);
        if (entry == null) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_invalid");
        if (entry.locked()) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_locked");
        long remainingSeconds = timedRemainingSeconds(player, entry.key(), entry.dailyLimit());
        if (remainingSeconds > 0L) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_timed", formatDuration(remainingSeconds));
        int totalRemaining = totalRemaining(entry);
        if (totalRemaining <= 0) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_limit");
        int limitedAmount = Math.min(requested, totalRemaining);
        ItemStack paymentStack = paymentStack(entry.currencyId());
        if (paymentStack.isEmpty()) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_invalid");
        long available = safeAdd(entry.totalMaterials(), entry.walletCount());
        int affordableAmount = entry.price() <= 0L ? 0 : (int) Math.min(limitedAmount, available / entry.price());
        if (affordableAmount <= 0) return BuyResult.summary(entry, requested, 0, 0L, false);
        long totalPrice = safeMultiply(entry.price(), affordableAmount);
        if (totalPrice <= 0L) return BuyResult.fail("msg.adventuresystems.curios.wallet.shop_buy_fail_invalid");
        PaymentConsumeResult consumed = consumePayment(player, entry.currencyId(), paymentStack, totalPrice);
        int completed = consumed.total() >= entry.price() ? (int) Math.min(affordableAmount, consumed.total() / entry.price()) : 0;
        long actualPrice = safeMultiply(entry.price(), completed);
        if (completed == 0) return BuyResult.summary(entry, requested, 0, 0L, false);
        completeBuyReward(player, entry, completed, selectedRewardIndex);
        addUsage(player, entry, completed);
        return BuyResult.summary(entry, requested, completed, actualPrice, completed >= requested);
    }


    private static PaymentConsumeResult consumePayment(ServerPlayer player, String paymentId, ItemStack paymentStack, long amount) {
        if (player == null || paymentStack == null || paymentStack.isEmpty() || amount <= 0L) return PaymentConsumeResult.empty();
        boolean currencyPayment = isCurrencyPayment(paymentId);
        long remaining = amount;
        long fromBackpack = 0L;
        if (usesBackpack(player)) {
            fromBackpack = MaterialStorage.consumeBackpack(player, paymentStack, remaining);
            remaining -= fromBackpack;
        }
        long fromRs = 0L;
        if (remaining > 0L && usesRs(player)) {
            fromRs = MaterialStorage.consumeRs(player, paymentStack, remaining);
            remaining -= fromRs;
        }
        long fromInventory = MaterialStorage.consumeInventory(player, paymentStack, remaining);
        remaining -= fromInventory;
        long fromWallet = 0L;
        if (remaining > 0L && currencyPayment) {
            long directWallet = Data.extractDirectForAutomaticPayment(player, paymentId, remaining, false);
            fromWallet += directWallet;
            remaining -= directWallet;
        }
        if (remaining > 0L && currencyPayment) {
            long convertedWallet = Data.extractForAutomaticPayment(player, paymentId, remaining, false);
            fromWallet += convertedWallet;
        }
        return new PaymentConsumeResult(fromInventory, fromWallet, fromBackpack, fromRs);
    }

    private record PaymentConsumeResult(long fromInventory, long fromWallet, long fromBackpack, long fromRs) {
        static PaymentConsumeResult empty() {
            return new PaymentConsumeResult(0L, 0L, 0L, 0L);
        }

        long total() {
            return safeAdd(safeAdd(fromInventory, fromWallet), safeAdd(fromBackpack, fromRs));
        }
    }

    private static void completeBuyReward(ServerPlayer player, Entry entry, int completed, int selectedRewardIndex) {
        if (entry == null || completed <= 0) return;
        if (!entry.selectable() && !entry.gacha() && entry.command() != null && !entry.command().isBlank()) {
            for (int i = 0; i < completed; i++) executeShopCommand(player, entry.command(), completed);
            return;
        }
        if (entry.selectable()) {
            Reward reward = selectedReward(entry.rewards(), selectedRewardIndex);
            if (reward == null) return;
            completeReward(player, reward, completed);
        } else if (entry.gacha()) {
            for (int i = 0; i < completed; i++) {
                Reward reward = rollReward(player, entry.rewards());
                if (reward != null) completeReward(player, reward, 1);
            }
        } else {
            giveRepeated(player, entry.stack(), completed);
        }
    }

    private static void completeReward(ServerPlayer player, Reward reward, int times) {
        if (player == null || reward == null || times <= 0) return;
        if (reward.command() != null && !reward.command().isBlank()) {
            for (int i = 0; i < times; i++) executeShopCommand(player, reward.command(), times);
            return;
        }
        if (!reward.empty()) {
            for (int i = 0; i < times; i++) giveStack(player, reward.stack().copy());
        }
    }

    public static SellResult sellDetailed(ServerPlayer player, int index, int amount) {
        if (player == null || amount <= 0 || !Data.hasWallet(player)) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_invalid");
        int requested = unpackRequestedAmount(amount);
        int selectedMaterialIndex = unpackSelectedRewardIndex(amount);
        Entry entry = entry(Mode.SELL, index, player);
        if (entry == null) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_invalid");
        SellMaterial material = selectedSellMaterial(entry, selectedMaterialIndex);
        ItemStack materialStack = material.stack();
        int perTrade = materialStack.getCount();
        if (perTrade <= 0) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_invalid");
        Optional<ItemStack> wallet = Data.equippedWallet(player);
        if (wallet.isEmpty()) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_invalid");
        long current = Data.amount(wallet.get(), entry.currencyId());
        long maxTradesByWallet = entry.price() <= 0L ? 0L : (Long.MAX_VALUE - current) / entry.price();
        if (maxTradesByWallet <= 0L) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_wallet_full");
        int safeAmount = (int) Math.min(requested, maxTradesByWallet);
        long oldProgress = sellProgress(player, material.progressKey());
        long requestedItems = safeMultiply(perTrade, safeAmount);
        long stillNeededForRequest = requestedItems - Math.min(oldProgress, requestedItems);
        boolean useBackpack = usesBackpack(player);
        boolean useRs = usesRs(player);
        MaterialStorage.Snapshot sourceSnapshot = MaterialStorage.snapshot(player, materialStack, useBackpack, useRs);
        long availableMaterials = sourceSnapshot.total();
        long toConsume = Math.min(availableMaterials, stillNeededForRequest);
        String materialName = itemNameArg(materialStack);
        String currencyName = itemNameArg(entry.currencyId());
        if (toConsume <= 0L) {
            long missing = Math.max(1L, stillNeededForRequest);
            return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_missing_summary", formatExact(requested), formatExact(requested), formatExact(0L), materialName, formatExact(0L), currencyName, formatExact(missing), materialName, formatExact(sourceSnapshot.inventoryCount()), formatMaterialSource(sourceSnapshot.backpackCount(), sourceSnapshot.backpackLoaded(), sourceSnapshot.hasBackpack(), "backpack"), formatMaterialSource(sourceSnapshot.rsCount(), sourceSnapshot.rsLoaded(), sourceSnapshot.rsState() == MaterialStorage.RsState.BOUND, "rs"));
        }
        MaterialStorage.ConsumeResult consumed = MaterialStorage.consume(player, materialStack, toConsume, useBackpack, useRs);
        if (consumed.total() <= 0L) return SellResult.fail("msg.adventuresystems.curios.wallet.shop_sell_fail_invalid");
        long totalProgress = oldProgress + consumed.total();
        long completed = Math.min(safeAmount, totalProgress / perTrade);
        long leftProgress = totalProgress - completed * perTrade;
        long pay = safeMultiply(entry.price(), completed);
        long paid = 0L;
        if (completed > 0L && pay > 0L) {
            paid = Data.add(wallet.get(), entry.currencyId(), pay);
        }
        setSellProgress(player, material.progressKey(), leftProgress);
        long missingValue = requestedItems - oldProgress - consumed.total();
        long missing = Math.max(missingValue, 0L);
        MaterialStorage.Snapshot after = consumed.after();
        return new SellResult(true, completed > 0L, requested, completed, consumed.total(), consumed.fromInventory(), consumed.fromBackpack(), consumed.fromRs(), missing, leftProgress, perTrade, after.inventoryCount(), after.backpackCount(), after.rsCount(), paid, materialName, currencyName, "");
    }

    private static ListTag entriesTag(Mode mode, ServerPlayer player) {
        ListTag list = new ListTag();
        for (Entry entry : entries(mode, player)) {
            list.add(entryToTag(entry));
        }
        return list;
    }

    private static CompoundTag entryToTag(Entry entry) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Index", entry.index());
        tag.putString("Key", entry.key());
        tag.put("Item", entry.stack().save(new CompoundTag()));
        tag.putString("Currency", entry.currencyId());
        tag.putLong("Price", entry.price());
        tag.putInt("DailyLimit", entry.dailyLimit());
        tag.putInt("TimedLimitSeconds", entry.dailyLimit());
        tag.putLong("TimedRemainingSeconds", entry.timedRemainingSeconds());
        tag.putInt("TotalLimit", entry.totalLimit());
        tag.putInt("DailyBought", entry.dailyBought());
        tag.putInt("TotalBought", entry.totalBought());
        tag.putLong("InventoryCount", entry.inventoryCount());
        tag.putLong("WalletCount", entry.walletCount());
        tag.putLong("BackpackCount", entry.backpackCount());
        tag.putLong("RsCount", entry.rsCount());
        tag.putBoolean("BackpackLoaded", entry.backpackLoaded());
        tag.putBoolean("HasBackpack", entry.hasBackpack());
        tag.putBoolean("RsLoaded", entry.rsLoaded());
        tag.putString("RsState", entry.rsState());
        tag.putLong("SellProgress", entry.sellProgress());
        tag.putString("PageName", entry.pageName());
        tag.putString("DisplayName", entry.displayName());
        tag.putString("Description", entry.description());
        tag.putString("Command", entry.command());
        tag.putInt("RequiredQuestCount", entry.requiredQuestCount());
        tag.putInt("RequiredQuestCompleted", entry.requiredQuestCompleted());
        tag.put("RequiredQuestCompletedIds", questIdsTag(entry.requiredQuestCompletedIds()));
        tag.putLong("RequiredQuestId", entry.requiredQuestId());
        tag.putString("RequiredQuestTitle", entry.requiredQuestTitle());
        tag.put("RequiredQuestIds", questIdsTag(entry.requiredQuestIds()));
        tag.put("RequiredQuestTitles", questTitlesTag(entry.requiredQuestTitles()));
        tag.putBoolean("Unlocked", entry.unlocked());
        tag.putBoolean("Gacha", entry.gacha());
        tag.putBoolean("Selectable", entry.selectable());
        tag.put("Rewards", rewardsTag(entry.rewards()));
        return tag;
    }

    private static Entry entryFromTag(Mode mode, CompoundTag tag) {
        ItemStack stack = ItemStack.of(tag.getCompound("Item"));
        String currency = tag.getString("Currency");
        long price = tag.getLong("Price");
        int index = tag.getInt("Index");
        if (stack.isEmpty() || currency.isEmpty() || price <= 0L) return null;
        String key = tag.getString("Key");
        int dailyLimit = tag.contains("TimedLimitSeconds", Tag.TAG_INT) ? tag.getInt("TimedLimitSeconds") : tag.getInt("DailyLimit");
        int totalLimit = tag.getInt("TotalLimit");
        int dailyBought = tag.getInt("DailyBought");
        int totalBought = tag.getInt("TotalBought");
        long timedRemainingSeconds = tag.getLong("TimedRemainingSeconds");
        long inventoryCount = tag.getLong("InventoryCount");
        long walletCount = tag.getLong("WalletCount");
        long backpackCount = tag.getLong("BackpackCount");
        long rsCount = tag.getLong("RsCount");
        boolean backpackLoaded = tag.getBoolean("BackpackLoaded");
        boolean hasBackpack = tag.getBoolean("HasBackpack");
        boolean rsLoaded = tag.getBoolean("RsLoaded");
        String rsState = tag.getString("RsState");
        long sellProgress = tag.getLong("SellProgress");
        String pageName = tag.getString("PageName");
        String displayName = tag.getString("DisplayName");
        String description = tag.getString("Description");
        String command = tag.getString("Command");
        int requiredQuestCount = tag.getInt("RequiredQuestCount");
        int requiredQuestCompleted = tag.getInt("RequiredQuestCompleted");
        List<Long> requiredQuestCompletedIds = questIdsFromTag(tag.getList("RequiredQuestCompletedIds", Tag.TAG_LONG));
        long requiredQuestId = tag.getLong("RequiredQuestId");
        String requiredQuestTitle = tag.getString("RequiredQuestTitle");
        List<Long> requiredQuestIds = questIdsFromTag(tag.getList("RequiredQuestIds", Tag.TAG_LONG));
        if (requiredQuestIds.isEmpty() && requiredQuestId != 0L) requiredQuestIds.add(requiredQuestId);
        List<String> requiredQuestTitles = questTitlesFromTag(tag.getList("RequiredQuestTitles", Tag.TAG_STRING));
        if (requiredQuestTitles.isEmpty() && !requiredQuestTitle.isBlank()) requiredQuestTitles.add(requiredQuestTitle);
        boolean unlocked = !tag.contains("Unlocked") || tag.getBoolean("Unlocked");
        boolean gacha = tag.getBoolean("Gacha");
        boolean selectable = tag.getBoolean("Selectable");
        List<Reward> rewards = rewardsFromTag(tag.getList("Rewards", Tag.TAG_COMPOUND));
        return new Entry(mode, index, key, stack, currency, price, dailyLimit, totalLimit, pageName, displayName, description, command, requiredQuestCount, requiredQuestCompleted, requiredQuestCompletedIds, requiredQuestId, requiredQuestTitle, requiredQuestIds, requiredQuestTitles, unlocked, gacha, selectable, rewards, dailyBought, totalBought, timedRemainingSeconds, inventoryCount, walletCount, backpackCount, rsCount, backpackLoaded, hasBackpack, rsLoaded, rsState, sellProgress);
    }

    private static Entry entry(Mode mode, int index, ServerPlayer player) {
        List<String> source = mode == Mode.BUY ? buyLines : sellLines;
        if (index < 0 || index >= source.size()) return null;
        return parseEntry(mode, index, source.get(index), player, new EntryBuildContext(player));
    }


    private static String createLine(Mode mode, ItemStack stack, String currencyId, long price, int count, int dailyLimit, int totalLimit, String questText, boolean gacha, String rewardsText, String uid) {
        if (stack == null || stack.isEmpty() || currencyId == null || currencyId.isBlank() || price <= 0L) return null;
        String paymentId = normalizePaymentId(currencyId);
        if (paymentId.isEmpty()) return null;
        Item item = stack.getItem();
        if (item == Items.AIR) return null;
        int safeCount = Math.max(1, Math.min(count, stack.getMaxStackSize()));
        StringBuilder builder = new StringBuilder();
        String itemText = StackCodec.toConfigString(stack, safeCount);
        if (itemText.isEmpty()) return null;
        builder.append(itemText).append("|").append(paymentId).append("|").append(price);
        int safeTimed = sanitizeTimedSeconds(dailyLimit);
        int safeTotal = Math.max(0, totalLimit);
        if (safeTimed > 0) builder.append("|time=").append(safeTimed);
        if (safeTotal > 0) builder.append("|limit=").append(safeTotal);
        String normalizedQuests = normalizeQuestText(questText);
        int requiredQuestCount = normalizeRequiredQuestCount(questText, normalizedQuests);
        String pageName = normalizePageName(questText);
        if (!pageName.isEmpty()) builder.append("|page=").append(pageName);
        String displayName = normalizeDisplayName(questText);
        if (!displayName.isEmpty()) builder.append("|name=").append(displayName);
        String description = normalizeDescription(questText);
        if (!description.isEmpty()) builder.append("|description=").append(description);
        String command = normalizeCommand(questText);
        if (!command.isEmpty()) builder.append("|command=").append(command);
        if (!normalizedQuests.isEmpty()) builder.append("|quests=").append(normalizedQuests);
        if (requiredQuestCount > 0 && !normalizedQuests.isEmpty()) builder.append("|questNeed=").append(requiredQuestCount);
        String normalizedRewards = normalizeRewardsText(rewardsText);
        boolean selectable = normalizeSelectableReward(questText);
        boolean useRewardPool = gacha || selectable || !normalizedRewards.isEmpty();
        if (useRewardPool) {
            if (normalizedRewards.isEmpty() || parseRewards(normalizedRewards).isEmpty()) return null;
            if (mode == Mode.SELL) {
                builder.append("|choice=true");
            } else if (selectable) {
                builder.append("|choice=true");
            } else {
                builder.append("|gacha=true");
            }
            builder.append("|rewards=").append(normalizedRewards);
        }
        builder.append("|uid=").append(uid == null || uid.isBlank() ? UUID.randomUUID() : uid);
        return builder.toString();
    }

    private static ItemStack stackFromId(String itemId, int count) {
        return StackCodec.fromConfigString(itemId, count);
    }

    private static Entry parseEntry(Mode mode, int index, String line, ServerPlayer player, EntryBuildContext context) {
        if (line == null) return null;
        String trimmed = line.trim();
        String[] parts = trimmed.split("\\|");
        if (parts.length < 3) return null;
        ItemStack parsedStack = StackCodec.fromConfigString(parts[0].trim());
        if (parsedStack.isEmpty()) return null;
        int count = parsedStack.getCount();
        try {
            String currency = normalizePaymentId(parts[1].trim());
            if (currency.isEmpty()) return null;
            long price = Long.parseLong(parts[2].trim());
            if (price <= 0L) return null;
            int timedLimitSeconds = 0;
            int totalLimit = 0;
            String pageName = "";
            String displayName = "";
            String description = "";
            String command = "";
            int requiredQuestCount = 0;
            List<Long> questIds = new ArrayList<>();
            boolean gacha = false;
            boolean selectable = false;
            List<Reward> rewards = new ArrayList<>();
            for (int i = 3; i < parts.length; i++) {
                String option = parts[i].trim();
                if (option.isEmpty()) continue;
                int eq = option.indexOf('=');
                String key = eq > 0 ? option.substring(0, eq).trim().toLowerCase() : option.toLowerCase();
                String value = eq > 0 ? option.substring(eq + 1).trim() : "true";
                switch (key) {
                    case "time" -> timedLimitSeconds = sanitizeTimedSeconds(parsePositiveInt(value));
                    case "limit" -> totalLimit = parsePositiveInt(value);
                    case "page" -> pageName = sanitizePageName(value);
                    case "name", "title", "displayname" -> displayName = sanitizeDisplayName(value);
                    case "description", "desc" -> description = sanitizeDescription(value);
                    case "command", "cmd" -> command = sanitizeCommand(value);
                    case "questneed" -> requiredQuestCount = parsePositiveInt(value);
                    case "quests" -> questIds = parseQuestIds(value);
                    case "gacha" -> gacha = Boolean.parseBoolean(value);
                    case "choice" -> selectable = Boolean.parseBoolean(value);
                    case "rewards" -> rewards = parseRewards(value);
                    default -> {
                    }
                }
            }
            if (mode == Mode.SELL && !rewards.isEmpty()) selectable = true;
            if (selectable && rewards.isEmpty()) return null;
            if (mode == Mode.BUY && !selectable && !rewards.isEmpty()) gacha = true;
            if (mode == Mode.SELL) gacha = false;
            if (selectable) gacha = false;
            if (mode == Mode.BUY && gacha && rewards.isEmpty()) return null;
            ItemStack stack = parsedStack.copy();
            stack.setCount(Math.max(1, Math.min(count, stack.getMaxStackSize())));
            String limitKey = limitKey(mode, index, trimmed);
            EntryBuildContext safeContext = context == null ? new EntryBuildContext(player) : context;
            if (mode == Mode.SELL && player != null && !rewards.isEmpty()) rewards = sellRewardsWithSnapshots(player, limitKey, rewards, safeContext);
            if (mode == Mode.SELL && selectable && rewards.isEmpty()) return null;
            int dailyBought = 0;
            int totalBought = player == null ? 0 : totalBought(player, limitKey);
            long timedRemainingSeconds = player == null ? 0L : timedRemainingSeconds(player, limitKey, timedLimitSeconds);
            MaterialStorage.Snapshot materialSnapshot = MaterialStorage.Snapshot.empty();
            long walletCount = 0L;
            if (player != null) {
                if (mode == Mode.SELL) {
                    materialSnapshot = safeContext.snapshot(stack);
                } else {
                    ItemStack paymentStack = paymentStack(currency);
                    if (!paymentStack.isEmpty()) materialSnapshot = safeContext.snapshot(paymentStack);
                    if (isCurrencyPayment(currency)) walletCount = safeContext.walletPaymentCount(currency);
                }
            }
            long sellProgress = mode == Mode.SELL && player != null ? sellProgress(player, limitKey) : 0L;
            requiredQuestCount = sanitizeRequiredQuestCount(requiredQuestCount, questIds.size());
            List<Long> completedQuestIds = completedQuestIds(safeContext, questIds);
            int requiredQuestCompleted = completedQuestIds.size();
            boolean unlocked = areQuestsCompleted(safeContext, questIds, requiredQuestCount);
            List<String> titles = questTitles(safeContext, questIds);
            long primaryQuestId = primaryQuestId(safeContext, questIds);
            String primaryTitle = primaryQuestTitle(primaryQuestId, questIds, titles);
            return new Entry(mode, index, limitKey, stack, currency, price, timedLimitSeconds, totalLimit, pageName, displayName, description, command, requiredQuestCount, requiredQuestCompleted, completedQuestIds, primaryQuestId, primaryTitle, questIds, titles, unlocked, gacha, selectable, rewards, dailyBought, totalBought, timedRemainingSeconds, materialSnapshot.inventoryCount(), walletCount, materialSnapshot.backpackCount(), materialSnapshot.rsCount(), materialSnapshot.backpackLoaded(), materialSnapshot.hasBackpack(), materialSnapshot.rsLoaded(), materialSnapshot.rsState().name(), sellProgress);
        } catch (Exception ignored) {
            return null;
        }
    }


    private static String normalizeQuestText(String questText) {
        List<Long> ids = parseQuestIds(questText);
        if (ids.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (Long id : ids) {
            if (id == null || id == 0L) continue;
            if (!builder.isEmpty()) builder.append(',');
            builder.append(Long.toUnsignedString(id));
        }
        return builder.toString();
    }

    private static int normalizeRequiredQuestCount(String questText, String normalizedQuests) {
        int total = parseQuestIds(normalizedQuests).size();
        int value = 0;
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            String raw = option.substring(eq + 1).trim();
            if (key.equals("questneed")) {
                value = parsePositiveInt(raw);
            }
        }
        return sanitizeRequiredQuestCount(value, total);
    }

    private static String normalizePageName(String questText) {
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            String raw = option.substring(eq + 1).trim();
            if (key.equals("page")) {
                return sanitizePageName(raw);
            }
        }
        return "";
    }

    private static List<String> splitOptionTokens(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;
        for (String raw : text.split(";")) {
            String value = raw.trim();
            if (!value.isEmpty()) result.add(value);
        }
        return result;
    }

    private static int sanitizeRequiredQuestCount(int value, int total) {
        if (value <= 0 || total <= 0) return 0;
        return Math.min(value, total);
    }

    private static String sanitizePageName(String value) {
        if (value == null) return "";
        String text = value.trim().replace("|", "/").replace(";", "/");
        if (text.length() > 24) text = text.substring(0, 24);
        return text;
    }

    private static String normalizePaymentId(String value) {
        if (value == null || value.isBlank()) return "";
        String text = value.trim();
        if (isCurrencyPayment(text)) return text;
        ItemStack stack = paymentStack(text);
        return stack.isEmpty() ? "" : text;
    }

    private static boolean isCurrencyPayment(String value) {
        return value != null && Data.currencyMap().containsKey(value);
    }

    private static ItemStack paymentStack(String value) {
        if (value == null || value.isBlank()) return ItemStack.EMPTY;
        ItemStack stack = StackCodec.fromConfigString(value, 1);
        if (!stack.isEmpty()) stack.setCount(1);
        return stack;
    }

    private static String sanitizeDisplayName(String value) {
        if (value == null) return "";
        String text = value.trim().replace("|", "/").replace(";", "/");
        if (text.length() > 48) text = text.substring(0, 48);
        return text;
    }

    private static String sanitizeDescription(String value) {
        if (value == null) return "";
        String text = value.trim().replace("|", "/").replace(";", "/").replace('\n', ' ').replace('\r', ' ');
        if (text.length() > 256) text = text.substring(0, 256);
        return text;
    }

    private static String sanitizeCommand(String value) {
        if (value == null) return "";
        String text = value.trim().replace("|", " ").replace(";", " ");
        if (text.startsWith("/")) text = text.substring(1);
        if (text.length() > 2048) text = text.substring(0, 2048);
        return text;
    }

    private static String normalizeDisplayName(String questText) {
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            if (key.equals("name") || key.equals("title") || key.equals("displayname")) return sanitizeDisplayName(option.substring(eq + 1));
        }
        return "";
    }

    private static String normalizeDescription(String questText) {
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            if (key.equals("description") || key.equals("desc")) return sanitizeDescription(option.substring(eq + 1));
        }
        return "";
    }

    private static String normalizeCommand(String questText) {
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            if (key.equals("command") || key.equals("cmd")) return sanitizeCommand(option.substring(eq + 1));
        }
        return "";
    }


    private static boolean normalizeSelectableReward(String questText) {
        for (String option : splitOptionTokens(questText)) {
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            String raw = option.substring(eq + 1).trim();
            if (key.equals("choice")) {
                return Boolean.parseBoolean(raw);
            }
        }
        return false;
    }

    private static String normalizeRewardsText(String rewardsText) {
        if (rewardsText == null || rewardsText.isBlank()) return "";
        return rewardsText.trim();
    }

    private static List<Long> parseQuestIds(String value) {
        List<Long> ids = new ArrayList<>();
        if (value == null || value.isBlank()) return ids;
        for (String raw : value.split("[,;]")) {
            String token = raw.trim();
            if (token.isEmpty() || token.contains("=")) continue;
            long id = FTBQuestApi.parseQuestId(token);
            if (id != 0L && !ids.contains(id)) ids.add(id);
        }
        return ids;
    }

    private static boolean areQuestsCompleted(EntryBuildContext context, List<Long> questIds, int requiredQuestCount) {
        if (questIds == null || questIds.isEmpty()) return true;
        if (context == null || context.player() == null) return false;
        int valid = 0;
        int completed = 0;
        for (Long questId : questIds) {
            if (questId == null || questId == 0L) continue;
            valid++;
            if (context.questCompleted(questId)) completed++;
        }
        if (valid <= 0) return true;
        int need = requiredQuestCount <= 0 ? valid : Math.min(requiredQuestCount, valid);
        return completed >= need;
    }

    private static List<Long> completedQuestIds(EntryBuildContext context, List<Long> questIds) {
        List<Long> result = new ArrayList<>();
        if (context == null || context.player() == null || questIds == null) return result;
        for (Long questId : questIds) {
            if (questId == null || questId == 0L) continue;
            if (context.questCompleted(questId) && !result.contains(questId)) result.add(questId);
        }
        return result;
    }

    private static List<String> questTitles(EntryBuildContext context, List<Long> questIds) {
        List<String> titles = new ArrayList<>();
        if (context == null || questIds == null) return titles;
        for (Long questId : questIds) {
            if (questId == null || questId == 0L) continue;
            titles.add(context.questTitle(questId));
        }
        return titles;
    }

    private static long primaryQuestId(EntryBuildContext context, List<Long> questIds) {
        if (questIds == null || questIds.isEmpty()) return 0L;
        if (context != null && context.player() != null) {
            for (Long questId : questIds) {
                if (questId != null && questId != 0L && !context.questCompleted(questId)) return questId;
            }
        }
        Long first = questIds.get(0);
        return first == null ? 0L : first;
    }

    private static String primaryQuestTitle(long primaryQuestId, List<Long> questIds, List<String> titles) {
        if (primaryQuestId == 0L) return "";
        if (questIds == null || titles == null) return Long.toUnsignedString(primaryQuestId);
        for (int i = 0; i < questIds.size() && i < titles.size(); i++) {
            Long id = questIds.get(i);
            if (id != null && id == primaryQuestId) return titles.get(i);
        }
        return Long.toUnsignedString(primaryQuestId);
    }

    private static ListTag questIdsTag(List<Long> questIds) {
        ListTag list = new ListTag();
        if (questIds == null) return list;
        for (Long id : questIds) {
            if (id != null && id != 0L) list.add(net.minecraft.nbt.LongTag.valueOf(id));
        }
        return list;
    }

    private static List<Long> questIdsFromTag(ListTag list) {
        List<Long> ids = new ArrayList<>();
        for (Tag tag : list) {
            if (!(tag instanceof net.minecraft.nbt.NumericTag numericTag)) continue;
            long id = numericTag.getAsLong();
            if (id != 0L && !ids.contains(id)) ids.add(id);
        }
        return ids;
    }

    private static ListTag questTitlesTag(List<String> titles) {
        ListTag list = new ListTag();
        if (titles == null) return list;
        for (String title : titles) {
            list.add(net.minecraft.nbt.StringTag.valueOf(title == null ? "" : title));
        }
        return list;
    }

    private static List<String> questTitlesFromTag(ListTag list) {
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) titles.add(list.getString(i));
        return titles;
    }
    private static List<Reward> sellRewardsWithSnapshots(ServerPlayer player, String limitKey, List<Reward> rewards, EntryBuildContext context) {
        List<Reward> result = new ArrayList<>();
        if (rewards == null) return result;
        EntryBuildContext safeContext = context == null ? new EntryBuildContext(player) : context;
        for (int i = 0; i < rewards.size(); i++) {
            Reward reward = rewards.get(i);
            if (reward == null || reward.empty()) continue;
            MaterialStorage.Snapshot snapshot = safeContext.snapshot(reward.stack());
            String progressKey = sellOptionProgressKey(limitKey, i);
            result.add(reward.withSellSnapshot(snapshot, sellProgress(player, progressKey)));
        }
        return result;
    }

    private static String sellOptionProgressKey(String baseKey, int selectedIndex) {
        return selectedIndex < 0 ? baseKey : baseKey + "#" + selectedIndex;
    }

    private static SellMaterial selectedSellMaterial(Entry entry, int selectedIndex) {
        if (entry != null && entry.selectable() && entry.rewards() != null && !entry.rewards().isEmpty()) {
            int safeIndex = Math.max(0, Math.min(selectedIndex, entry.rewards().size() - 1));
            Reward reward = entry.rewards().get(safeIndex);
            if (reward != null && !reward.empty()) return new SellMaterial(reward.stack(), sellOptionProgressKey(entry.key(), safeIndex));
        }
        return new SellMaterial(entry == null ? ItemStack.EMPTY : entry.stack(), entry == null ? "" : entry.key());
    }

    private static int parsePositiveInt(String value) {
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static List<Reward> parseRewards(String text) {
        List<Reward> rewards = new ArrayList<>();
        if (text == null || text.isBlank()) return rewards;
        for (String raw : text.split(",")) {
            Reward reward = parseReward(raw.trim());
            if (reward != null) rewards.add(reward);
        }
        return withChances(rewards);
    }

    private static Reward parseReward(String text) {
        if (text == null || text.isBlank()) return null;
        int at = text.indexOf('@');
        if (at <= 0 || at >= text.length() - 1) return null;
        String itemText = text.substring(0, at).trim();
        String weightAndOptions = text.substring(at + 1).trim();
        String[] optionParts = weightAndOptions.split("~");
        int weight = parsePositiveInt(optionParts.length == 0 ? weightAndOptions : optionParts[0]);
        if (weight <= 0) return null;
        String displayName = "";
        String command = "";
        for (int i = 1; i < optionParts.length; i++) {
            String option = optionParts[i].trim();
            if (option.isEmpty()) continue;
            int eq = option.indexOf('=');
            if (eq <= 0) continue;
            String key = option.substring(0, eq).trim().toLowerCase();
            String value = option.substring(eq + 1).trim();
            if (key.equals("name") || key.equals("title") || key.equals("displayname")) displayName = sanitizeDisplayName(value);
            else if (key.equals("command") || key.equals("cmd")) command = sanitizeCommand(value);
        }
        if (itemText.equalsIgnoreCase("empty") || itemText.equalsIgnoreCase("air") || itemText.equalsIgnoreCase("none")) {
            return new Reward(ItemStack.EMPTY, weight, 0.0D, displayName, command);
        }
        ItemStack stack = StackCodec.fromConfigString(itemText);
        if (stack.isEmpty()) return null;
        return new Reward(stack, weight, 0.0D, displayName, command);
    }

    private static List<Reward> withChances(List<Reward> rewards) {
        int total = 0;
        for (Reward reward : rewards) total += Math.max(0, reward.weight());
        if (total <= 0) return rewards;
        List<Reward> result = new ArrayList<>();
        for (Reward reward : rewards) {
            result.add(new Reward(reward.stack(), reward.weight(), reward.weight() * 100.0D / total, reward.inventoryCount(), reward.backpackCount(), reward.rsCount(), reward.backpackLoaded(), reward.hasBackpack(), reward.rsLoaded(), reward.rsState(), reward.sellProgress(), reward.displayName(), reward.command()));
        }
        return result;
    }

    private static ListTag rewardsTag(List<Reward> rewards) {
        ListTag list = new ListTag();
        if (rewards == null) return list;
        for (Reward reward : rewards) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Empty", reward.empty());
            if (!reward.empty()) tag.put("Item", reward.stack().save(new CompoundTag()));
            tag.putInt("Weight", reward.weight());
            tag.putDouble("Chance", reward.chance());
            tag.putString("DisplayName", reward.displayName() == null ? "" : reward.displayName());
            tag.putString("Command", reward.command() == null ? "" : reward.command());
            tag.putLong("InventoryCount", reward.inventoryCount());
            tag.putLong("BackpackCount", reward.backpackCount());
            tag.putLong("RsCount", reward.rsCount());
            tag.putBoolean("BackpackLoaded", reward.backpackLoaded());
            tag.putBoolean("HasBackpack", reward.hasBackpack());
            tag.putBoolean("RsLoaded", reward.rsLoaded());
            tag.putString("RsState", reward.rsState());
            tag.putLong("SellProgress", reward.sellProgress());
            list.add(tag);
        }
        return list;
    }

    private static List<Reward> rewardsFromTag(ListTag list) {
        List<Reward> rewards = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ItemStack stack = tag.getBoolean("Empty") ? ItemStack.EMPTY : ItemStack.of(tag.getCompound("Item"));
            int weight = tag.getInt("Weight");
            double chance = tag.getDouble("Chance");
            String displayName = tag.getString("DisplayName");
            String command = tag.getString("Command");
            long inventoryCount = tag.getLong("InventoryCount");
            long backpackCount = tag.getLong("BackpackCount");
            long rsCount = tag.getLong("RsCount");
            boolean backpackLoaded = tag.getBoolean("BackpackLoaded");
            boolean hasBackpack = tag.getBoolean("HasBackpack");
            boolean rsLoaded = tag.getBoolean("RsLoaded");
            String rsState = tag.getString("RsState");
            long sellProgress = tag.getLong("SellProgress");
            if (weight > 0) rewards.add(new Reward(stack, weight, chance, inventoryCount, backpackCount, rsCount, backpackLoaded, hasBackpack, rsLoaded, rsState, sellProgress, displayName, command));
        }
        return rewards;
    }

    private static int unpackRequestedAmount(int amount) {
        int value = amount;
        if (value >= SELECTED_REWARD_AMOUNT_FACTOR) {
            value %= SELECTED_REWARD_AMOUNT_FACTOR;
            if (value == 0) return 1;
        }
        return Math.min(64, value);
    }

    private static int unpackSelectedRewardIndex(int amount) {
        if (amount < SELECTED_REWARD_AMOUNT_FACTOR) return -1;
        return Math.max(0, amount / SELECTED_REWARD_AMOUNT_FACTOR - 1);
    }

    private static Reward selectedReward(List<Reward> rewards, int index) {
        if (rewards == null || rewards.isEmpty()) return null;
        int safeIndex = Math.max(0, Math.min(index, rewards.size() - 1));
        return rewards.get(safeIndex);
    }

    private static Reward rollReward(ServerPlayer player, List<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) return null;
        int total = 0;
        for (Reward reward : rewards) total += Math.max(0, reward.weight());
        if (total <= 0) return null;
        int value = player.getRandom().nextInt(total) + 1;
        int cursor = 0;
        for (Reward reward : rewards) {
            cursor += Math.max(0, reward.weight());
            if (value <= cursor) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    private static int totalRemaining(Entry entry) {
        if (entry == null) return 0;
        if (entry.totalLimit() <= 0) return 64;
        return Math.max(0, entry.totalLimit() - entry.totalBought());
    }

    private static void addUsage(ServerPlayer player, Entry entry, int amount) {
        if (player == null || entry == null || amount <= 0) return;
        CompoundTag root = usageRoot(player);
        if (entry.dailyLimit() > 0) {
            CompoundTag timedRoot = root.getCompound(TIMED_KEY);
            CompoundTag timed = timedRoot.getCompound(entry.key());
            timed.putLong("NextReadySecond", playSecond(player) + entry.dailyLimit());
            timedRoot.put(entry.key(), timed);
            root.put(TIMED_KEY, timedRoot);
        }
        CompoundTag totalRoot = root.getCompound(TOTAL_KEY);
        totalRoot.putInt(entry.key(), Math.max(0, totalRoot.getInt(entry.key())) + amount);
        root.put(TOTAL_KEY, totalRoot);
        player.getPersistentData().put(USAGE_KEY, root);
    }

    private static long timedRemainingSeconds(ServerPlayer player, String key, int timedLimitSeconds) {
        if (player == null || key == null || key.isBlank() || timedLimitSeconds <= 0) return 0L;
        CompoundTag timedRoot = usageRoot(player).getCompound(TIMED_KEY);
        CompoundTag timed = timedRoot.getCompound(key);
        long nextReady = timed.getLong("NextReadySecond");
        if (nextReady <= 0L) return 0L;
        return Math.max(0L, nextReady - playSecond(player));
    }

    private static int totalBought(ServerPlayer player, String key) {
        if (player == null || key == null || key.isBlank()) return 0;
        return Math.max(0, usageRoot(player).getCompound(TOTAL_KEY).getInt(key));
    }

    private static CompoundTag usageRoot(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(USAGE_KEY, Tag.TAG_COMPOUND)) data.put(USAGE_KEY, new CompoundTag());
        return data.getCompound(USAGE_KEY);
    }

    private static long playSecond(ServerPlayer player) {
        int playTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        return Math.max(0L, playTicks / TICKS_PER_SECOND);
    }

    private static int sanitizeTimedSeconds(int seconds) {
        return Math.max(seconds, 0);
    }

    private static String formatDuration(long seconds) {
        long value = Math.max(seconds, 0L);
        if (value < 60L) return value + "s";
        if (value < 3600L) return (value / 60L) + "m " + (value % 60L) + "s";
        if (value < 86400L) return (value / 3600L) + "h " + ((value % 3600L) / 60L) + "m";
        return (value / 86400L) + "d " + ((value % 86400L) / 3600L) + "h";
    }

    private static String formatMaterialSource(long count, boolean loaded, boolean available, String type) {
        if (!loaded) return "hidden";
        if (!available) return type.equals("rs") ? "unbound" : "missing";
        return formatExact(count);
    }

    private static String formatExact(long value) {
        return String.format(java.util.Locale.ROOT, "%,d", Math.max(0L, value));
    }

    private static long sellProgress(ServerPlayer player, String key) {
        if (player == null || key == null || key.isBlank()) return 0L;
        return Math.max(0L, player.getPersistentData().getCompound(SELL_PROGRESS_KEY).getLong(key));
    }

    private static void setSellProgress(ServerPlayer player, String key, long value) {
        if (player == null || key == null || key.isBlank()) return;
        CompoundTag root = player.getPersistentData().getCompound(SELL_PROGRESS_KEY);
        if (value > 0L) root.putLong(key, value);
        else root.remove(key);
        player.getPersistentData().put(SELL_PROGRESS_KEY, root);
    }

    private static String limitKey(Mode mode, int index, String line) {
        String uid = entryUid(line);
        if (!uid.isBlank()) {
            return UUID.nameUUIDFromBytes((mode.name() + ":" + uid).getBytes(StandardCharsets.UTF_8)).toString();
        }
        UUID uuid = UUID.nameUUIDFromBytes((mode.name() + ":" + index + ":" + line).getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

    private static String entryUid(String line) {
        if (line == null || line.isBlank()) return "";
        String[] parts = line.split("\\|");
        for (String part : parts) {
            String value = part == null ? "" : part.trim();
            if (!value.regionMatches(true, 0, "uid=", 0, 4)) continue;
            String uid = value.substring(4).trim();
            if (uid.matches("[A-Za-z0-9_-]{8,64}")) return uid;
        }
        return "";
    }

    private static String withEntryUid(String line) {
        if (line == null || line.isBlank() || !entryUid(line).isBlank()) return line;
        return line + "|uid=" + UUID.randomUUID();
    }

    private static void giveRepeated(ServerPlayer player, ItemStack stack, int times) {
        long total = safeMultiply(stack.getCount(), times);
        while (total > 0L) {
            ItemStack copy = stack.copy();
            int count = (int) Math.min(total, copy.getMaxStackSize());
            copy.setCount(count);
            giveStack(player, copy);
            total -= count;
        }
    }

    private static void executeShopCommand(ServerPlayer player, String command, int amount) {
        if (player == null || command == null || command.isBlank()) return;
        String normalized = command.replace("%player%", player.getScoreboardName()).replace("{player}", player.getScoreboardName()).replace("%amount%", String.valueOf(amount)).replace("{amount}", String.valueOf(amount));
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), normalized);
    }

    private static void giveStack(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        if (a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    public enum Mode {
        BUY,
        SELL
    }

    public record Entry(Mode mode, int index, String key, ItemStack stack, String currencyId, long price, int dailyLimit, int totalLimit, String pageName, String displayName, String description, String command, int requiredQuestCount, int requiredQuestCompleted, List<Long> requiredQuestCompletedIds, long requiredQuestId, String requiredQuestTitle, List<Long> requiredQuestIds, List<String> requiredQuestTitles, boolean unlocked, boolean gacha, boolean selectable, List<Reward> rewards, int dailyBought, int totalBought, long timedRemainingSeconds, long inventoryCount, long walletCount, long backpackCount, long rsCount, boolean backpackLoaded, boolean hasBackpack, boolean rsLoaded, String rsState, long sellProgress) {
        public boolean locked() {
            return requiredQuestIds != null && !requiredQuestIds.isEmpty() && !unlocked;
        }

        public int requiredQuestNeed() {
            int total = requiredQuestIds == null ? 0 : requiredQuestIds.size();
            if (total == 0) return 0;
            return requiredQuestCount <= 0 ? total : Math.min(requiredQuestCount, total);
        }

        public boolean isQuestCompleted(long questId) {
            return requiredQuestCompletedIds != null && requiredQuestCompletedIds.contains(questId);
        }

        public String safePageName() {
            return pageName == null || pageName.isBlank() ? "" : pageName;
        }

        public int timedLimitSeconds() {
            return dailyLimit;
        }

        public long totalMaterials() {
            return safeAdd(safeAdd(inventoryCount, backpackCount), rsCount);
        }
    }

    private static String itemNameArg(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        return "tr:" + stack.getDescriptionId();
    }

    private static String itemNameArg(String itemId) {
        if (itemId == null || itemId.isBlank()) return "";
        try {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item == null || item == Items.AIR) return itemId;
            return "tr:" + item.getDescriptionId();
        } catch (Exception ignored) {
            return itemId;
        }
    }

    private static String packArgs(String... args) {
        if (args == null || args.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (String arg : args) builder.append("|").append(arg == null ? "" : arg.replace("|", "/"));
        return builder.toString();
    }

    public record BuyResult(boolean success, String messageKey, String[] args, int amount, long price) {
        static BuyResult summary(Entry entry, int requested, int completed, long spent, boolean fullSuccess) {
            int failed = Math.max(0, requested - completed);
            String currencyName = itemNameArg(entry.currencyId());
            if (!entry.selectable() && !entry.gacha() && entry.command() != null && !entry.command().isBlank()) {
                String key = fullSuccess ? "msg.adventuresystems.curios.wallet.shop_buy_success_command" : completed > 0 ? "msg.adventuresystems.curios.wallet.shop_buy_partial_command" : "msg.adventuresystems.curios.wallet.shop_buy_fail_summary_command";
                String[] args = fullSuccess
                        ? new String[]{formatExact(completed), formatExact(spent), currencyName}
                        : new String[]{formatExact(requested), formatExact(completed), formatExact(failed), formatExact(spent), currencyName};
                return new BuyResult(fullSuccess, key, args, completed, spent);
            }
            if (entry.selectable()) {
                String key = fullSuccess ? "msg.adventuresystems.curios.wallet.shop_buy_success_choice" : completed > 0 ? "msg.adventuresystems.curios.wallet.shop_buy_partial_choice" : "msg.adventuresystems.curios.wallet.shop_buy_fail_summary_choice";
                String[] args = fullSuccess
                        ? new String[]{formatExact(completed), formatExact(spent), currencyName, formatExact(completed)}
                        : new String[]{formatExact(requested), formatExact(completed), formatExact(failed), formatExact(spent), currencyName, formatExact(completed)};
                return new BuyResult(fullSuccess, key, args, completed, spent);
            }
            if (entry.gacha()) {
                String key = fullSuccess ? "msg.adventuresystems.curios.wallet.shop_buy_success_gacha" : completed > 0 ? "msg.adventuresystems.curios.wallet.shop_buy_partial_gacha" : "msg.adventuresystems.curios.wallet.shop_buy_fail_summary_gacha";
                String[] args = fullSuccess
                        ? new String[]{formatExact(completed), formatExact(spent), currencyName, formatExact(completed)}
                        : new String[]{formatExact(requested), formatExact(completed), formatExact(failed), formatExact(spent), currencyName, formatExact(completed)};
                return new BuyResult(fullSuccess, key, args, completed, spent);
            }
            long gained = safeMultiply(entry.stack().getCount(), completed);
            String itemName = itemNameArg(entry.stack());
            String key = fullSuccess ? "msg.adventuresystems.curios.wallet.shop_buy_success_item" : completed > 0 ? "msg.adventuresystems.curios.wallet.shop_buy_partial_item" : "msg.adventuresystems.curios.wallet.shop_buy_fail_summary_item";
            String[] args = fullSuccess
                    ? new String[]{formatExact(completed), formatExact(spent), currencyName, formatExact(gained), itemName}
                    : new String[]{formatExact(requested), formatExact(completed), formatExact(failed), formatExact(spent), currencyName, formatExact(gained), itemName};
            return new BuyResult(fullSuccess, key, args, completed, spent);
        }

        static BuyResult fail(String key, String... args) {
            return new BuyResult(false, key, args == null ? new String[0] : args, 0, 0L);
        }
    }

    public record SellResult(boolean changed, boolean completedAny, long requested, long completed, long consumed, long fromInventory, long fromBackpack, long fromRs, long missing, long progress, long perTrade, long inventoryLeft, long backpackLeft, long rsLeft, long paid, String materialName, String currencyName, String messageKey) {
        static SellResult fail(String key, String... args) {
            return new SellResult(false, false, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, "", "", key + packArgs(args));
        }

        public String noticeKey() {
            if (!changed) return messageKey;
            long failed = Math.max(0L, requested - completed);
            if (completedAny && (failed > 0L || missing > 0L)) {
                return "msg.adventuresystems.curios.wallet.shop_sell_partial_summary" + packArgs(formatExact(requested), formatExact(completed), formatExact(failed), formatExact(consumed), materialName, formatExact(paid), currencyName, formatExact(missing), materialName, formatExact(progress), formatExact(perTrade));
            }
            if (completedAny) {
                return "msg.adventuresystems.curios.wallet.shop_sell_success_summary" + packArgs(formatExact(completed), formatExact(consumed), materialName, formatExact(paid), currencyName);
            }
            return "msg.adventuresystems.curios.wallet.shop_sell_progress_summary" + packArgs(formatExact(consumed), materialName, formatExact(progress), formatExact(perTrade), formatExact(missing), materialName);
        }
    }

    public record Reward(ItemStack stack, int weight, double chance, long inventoryCount, long backpackCount, long rsCount, boolean backpackLoaded, boolean hasBackpack, boolean rsLoaded, String rsState, long sellProgress, String displayName, String command) {

        public Reward(ItemStack stack, int weight, double chance, String displayName, String command) {
            this(stack, weight, chance, 0L, 0L, 0L, false, false, false, "HIDDEN", 0L, displayName == null ? "" : displayName, command == null ? "" : command);
        }

        public boolean empty() {
            return stack == null || stack.isEmpty();
        }

        public boolean commandReward() {
            return command != null && !command.isBlank();
        }

        public long totalMaterials() {
            return safeAdd(safeAdd(inventoryCount, backpackCount), rsCount);
        }

        public Reward withSellSnapshot(MaterialStorage.Snapshot snapshot, long progress) {
            MaterialStorage.Snapshot safe = snapshot == null ? MaterialStorage.Snapshot.empty() : snapshot;
            return new Reward(stack, weight, chance, safe.inventoryCount(), safe.backpackCount(), safe.rsCount(), safe.backpackLoaded(), safe.hasBackpack(), safe.rsLoaded(), safe.rsState().name(), progress, displayName, command);
        }
    }


    private static final class EntryBuildContext {
        private final ServerPlayer player;
        private final boolean useBackpack;
        private final boolean useRs;
        private final Map<String, MaterialStorage.Snapshot> snapshots = new HashMap<>();
        private final Map<String, Long> walletCounts = new HashMap<>();
        private final Map<Long, Boolean> questCompleted = new HashMap<>();
        private final Map<Long, String> questTitles = new HashMap<>();

        private EntryBuildContext(ServerPlayer player) {
            this.player = player;
            this.useBackpack = usesBackpack(player);
            this.useRs = usesRs(player);
        }

        private ServerPlayer player() {
            return player;
        }

        private MaterialStorage.Snapshot snapshot(ItemStack stack) {
            if (player == null || stack == null || stack.isEmpty()) return MaterialStorage.Snapshot.empty();
            String key = stackKey(stack);
            return snapshots.computeIfAbsent(key, ignored -> MaterialStorage.snapshot(player, stack, useBackpack, useRs));
        }

        private long walletPaymentCount(String currencyId) {
            if (player == null || currencyId == null || currencyId.isBlank()) return 0L;
            return walletCounts.computeIfAbsent(currencyId, id -> Data.countForAutomaticPayment(player, id));
        }

        private boolean questCompleted(long questId) {
            if (player == null || questId == 0L) return false;
            return questCompleted.computeIfAbsent(questId, id -> FTBQuestApi.isQuestCompleted(player, id));
        }

        private String questTitle(long questId) {
            if (player == null || questId == 0L) return Long.toUnsignedString(questId);
            return questTitles.computeIfAbsent(questId, id -> FTBQuestApi.questTitle(player, id));
        }

        private String stackKey(ItemStack stack) {
            String key = StackCodec.toConfigString(stack, 1);
            if (!key.isEmpty()) return key;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return id == null ? "" : id.toString();
        }
    }

    private record SellMaterial(ItemStack stack, String progressKey) {
    }
}

