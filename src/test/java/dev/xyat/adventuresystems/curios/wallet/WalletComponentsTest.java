//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.wallet;

import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.storage.WalletMaterialMatcher;
import dev.xyat.adventuresystems.data.AdventureItemData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Run after Minecraft registries have been bootstrapped, including in the runtime fixture. *^/
public final class WalletComponentsTest {
    @org.junit.jupiter.api.BeforeAll
    static void bootstrap() throws Exception {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());
        net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("wallet-tests-"));
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @org.junit.jupiter.api.Test
    void componentConfigAndMaterialVariantsSurvive() { run(); }

    @org.junit.jupiter.api.Test
    void shopRewardGrammarIgnoresComponentDelimiters() throws Exception {
        ItemStack source = new ItemStack(Items.DIAMOND, 5);
        source.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("x@a,b|c;d~e"));
        AdventureItemData.updateCustomData(source, tag -> tag.putLong("Value", 42L));
        String itemText = StackCodec.toConfigString(source, 5);
        var method = dev.xyat.adventuresystems.curios.wallet.shop.Shop.class.getDeclaredMethod("parseRewards", String.class);
        method.setAccessible(true);
        var rewards = (java.util.List<?>) method.invoke(null, itemText + "@2,minecraft:emerald*3@1");
        check(rewards.size() == 2, "Component punctuation split the reward pool");
        var first = (dev.xyat.adventuresystems.curios.wallet.shop.Shop.Reward) rewards.get(0);
        check(first.stack().getCount() == 5 && ItemStack.isSameItemSameComponents(source, first.stack()), "Reward parser changed components");
    }

    @org.junit.jupiter.api.Test
    void shopEntryGrammarIgnoresComponentDelimiters() throws Exception {
        ItemStack source = new ItemStack(Items.DIAMOND, 5);
        source.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("x|uid=abcdefgh|@a,b;c~d"));
        AdventureItemData.updateCustomData(source, tag -> tag.putLong("Value", 42L));
        String itemText = StackCodec.toConfigString(source, 5);
        String line = itemText + "|minecraft:emerald|2|uid=real_entry_id|icon=" + itemText + "|rewards=" + itemText + "@2,minecraft:emerald*3@1";
        var method = java.util.Arrays.stream(dev.xyat.adventuresystems.curios.wallet.shop.Shop.class.getDeclaredMethods()).filter(m -> m.getName().equals("parseEntry")).findFirst().orElseThrow();
        method.setAccessible(true);
        var entry = (dev.xyat.adventuresystems.curios.wallet.shop.Shop.Entry) method.invoke(null, dev.xyat.adventuresystems.curios.wallet.shop.Shop.Mode.BUY, 0, line, null, null);
        check(entry != null, "Component punctuation split the shop entry");
        check(entry.stack().getCount() == 5 && ItemStack.isSameItemSameComponents(source, entry.stack()), "Entry parser changed components");
        var uid = dev.xyat.adventuresystems.curios.wallet.shop.Shop.class.getDeclaredMethod("entryUid", String.class);
        uid.setAccessible(true);
        check("real_entry_id".equals(uid.invoke(null, line)), "Component name was interpreted as entry identity");
        var encode = dev.xyat.adventuresystems.curios.wallet.shop.Shop.class.getDeclaredMethod("entryToTag", dev.xyat.adventuresystems.curios.wallet.shop.Shop.Entry.class);
        var decode = dev.xyat.adventuresystems.curios.wallet.shop.Shop.class.getDeclaredMethod("entryFromTag", dev.xyat.adventuresystems.curios.wallet.shop.Shop.Mode.class, net.minecraft.nbt.CompoundTag.class);
        encode.setAccessible(true);
        decode.setAccessible(true);
        var wire = (net.minecraft.nbt.CompoundTag) encode.invoke(null, entry);
        var received = (dev.xyat.adventuresystems.curios.wallet.shop.Shop.Entry) decode.invoke(null, dev.xyat.adventuresystems.curios.wallet.shop.Shop.Mode.BUY, wire);
        check(received != null && received.stack().getCount() == 5 && ItemStack.isSameItemSameComponents(source, received.stack()), "Shop wire stack lost components");
        check(ItemStack.isSameItemSameComponents(source, received.icon()), "Shop wire icon lost components");
        check(received.rewards().size() == 2 && received.rewards().get(0).stack().getCount() == 5 && ItemStack.isSameItemSameComponents(source, received.rewards().get(0).stack()), "Shop wire reward lost components");
    }

    @org.junit.jupiter.api.Test
    void unquotedShopNamesDoNotStartComponentQuotes() {
        String[] parts = StackCodec.splitTopLevel("minecraft:diamond|minecraft:emerald|2|name=Player's Item|uid=real_entry_id", '|');
        check(parts.length == 5 && parts[4].equals("uid=real_entry_id"), "Apostrophe in a shop name swallowed later options");
    }

    public static void run() {
        ItemStack source = new ItemStack(Items.DIAMOND, 7);
        source.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Price * 5 #nbt64: is a name"));
        source.remove(DataComponents.RARITY);
        AdventureItemData.updateCustomData(source, tag -> {
            tag.putLong("Balance", Long.MAX_VALUE);
            tag.putByte("Small", (byte) 1);
            tag.putBoolean(WalletMaterialMatcher.PICKED_UP_TAG, true);
        });
        String text = StackCodec.toConfigString(source, 7);
        check(text.startsWith("minecraft:diamond["), "Neo config must use native components");
        ItemStack restored = StackCodec.fromConfigString(text);
        check(restored.getCount() == 7, "Config count was lost");
        check(ItemStack.isSameItemSameComponents(source, restored), "Config component patch was lost");
        check(AdventureItemData.customData(restored).contains("Balance", Tag.TAG_LONG), "Long became a different numeric type");
        check(AdventureItemData.customData(restored).contains("Small", Tag.TAG_BYTE), "Byte became a different numeric type");
        ItemStack changedCount = StackCodec.fromConfigString(StackCodec.withCount(text, 3));
        check(changedCount.getCount() == 3 && ItemStack.isSameItemSameComponents(source, changedCount), "Count edit changed components");
        check(StackCodec.fromConfigString("minecraft:diamond#nbt64:e30").isEmpty(), "Legacy item NBT was accepted on Neo");

        ItemStack clean = source.copy();
        AdventureItemData.updateCustomData(clean, tag -> tag.remove(WalletMaterialMatcher.PICKED_UP_TAG));
        check(WalletMaterialMatcher.matches(source, clean), "Pickup bookkeeping changed material matching");
        check(AdventureItemData.customData(source).getBoolean(WalletMaterialMatcher.PICKED_UP_TAG), "Matching mutated the source stack");
        ItemStack different = clean.copy();
        different.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Different"));
        check(!WalletMaterialMatcher.matches(source, different), "Distinct component material matched");
        var variants = WalletMaterialMatcher.exactVariants(source);
        check(variants.size() == 2, "Exact storage variants were collapsed");
        check(!AdventureItemData.customData(variants.get(0)).contains(WalletMaterialMatcher.PICKED_UP_TAG), "Clean variant retains pickup mark");
        check(AdventureItemData.customData(variants.get(1)).getBoolean(WalletMaterialMatcher.PICKED_UP_TAG), "Marked variant is missing");
        AdventureItemData.updateCustomData(different, tag -> tag.putLong("Balance", 5));
        check(AdventureItemData.customData(source).getLong("Balance") == Long.MAX_VALUE, "CustomData copy was aliased");
        System.out.println("WalletComponentsTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
*///?}
