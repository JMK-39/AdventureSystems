package dev.xyat.adventuresystems.curios.wallet.compat.ftb;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.event.WalletModule;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.ftb.api.FTBVirtualItemProvider;
import dev.xyat.adventuresystems.ftb.api.FTBVirtualItemProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FTBCompat {
    private FTBCompat() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> FTBVirtualItemProviders.register(new WalletProvider()));
    }

    private static final class WalletProvider implements FTBVirtualItemProvider {
        private static final ResourceLocation ID = new ResourceLocation(CuriosModule.MODID, "currency_wallet");

        @Override
        public ResourceLocation id() {
            return ID;
        }

        @Override
        public boolean matches(ServerPlayer player, ItemStack filterStack) {
            if (player == null || filterStack == null || filterStack.isEmpty()) return false;
            if (!CuriosConfig.enableCurrencyWallet) return false;
            if (!WalletModule.hasWallet(player)) return false;
            return Data.isCurrencyItem(filterStack);
        }

        @Override
        public long count(ServerPlayer player, ItemStack filterStack) {
            if (!matches(player, filterStack)) return 0L;
            String id = Data.currencyId(filterStack);
            if (id.isEmpty()) return 0L;
            return Data.countForAutomaticPayment(player, id);
        }

        @Override
        public long extract(ServerPlayer player, ItemStack filterStack, long amount, boolean simulate) {
            if (!matches(player, filterStack) || amount <= 0L) return 0L;

            String id = Data.currencyId(filterStack);
            if (id.isEmpty()) return 0L;

            long available = Data.countForAutomaticPayment(player, id);
            long extracted = Math.min(available, amount);
            if (extracted <= 0L) return 0L;

            if (simulate) return extracted;

            return Data.extractForAutomaticPayment(player, id, extracted, false);
        }

        @Override
        public void sync(ServerPlayer player) {
            if (player != null) {
                Network.sync(player);
            }
        }
    }
}

