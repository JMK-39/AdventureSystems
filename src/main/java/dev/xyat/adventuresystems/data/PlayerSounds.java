package dev.xyat.adventuresystems.data;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/** Sounds only the player hears. 26.1 dropped Player#playNotifySound, so the sound packet goes to that player. */
public final class PlayerSounds {
    private PlayerSounds() {
    }

    public static void notify(Player player, SoundEvent sound, SoundSource source, float volume, float pitch) {
        //? if >=26.1 {
        /*if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source,
                    player.getX(), player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
        }
        *///?} else {
        player.playNotifySound(sound, source, volume, pitch);
        //?}
    }
}
