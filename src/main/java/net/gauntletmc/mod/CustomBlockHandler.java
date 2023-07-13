package net.gauntletmc.mod;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CustomBlockHandler {

    private static final Map<ObjectIntPair<ResourceLocation>, List<BlockState>> REGISTRY = new HashMap<>();
    private static final Map<BlockState, ItemStack> BLOCKS = new HashMap<>();
    private static final Map<BlockState, ResourceLocation> IDS = new HashMap<>();
    private static final Object2IntMap<BlockState> INDEXES = new Object2IntOpenHashMap<>();

    public static void update(Map<ObjectIntPair<ResourceLocation>, List<BlockState>> blocks) {
        REGISTRY.clear();
        REGISTRY.putAll(blocks);

        BLOCKS.clear();
        for (var entry : REGISTRY.entrySet()) {
            ItemStack stack = null;
            ResourceLocation id = entry.getKey().key();
            int index = entry.getKey().valueInt();
            for (var state : entry.getValue()) {
                if (stack == null) {
                    stack = new ItemStack(state.getBlock());
                    stack.setHoverName(Component.translatable(id.toLanguageKey("item")));
                }
                BLOCKS.put(state, stack);
                IDS.put(state, id);
                INDEXES.put(state, index);
            }
        }
    }

    public static void clear() {
        BLOCKS.clear();
    }

    public static ItemStack get(BlockState state) {
        return BLOCKS.getOrDefault(state, ItemStack.EMPTY);
    }

    public static ResourceLocation getID(BlockState state) {
        return IDS.get(state);
    }

    public static int getIndex(BlockState state) {
        return INDEXES.getInt(state);
    }

    public static Collection<ItemStack> getItems() {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndTagSet();
        set.addAll(BLOCKS.values());
        set.removeIf(ItemStack::isEmpty);
        return set;
    }
}
