package dev.xyat.adventuresystems.curios.paradiselost.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ParadiseLostSavedData extends SavedData {
    // 内存缓存：PlayerUUID -> PlayerData(score, eatenFoods)
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * 存储每个玩家的数据
     */
    public static class PlayerData {
        public int score = 0;
        public final Set<String> eatenFoods = new HashSet<>();

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("score", score);
            ListTag list = new ListTag();
            eatenFoods.forEach(f -> list.add(StringTag.valueOf(f)));
            tag.put("eatenFoods", list);
            return tag;
        }

        public static PlayerData load(CompoundTag tag) {
            PlayerData data = new PlayerData();
            data.score = tag.getInt("score");
            ListTag list = tag.getList("eatenFoods", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                data.eatenFoods.add(list.getString(i));
            }
            return data;
        }
    }

    /**
     * 获取单例实例。
     * 通过 server.overworld() 来确保数据在所有维度中共享。
     */
    public static ParadiseLostSavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new RuntimeException("Client side access to SavedData!");
        }
        return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(
                ParadiseLostSavedData::load,
                ParadiseLostSavedData::new,
                "adventuresystems_paradise_lost_data"
        );
    }

    private PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData());
    }

    public boolean hasEaten(UUID playerId, String foodId) {
        return getPlayerData(playerId).eatenFoods.contains(foodId);
    }

    /**
     * 为玩家添加食物记录和营养点数
     */
    public void addFood(UUID playerId, String foodId, int nutrition) {
        PlayerData data = getPlayerData(playerId);
        // 只有在食物是新的情况下才添加
        if (data.eatenFoods.add(foodId)) {
            data.score += nutrition;
            this.setDirty(); // 标记需要存盘
        }
    }

    public int getScore(UUID playerId) {
        return getPlayerData(playerId).score;
    }

    public ParadiseLostSavedData() {}

    /**
     * 从 NBT 加载数据
     */
    public static ParadiseLostSavedData load(CompoundTag nbt) {
        ParadiseLostSavedData data = new ParadiseLostSavedData();
        CompoundTag maps = nbt.getCompound("PlayerData");
        for (String key : maps.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            CompoundTag playerDataTag = maps.getCompound(key);
            data.playerDataMap.put(playerId, PlayerData.load(playerDataTag));
        }
        return data;
    }

    /**
     * 保存数据到 NBT
     */
    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        CompoundTag maps = new CompoundTag();
        playerDataMap.forEach((uuid, playerData) -> maps.put(uuid.toString(), playerData.save()));
        nbt.put("PlayerData", maps);
        return nbt;
    }
}
