package dev.xyat.adventuresystems.curios.wallet;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

/**
 * Wallet coins for scripts and other mods. The wallet holds one balance of wallet coins
 * ({@code adventuresystems:adventure_coin}); the coin items it takes in (gold coin, diamond coin …) are worth their
 * configured value in wallet coins.
 *
 * <p>From KubeJS: {@code const Wallet = Java.loadClass('dev.xyat.adventuresystems.curios.wallet.WalletApi')}, then
 * {@code Wallet.give(player, 25)} for 25 wallet coins, or {@code Wallet.give(player, 'kubejs:diamond_coin', 2)} for the
 * value of two diamond coins.</p>
 */
public final class WalletApi {
    private WalletApi() {
    }

    /** The wallet coin and the coin items the wallet takes in, cheapest first. */
    public static List<String> currencies() {
        return Data.coinMap().values().stream()
                .sorted(java.util.Comparator.comparingLong(dev.xyat.adventuresystems.curios.wallet.data.CurrencyType::value))
                .map(dev.xyat.adventuresystems.curios.wallet.data.CurrencyType::itemId).toList();
    }

    /** The full id of the wallet coin or a coin item for an id or short name, or "" when there is none. */
    public static String resolve(String currency) {
        if (currency == null || currency.isBlank()) return "";
        String name = currency.trim().toLowerCase(Locale.ROOT);
        if (name.equals("coin") || name.equals("adventure_coin")) return Data.VIRTUAL_ID;
        for (String candidate : new String[]{name, CuriosModule.MODID + ":" + name, "kubejs:" + name, "kubejs:" + name + "_coin"}) {
            if (Data.unitValue(candidate) > 0L) return candidate;
        }
        return "";
    }

    /** Whether the player wears a wallet (the balance lives in it). */
    public static boolean hasWallet(ServerPlayer player) {
        return player != null && Data.hasWallet(player);
    }

    /** Adds wallet coins; returns how many were added (0 without a wallet or when the wallet module is off). */
    public static long give(ServerPlayer player, long amount) {
        return give(player, Data.VIRTUAL_ID, amount);
    }

    /** Adds the value of {@code amount} of a coin (or wallet coins); returns how many of them were added. */
    public static long give(ServerPlayer player, String currency, long amount) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || amount <= 0L) return 0L;
        String id = resolve(currency);
        if (id.isEmpty()) return 0L;
        long added = Data.add(player, id, amount);
        if (added > 0L) Network.sync(player);
        return added;
    }

    /** Takes wallet coins; returns how many were taken. */
    public static long take(ServerPlayer player, long amount) {
        return take(player, Data.VIRTUAL_ID, amount);
    }

    /** Takes the value of up to {@code amount} of a coin (or wallet coins); returns how many of them were taken. */
    public static long take(ServerPlayer player, String currency, long amount) {
        if (!CuriosConfig.enableCurrencyWallet || player == null || amount <= 0L) return 0L;
        String id = resolve(currency);
        if (id.isEmpty()) return 0L;
        long taken = Data.extractDirect(player, id, amount, false);
        if (taken > 0L) Network.sync(player);
        return taken;
    }

    /** The player's balance in wallet coins. */
    public static long balance(ServerPlayer player) {
        return Data.countDirect(player, Data.VIRTUAL_ID);
    }
}
