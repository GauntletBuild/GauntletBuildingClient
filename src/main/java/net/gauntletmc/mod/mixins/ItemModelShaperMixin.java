package net.gauntletmc.mod.mixins;

import net.gauntletmc.mod.CustomBlockHandler;
import net.gauntletmc.mod.GauntletClient;
import net.gauntletmc.mod.handlers.IgnoredBlocksListener;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemModelShaper.class)
public class ItemModelShaperMixin {

    @Shadow
    @Final
    private ModelManager modelManager;

    @Inject(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    public void onModelGet(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        if (!tag.contains("gauntlet:item")) return;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("gauntlet:item"));
        if (IgnoredBlocksListener.isIgnored(id)) return;
        List<BlockState> states = CustomBlockHandler.getStates(id);
        if (states == null || states.isEmpty()) return;
        BakedModel model = this.modelManager.getBlockModelShaper().getBlockModel(states.get(0));
        if (model != this.modelManager.getMissingModel()) {
            cir.setReturnValue(model);
        }
    }
}