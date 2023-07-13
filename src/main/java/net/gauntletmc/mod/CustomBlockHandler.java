package net.gauntletmc.mod;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CustomBlockHandler {

    private static final Map<ResourceLocation, List<BlockState>> REGISTRY = new HashMap<>();
    private static final Map<BlockState, ItemStack> BLOCKS = new HashMap<>();
    private static final Map<BlockState, ResourceLocation> IDS = new HashMap<>();
    private static final Object2IntMap<BlockState> INDEXES = new Object2IntOpenHashMap<>();

    public static void update(Map<ObjectIntPair<ResourceLocation>, List<BlockState>> blocks) {
        REGISTRY.clear();

        BLOCKS.clear();
        IDS.clear();
        INDEXES.clear();
        for (var entry : blocks.entrySet()) {
            ItemStack stack = null;
            ResourceLocation id = entry.getKey().key();
            int index = entry.getKey().valueInt();
            REGISTRY.put(id, entry.getValue());
            for (var state : entry.getValue()) {
                if (stack == null) {
                    stack = new ItemStack(state.getBlock());
                    stack.setHoverName(Component.translatable(id.toLanguageKey("item")).withStyle(Style.EMPTY.withItalic(false)));

                    CompoundTag tag = stack.getOrCreateTag();
                    CompoundTag bukkitValues = tag.getCompound("PublicBukkitValues");
                    bukkitValues.putString("gauntlet:item", id.toString());
                    tag.put("PublicBukkitValues", bukkitValues);
                    tag.putInt("CustomModelData", CustomBlockHandler.getIndex(state));
                    stack.setTag(tag);
                }
                BLOCKS.put(state, stack);
                IDS.put(state, id);
                INDEXES.put(state, index);
            }
        }
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

    public static List<BlockState> getStates(ResourceLocation id) {
        return REGISTRY.getOrDefault(id, List.of());
    }

    public static Collection<ItemStack> getItems() {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndTagSet();
        List<ObjectIntPair<BlockState>> list = new ArrayList<>();
        for (var entry : INDEXES.object2IntEntrySet()) {
            list.add(ObjectIntPair.of(entry.getKey(), entry.getIntValue()));
        }
        list.sort(Comparator.comparingInt(ObjectIntPair::valueInt));
        for (var entry : list) {
            ItemStack stack = BLOCKS.get(entry.key());
            if (stack != null && !stack.isEmpty()) {
                set.add(stack);
            }
        }
        return set;
    }
}
