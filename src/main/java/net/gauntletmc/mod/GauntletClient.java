package net.gauntletmc.mod;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

    public static final ResourceLocation CREATIVE_TAB = new ResourceLocation("gauntletmod", "gauntlet_blocks");

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
                    return customBlockStack;
                }
            }
            return stack;
        });

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("PublicBukkitValues")) {
                CompoundTag bukkitValues = tag.getCompound("PublicBukkitValues");
                if (bukkitValues.contains("gauntlet:item")) {
                    if(context.isAdvanced()) {
                        Component title = lines.get(0);
                        lines.clear();
                        lines.add(title);
                        lines.add(Component.literal(bukkitValues.getString("gauntlet:item")).withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        lines.set(0, lines.get(0).copy().setStyle(Style.EMPTY));
                    }
                }
            }
        });



        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB,
                FabricItemGroup.builder()
                    .icon(Items.NOTE_BLOCK::getDefaultInstance)
                    .title(Component.literal("Gauntlet Blocks"))
                    .displayItems((params, output) -> output.acceptAll(CustomBlockHandler.getItems()))
                    .build()
        );

    }
}
