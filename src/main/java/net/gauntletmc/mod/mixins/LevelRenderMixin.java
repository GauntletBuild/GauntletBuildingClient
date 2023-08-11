package net.gauntletmc.mod.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.gauntletmc.mod.barriers.BarrierHandler;
import net.gauntletmc.mod.barriers.BarrierInformation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRenderMixin {

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow
    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        throw new AssertionError();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void gauntlet$renderBarrier(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
        MultiBufferSource.BufferSource source = this.renderBuffers.bufferSource();
        ItemStack stack = Minecraft.getInstance().player == null ? ItemStack.EMPTY : Minecraft.getInstance().player.getMainHandItem();
        String group = BarrierHandler.getGroup(stack.getItem());
        if (group == null) return;
        for (var entry : BarrierInformation.get().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            BarrierHandler.Information info = BarrierHandler.getInformation(state);
            if (!info.group().equals(group)) continue;
            VoxelShape shape = state.getShape(Minecraft.getInstance().level, pos).move(pos.getX(), pos.getY(), pos.getZ());
            var position = camera.getPosition();
            float red = info.color().getFloatRed();
            float green = info.color().getFloatGreen();
            float blue = info.color().getFloatBlue();
            for (AABB aabb : shape.toAabbs()) {
                renderShape(poseStack, source.getBuffer(RenderType.debugLineStrip(1.25f)), Shapes.create(aabb), -position.x, -position.y, -position.z, red, green, blue, 1f);
            }
        }
        source.endBatch(RenderType.debugLineStrip(1.25f));
        RenderSystem.enableDepthTest();
    }
}
