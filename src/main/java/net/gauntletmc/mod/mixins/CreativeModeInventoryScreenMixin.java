package net.gauntletmc.mod.mixins;

import net.gauntletmc.mod.GauntletClient;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {

    @Shadow
    protected abstract boolean hasPermissions(Player player);

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void gauntlet$onInit(Player player, FeatureFlagSet flags, boolean hasPermissions, CallbackInfo ci) {
        Stream.of(GauntletClient.CREATIVE_TAB, GauntletClient.CREATIVE_TAB_DECORATIONS, GauntletClient.CREATIVE_TAB_ITEMS)
                .map(BuiltInRegistries.CREATIVE_MODE_TAB::getOptional)
                .flatMap(Optional::stream)
                .forEach(tab -> tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                        flags, this.hasPermissions(player), player.level().registryAccess()
                )));
    }

}
