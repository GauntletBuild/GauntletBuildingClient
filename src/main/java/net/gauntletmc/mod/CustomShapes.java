package net.gauntletmc.mod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CustomShapes {

    public static final VoxelShape FULL = Block.box(0, 0, 0, 16, 16, 16);
    public static final VoxelShape FLAT_NORTH = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape FLAT_SOUTH = Block.box(0, 0, 15, 16, 16, 16);
    public static final VoxelShape FLAT_WEST = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape FLAT_EAST = Block.box(15, 0, 0, 16, 16, 16);
    public static final VoxelShape FLAT_UP = Block.box(0, 15, 0, 16, 16, 16);
    public static final VoxelShape FLAT_DOWN = Block.box(0, 0, 0, 16, 1, 16);
    public static final VoxelShape MINI_NORTH = Block.box(4, 4, 0, 12, 12, 4);
    public static final VoxelShape MINI_SOUTH = Block.box(4, 4, 12, 12, 12, 16);
    public static final VoxelShape MINI_WEST = Block.box(0, 4, 4, 4, 12, 12);
    public static final VoxelShape MINI_EAST = Block.box(12, 4, 4, 16, 12, 12);
    public static final VoxelShape MINI_UP = Block.box(4, 12, 4, 12, 16, 12);
    public static final VoxelShape MINI_DOWN = Block.box(4, 0, 4, 12, 4, 12);

    public static VoxelShape fromByte(byte id) {
        return switch (id) {
            case 1 -> FLAT_NORTH;
            case 2 -> FLAT_SOUTH;
            case 3 -> FLAT_WEST;
            case 4 -> FLAT_EAST;
            case 5 -> FLAT_UP;
            case 6 -> FLAT_DOWN;
            case 7 -> MINI_NORTH;
            case 8 -> MINI_SOUTH;
            case 9 -> MINI_WEST;
            case 10 -> MINI_EAST;
            case 11 -> MINI_UP;
            case 12 -> MINI_DOWN;
            default -> FULL;
        };
    }
}
