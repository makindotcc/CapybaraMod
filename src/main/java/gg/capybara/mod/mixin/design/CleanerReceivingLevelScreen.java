package gg.capybara.mod.mixin.design;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ReceivingLevelScreen.class)
public abstract class CleanerReceivingLevelScreen extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");

    protected CleanerReceivingLevelScreen(Component component) {
        super(component);
    }

    /**
     * @author www_makin_cc
     * @reason Default receiving screen is ugly as hell.
     */
    @Overwrite
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }
}
