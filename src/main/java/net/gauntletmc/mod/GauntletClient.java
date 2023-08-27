package net.gauntletmc.mod;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamresourceful.resourcefullib.common.color.Color;
import it.unimi.dsi.fastutil.bytes.ByteObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.gauntletmc.mod.barriers.BarrierHandler;
import net.gauntletmc.mod.network.ClientboundOpenScreen;
import net.gauntletmc.mod.network.ServerboundRequestScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("NoTranslation")
public class GauntletClient implements ClientModInitializer {

    public static final ResourceLocation CREATIVE_TAB = new ResourceLocation("gauntletmod", "gauntlet_blocks");
    public static final ResourceLocation CREATIVE_TAB_DECORATIONS = new ResourceLocation("gauntletmod", "gauntlet_decorations");
    public static final ResourceLocation CREATIVE_TAB_ITEMS = new ResourceLocation("gauntletmod", "gauntlet_items");

    public static final KeyMapping OPEN_NOTES = new KeyMapping("Open Notes (Gauntlet)", InputConstants.KEY_N, "key.categories.misc");
    public static final KeyMapping TOGGLE_BARRIERS = new KeyMapping("Toggle Barriers", InputConstants.KEY_PERIOD, "key.categories.misc");

    @Override
    public void onInitializeClient() {
        Color.initRainbow();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new BarrierHandler());

        ClientPlayNetworking.registerGlobalReceiver(
                new ResourceLocation("gauntletblocks:blocks"),
                (client, handler, buffer, response) -> {
                    int size = buffer.readVarInt();
                    Map<ObjectIntPair<ResourceLocation>, List<ByteObjectPair<BlockState>>> blocks = new HashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        ResourceLocation id = buffer.readResourceLocation();
                        int index = buffer.readVarInt();
                        blocks.put(ObjectIntPair.of(id, index), buffer.readList((buf) ->
                                ByteObjectPair.of(buf.readByte(), buf.readById(Block.BLOCK_STATE_REGISTRY)))
                        );
                    }
                    CustomBlockHandler.update(blocks);
                }
        );
        ClientboundOpenScreen.init();

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
            if (stack.hasTag() && tag != null) {
                String id = tag.getString("gauntlet:item");
                if (!id.isBlank()) {
                    if(context.isAdvanced()) {
                        Component title = lines.get(0);
                        lines.clear();
                        lines.add(title);
                        lines.add(Component.literal(id).withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        lines.set(0, lines.get(0).copy().setStyle(Style.EMPTY));
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register((c) -> {
            if (OPEN_NOTES.consumeClick()) {
                ServerboundRequestScreen.send(Minecraft.getInstance().getUser().getProfileId());
            }
            if (TOGGLE_BARRIERS.consumeClick()) {
                BarrierHandler.toggle();
            }
        });

        final Predicate<Item> isNoteBlock = Items.NOTE_BLOCK::equals;
        final Predicate<Item> isRedstone = Items.REDSTONE::equals;

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB,
                FabricItemGroup.builder()
                    .icon(Items.NOTE_BLOCK::getDefaultInstance)
                    .title(Component.literal("Gauntlet Blocks"))
                    .displayItems((params, output) -> output.acceptAll(CustomBlockHandler.getItems(isNoteBlock)))
                    .build()
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB_DECORATIONS,
                FabricItemGroup.builder()
                        .icon(Items.REDSTONE::getDefaultInstance)
                        .title(Component.literal("Gauntlet Decorations"))
                        .displayItems((params, output) -> output.acceptAll(CustomBlockHandler.getItems(isRedstone)))
                        .build()
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                CREATIVE_TAB_ITEMS,
                FabricItemGroup.builder()
                        .icon(Items.BEDROCK::getDefaultInstance)
                        .title(Component.literal("Gauntlet Items"))
                        .displayItems((params, output) -> output.acceptAll(CustomBlockHandler.getItems(Predicate.not(isNoteBlock.or(isRedstone)))))
                        .build()
        );

        KeyBindingHelper.registerKeyBinding(OPEN_NOTES);
        KeyBindingHelper.registerKeyBinding(TOGGLE_BARRIERS);
    }
}
