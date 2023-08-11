package net.gauntletmc.mod.mixins;

import net.gauntletmc.mod.barriers.BarrierInformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Inject(
            method = "animateTick",
            at = @At("HEAD")
    )
    private void gauntlet$animateTick(int i, int j, int k, CallbackInfo ci){
        if (System.currentTimeMillis() / 1000 % 3 == 0) {
            List<BlockPos> removals = new ArrayList<>();
            for (var entry : BarrierInformation.get().entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                if (getBlockState(pos) != state) {
                    removals.add(pos);
                }
            }
            removals.forEach(BarrierInformation::remove);
        }
    }

    @Inject(
            method = "doAnimateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void gauntlet$doAnimateTick(int i, int j, int k, int l, RandomSource randomSource, Block block, BlockPos.MutableBlockPos mutableBlockPos, CallbackInfo ci) {
        BlockState state = getBlockState(mutableBlockPos);
        BarrierInformation.add(state, mutableBlockPos);
    }
}
