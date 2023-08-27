package net.gauntletmc.mod.barriers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.client.highlights.state.StateVariant;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.color.ConstantColors;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BarrierHandler extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

    private static final Information DEFAULT = new Information(ConstantColors.red, "barrier");

    private static final Map<BlockState, Information> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<Item, String> GROUPS = new ConcurrentHashMap<>();

    private static boolean defaultToggled = false;

    public BarrierHandler() {
        super(new Gson(), "gauntlet/barriers");
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("gauntlet", "barriers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        BarrierInformation.clear();
        BLOCKS.clear();

        object.forEach((key, value) -> BuiltInRegistries.BLOCK.getOptional(key)
            .flatMap(block -> codec(block).parse(JsonOps.INSTANCE, value).result())
            .ifPresent(values -> {
                for (List<BlockState> states : values.getFirst()) {
                    for (BlockState state : states) {
                        BLOCKS.put(state, values.getSecond());
                        GROUPS.put(state.getBlock().asItem(), values.getSecond().group());
                    }
                }
            })
        );
    }

    public static boolean isBarrier(BlockState state) {
        return BLOCKS.containsKey(state);
    }

    public static Information getInformation(BlockState state) {
        return BLOCKS.getOrDefault(state, DEFAULT);
    }

    public static String getGroup(Item item) {
        return GROUPS.getOrDefault(item, BarrierHandler.defaultToggled ? DEFAULT.group : null);
    }

    public static void toggle() {
        BarrierHandler.defaultToggled = !BarrierHandler.defaultToggled;
    }

    private static Codec<Pair<List<List<BlockState>>, Information>> codec(Block block) {
        return RecordCodecBuilder.create(instance -> instance.group(
            StateVariant.stateCodec(block).listOf().fieldOf("states").forGetter(Pair::getFirst),
            Color.CODEC.fieldOf("color").forGetter(pair -> pair.getSecond().color()),
            Codec.STRING.fieldOf("group").orElse("barrier").forGetter(pair -> pair.getSecond().group())
        ).apply(instance, (states, color, group) -> new Pair<>(states, new Information(color, group))));
    }

    public record Information(Color color, String group){}
}
