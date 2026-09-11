package dev.xyat.adventuresystems.tips.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;

public final class TipsStructureUtil {
    private TipsStructureUtil() {
    }

    public static List<String> getStructuresAt(ServerLevel level, BlockPos pos) {
        List<String> structureIds = new ArrayList<>();
        if (level == null || pos == null) {
            return structureIds;
        }

        List<StructureStart> structureStarts = level.structureManager().startsForStructure(new ChunkPos(pos), structure -> true);

        for (StructureStart start : structureStarts) {
            if (!start.isValid() || !start.getBoundingBox().isInside(pos)) {
                continue;
            }

            boolean isInsidePiece = false;
            for (StructurePiece piece : start.getPieces()) {
                if (piece.getBoundingBox().isInside(pos)) {
                    isInsidePiece = true;
                    break;
                }
            }

            if (!isInsidePiece) {
                continue;
            }

            Structure structure = start.getStructure();
            ResourceLocation key = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure);
            if (key != null) {
                structureIds.add(key.toString());
            }
        }

        return structureIds;
    }
}
