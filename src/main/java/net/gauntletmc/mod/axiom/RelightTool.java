package net.gauntletmc.mod.axiom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.axiomclientapi.CustomTool;
import com.moulberry.axiomclientapi.Effects;
import com.moulberry.axiomclientapi.pathers.BallShape;
import com.moulberry.axiomclientapi.pathers.ToolPatherUnique;
import com.moulberry.axiomclientapi.regions.BooleanRegion;
import com.moulberry.axiomclientapi.service.ToolService;
import net.gauntletmc.mod.network.ServerboundRelightChunks;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class RelightTool implements CustomTool {

    private final BooleanRegion region = ServiceHelper.createBooleanRegion();
    private ToolPatherUnique pather = null;
    private boolean usingTool = false;

    private boolean drewPreview = false;

    @Override
    public void reset() {
        this.region.clear();
        this.usingTool = false;
        this.pather = null;
        this.drewPreview = false;
    }

    @Override
    public String name() {
        return "Relight";
    }

    @Override
    public boolean callUseTool() {
        this.reset();
        this.usingTool = true;
        this.pather = ServiceHelper.createToolPatherUnique(1, BallShape.CUBE);
        return true;
    }

    @Override
    public void render(Camera camera, float tickDelta, long time, PoseStack poseStack, Matrix4f projection) {
        ToolService toolService = ServiceHelper.getToolService();

        if (!this.usingTool) {
            BlockHitResult hitResult = toolService.raycastBlock();
            if (hitResult == null) return;

            if (!drewPreview) {
                drewPreview = true;
                this.addChunkToRegion(newSectionPos(0, 0, 0));
            }
            SectionPos pos = SectionPos.of(hitResult.getBlockPos());
            Vec3 vec3 = new Vec3(pos.minBlockX(), pos.minBlockY(), pos.minBlockZ());

            this.region.render(camera, vec3, poseStack, projection, time, Effects.BLUE);
        } else if (!ServiceHelper.getToolService().isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            Set<SectionPos> sections = new HashSet<>();
            this.region.forEach((x, y, z) -> sections.add(newSectionPos(x, y, z)));
            if (!sections.isEmpty()) {
                ServerboundRelightChunks.send(sections);
            }
            this.reset();
        } else {
            Set<SectionPos> sections = new HashSet<>();
            this.pather.update((x, y, z) -> sections.add(newSectionPos(x, y, z)));
            sections.forEach(this::addChunkToRegion);
            this.region.render(camera, Vec3.ZERO, poseStack, projection, time, Effects.BLUE);
        }
    }

    private void addChunkToRegion(SectionPos section) {
        for (BlockPos pos : BlockPos.betweenClosed(section.minBlockX(), section.minBlockY(), section.minBlockZ(), section.maxBlockX(), section.maxBlockY(), section.maxBlockZ())) {
            this.region.add(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static SectionPos newSectionPos(int x, int y, int z) {
        x = SectionPos.blockToSectionCoord(x);
        y = SectionPos.blockToSectionCoord(y);
        z = SectionPos.blockToSectionCoord(z);
        return SectionPos.of(x, y, z);
    }
}
