package net.gauntletmc.mod.handlers;

import com.google.gson.JsonArray;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IgnoredBlocksListener extends SimplePreparableReloadListener<List<ResourceLocation>> implements IdentifiableResourceReloadListener {

    public static final IgnoredBlocksListener INSTANCE = new IgnoredBlocksListener();

    private final List<ResourceLocation> ignoredBlocks = new ArrayList<>();

    @Override
    protected @NotNull List<ResourceLocation> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<ResourceLocation> ignoredBlocks = new ArrayList<>();
        for (String namespace : resourceManager.getNamespaces()) {
            List<Resource> list = resourceManager.getResourceStack(new ResourceLocation(namespace, "gauntlet/no_dynamic_items.json"));
            for (Resource resource : list) {
                try {
                    JsonArray array = GsonHelper.parseArray(resource.openAsReader());
                    for (int i = 0; i < array.size(); i++) {
                        ResourceLocation id = ResourceLocation.tryParse(array.get(i).getAsString());
                        if (id != null) {
                            ignoredBlocks.add(id);
                        } else {
                            System.out.println("Failed to parse ignored block: " + array.get(i).getAsString());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ignoredBlocks;
    }

    @Override
    protected void apply(List<ResourceLocation> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.ignoredBlocks.clear();
        this.ignoredBlocks.addAll(object);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("gauntlet", "ignored_blocks");
    }

    public static boolean isIgnored(ResourceLocation id) {
        return INSTANCE.ignoredBlocks.contains(id);
    }
}
