//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.common.item;

import javax.annotation.Nonnull;

import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisabledCurioItem extends Item {
    public DisabledCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, @Nonnull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.disabled"));
    }
}

*///?} else {
package dev.xyat.adventuresystems.curios.common.item;

import javax.annotation.Nonnull;

import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisabledCurioItem extends Item {
    public DisabledCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.disabled"));
    }
}

//?}
