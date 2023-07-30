package net.gauntletmc.mod.mixins;

import net.gauntletmc.mod.CustomBlockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {RedStoneWireBlock.class})
public class CustomShapesMixin {

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void replaceShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        VoxelShape shape = CustomBlockHandler.getShape(blockState);
        if (shape != null) {
            cir.setReturnValue(shape);
        }
    }
}
