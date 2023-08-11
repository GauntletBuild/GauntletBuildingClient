package net.gauntletmc.mod.barriers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BarrierInformation {

    private static final Map<BlockPos, BlockState> BARRIERS = new ConcurrentHashMap<>();

    public static void clear() {
        BARRIERS.clear();
    }

    public static void remove(BlockPos pos) {
        BARRIERS.remove(pos);
    }

    public static void add(BlockState state, BlockPos pos) {
        if (BarrierHandler.isBarrier(state)) {
            BARRIERS.put(pos.immutable(), state);
        }
    }

    public static Map<BlockPos, BlockState> get() {
        return BARRIERS;
    }
}
