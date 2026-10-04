package dev.xyat.adventuresystems.curios.common.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * The entity's Curios inventory. Curios for 1.20.1 reaches it through its helper; since 1.21 the helper is gone
 * and the inventory is looked up directly.
 */
public final class CuriosAccess {
    private CuriosAccess() {
    }

    /** The first equipped curio of the item, if any. */
    public static Optional<SlotResult> findFirst(LivingEntity entity, Item item) {
        //? if >=1.21 {
        /*return CuriosApi.getCuriosInventory(entity).flatMap(inventory -> inventory.findFirstCurio(item));
        *///?} else {
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, item);
        //?}
    }

    /** The first equipped curio matching the filter, if any. */
    public static Optional<SlotResult> findFirst(LivingEntity entity, Predicate<ItemStack> filter) {
        //? if >=1.21 {
        /*return CuriosApi.getCuriosInventory(entity).flatMap(inventory -> inventory.findFirstCurio(filter));
        *///?} else {
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, filter);
        //?}
    }

    /** The entity's Curios inventory, if it has one. */
    public static Optional<ICuriosItemHandler> inventory(LivingEntity entity) {
        //? if >=1.21 {
        /*return CuriosApi.getCuriosInventory(entity);
        *///?} else {
        return CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve();
        //?}
    }
}
