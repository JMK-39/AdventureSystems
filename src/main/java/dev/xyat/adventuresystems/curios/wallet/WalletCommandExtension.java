package dev.xyat.adventuresystems.curios.wallet;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.command.CommandExtension;
import dev.xyat.kineticcore.api.command.KineticCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

/**
 * {@code /kt wallet give|take <players> <amount> [currency]} and {@code /kt wallet balance <player>}: wallet coins for
 * FTB quest command rewards, scripts and operators. Without a currency the amount is in wallet coins; with a coin item
 * it is worth that coin's value.
 */
public final class WalletCommandExtension implements CommandExtension {
    private WalletCommandExtension() {
    }

    public static void install() {
        KineticCommands.registerExtension(CuriosModule.MODID + "_wallet", new WalletCommandExtension());
    }

    @Override
    public void registerCommands(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("wallet").requires(source -> source.hasPermission(2))
                .then(change("give", true))
                .then(change("take", false))
                .then(Commands.literal("balance").then(Commands.argument("player", EntityArgument.player())
                        .executes(WalletCommandExtension::balance))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> change(String name, boolean give) {
        return Commands.literal(name).then(Commands.argument("players", EntityArgument.players())
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(context -> change(context, give, Data.VIRTUAL_ID))
                        .then(Commands.argument("currency", StringArgumentType.greedyString())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(WalletApi.currencies(), builder))
                                .executes(context -> change(context, give, StringArgumentType.getString(context, "currency"))))));
    }

    private static int change(CommandContext<CommandSourceStack> context, boolean give, String currency) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        long amount = LongArgumentType.getLong(context, "amount");
        String id = WalletApi.resolve(currency);
        if (id.isEmpty()) {
            context.getSource().sendFailure(AdventureText.translatable("command.adventuresystems.wallet.unknown_currency", currency));
            return 0;
        }
        int changed = 0;
        for (ServerPlayer player : players) {
            long moved = give ? WalletApi.give(player, id, amount) : WalletApi.take(player, id, amount);
            if (moved > 0L) changed++;
        }
        int count = changed;
        long value = Data.unitValue(id);
        long coins = amount > Long.MAX_VALUE / value ? Long.MAX_VALUE : amount * value;
        context.getSource().sendSuccess(() -> AdventureText.translatable(give
                ? "command.adventuresystems.wallet.given" : "command.adventuresystems.wallet.taken", coins, count), true);
        return changed;
    }

    private static int balance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        context.getSource().sendSuccess(() -> AdventureText.translatable("command.adventuresystems.wallet.balance",
                player.getName().getString(), WalletApi.balance(player)), false);
        return 1;
    }

    @Override
    public void appendHelpItems(CommandSourceStack source, List<MutableComponent> items) {
        if (source.hasPermission(2)) items.add(AdventureText.translatable("command.adventuresystems.wallet.help"));
    }
}
