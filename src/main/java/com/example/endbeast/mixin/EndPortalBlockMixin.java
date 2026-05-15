package com.example.endbeast.mixin;

import com.example.endbeast.PortalActivation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void endbeast$onEntityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean unused, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        if (entity instanceof ItemEntity itemEntity) {
            if (!PortalActivation.isActivated(serverLevel)) {
                PortalActivation.tryConsume(serverLevel, itemEntity);
                ci.cancel();
            }
            return;
        }

        if (!PortalActivation.isActivated(serverLevel)) {
            if (entity instanceof ServerPlayer player) {
                PortalActivation.repelPlayer(player, pos);
            }
            ci.cancel();
        }
    }
}
