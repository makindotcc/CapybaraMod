package gg.capybara.mod.mixin.optimization.world;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Initialize block state cache lazy off renderer thread.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LazyBlockBehaviour {
    private boolean lazyInitCacheFired;
    private boolean invalidateCache = false;

    @Shadow public abstract void initCache();

    @Inject(method = "initCache", at = @At("HEAD"), cancellable = true)
    public void initCache(CallbackInfo ci) {
        if (this.lazyInitCacheFired) {
            this.lazyInitCacheFired = false;
        } else {
            ci.cancel();
            this.invalidateCache = false;
        }
    }

    private void forceInitCache() {
        if (!this.invalidateCache && !RenderSystem.isOnRenderThread()) {
            this.invalidateCache = true;
            this.lazyInitCacheFired = true;
            this.initCache();
        }
    }

    @Inject(method = "isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;" +
        "Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/SupportType;)Z",
        at = @At("HEAD"))
    public void initCache(BlockGetter blockGetter, BlockPos blockPos, Direction direction, SupportType supportType,
        CallbackInfoReturnable<Boolean> cir) {
        this.forceInitCache();
    }

    @Inject(method = "isCollisionShapeFullBlock", at = @At("HEAD"))
    public void initCache(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        this.forceInitCache();
    }
}
