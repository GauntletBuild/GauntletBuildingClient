package net.gauntletmc.mod;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GauntletClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                new ResourceLocation("gauntletblocks:blocks"),
                (client, handler, buffer, response) -> {
                    int size = buffer.readVarInt();
                    Map<ObjectIntPair<ResourceLocation>, List<BlockState>> blocks = new HashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        ResourceLocation id = buffer.readResourceLocation();
                        int index = buffer.readVarInt();
                        blocks.put(ObjectIntPair.of(id, index), buffer.readList((buf) -> buf.readById(Block.BLOCK_STATE_REGISTRY)));
                    }
                    CustomBlockHandler.update(blocks);
                }
        );

        ClientPickBlockApplyCallback.EVENT.register((player, result, stack) -> {
            if (result instanceof BlockHitResult blockResult) {
                BlockState state = player.level().getBlockState(blockResult.getBlockPos());
                ItemStack customBlockStack = CustomBlockHandler.get(state);
                ResourceLocation id = CustomBlockHandler.getID(state);
                if (!customBlockStack.isEmpty() && id != null) {
                    CompoundTag tag = customBlockStack.getOrCreateTag();
                    CompoundTag bukkitValues = tag.getCompound("PublicBukkitValues");
                    bukkitValues.putString("gauntlet:item", id.toString());
                    tag.put("PublicBukkitValues", bukkitValues);
                    tag.putInt("CustomModelData", CustomBlockHandler.getIndex(state));
                    customBlockStack.setTag(tag);
                    return customBlockStack;
                }
            }
            return stack;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
            CustomBlockHandler.clear()
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation("gauntletmod", "gauntlet_blocks"),
                FabricItemGroup.builder()
                    .icon(Items.NOTE_BLOCK::getDefaultInstance)
                    .title(Component.literal("Gauntlet Blocks"))
                    .displayItems((params, output) ->
                            output.acceptAll(CustomBlockHandler.getItems())
                    ).build()
        );

    }
}
