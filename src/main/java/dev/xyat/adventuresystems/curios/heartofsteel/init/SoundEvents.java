package dev.xyat.adventuresystems.curios.heartofsteel.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundEvents {
    // 1. 定义注册器
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CuriosModule.MODID);

    // 2. 注册具体的音效对象
    public static final RegistryObject<SoundEvent> HEART_OF_STEEL = registerSound();

    // 辅助注册方法（改个名字防止和下面的方法重名冲突）
    private static RegistryObject<SoundEvent> registerSound() {
        return SOUND_EVENTS.register("heart_of_steel", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CuriosModule.MODID, "heart_of_steel")));
    }

    // 3. 【核心修复】加上这个方法！
    // 你的主类扫描器 scanAndLoadModules 会自动找到这个方法并执行，从而完美挂载音效！
    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
