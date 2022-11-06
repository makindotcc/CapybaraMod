package gg.capybara.mod.mixin.optimization.world.level.block.state;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Initialize face sturdy cache lazy.
 */
@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
public abstract class LazyBlockBehaviourStateCache {
    @Shadow
    @Final
    private boolean[] faceSturdy;

    private Runnable initializeFaceSturdy = null;

    @Shadow
    private static int getFaceSupportIndex(Direction direction, SupportType supportType) {
        return 0;
    }

    @Shadow
    @Final
    private static Direction[] DIRECTIONS;

    @Overwrite
    public boolean isFaceSturdy(Direction direction, SupportType supportType) {
        if (initializeFaceSturdy != null) {
            initializeFaceSturdy.run();
            initializeFaceSturdy = null;
        }
        return this.faceSturdy[getFaceSupportIndex(direction, supportType)];
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void ctor(BlockState blockState, CallbackInfo ci) {
        this.initializeFaceSturdy = () -> {
            for (Direction direction1 : DIRECTIONS) {
                for (SupportType supporttype : SupportType.values()) {
                    this.faceSturdy[getFaceSupportIndex(direction1, supporttype)] =
                        supporttype.isSupporting(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction1);
                }
            }
        };
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/SupportType.isSupporting (Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean isSupportingMock(SupportType instance, BlockState blockState, BlockGetter blockGetter,
        BlockPos blockPos, Direction direction) {
        return false;
    }
}
