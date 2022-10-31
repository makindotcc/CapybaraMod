package gg.capybara.mod;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;

public class VertexCache {
    public final VertexBuffer vertexBuffer;
    public final int textWidth;
    public final RenderType renderType;
    public final long timeoutAt;

    public VertexCache(VertexBuffer vertexBuffer, int textWidth, RenderType renderType, long timeoutAt) {
        this.vertexBuffer = vertexBuffer;
        this.textWidth = textWidth;
        this.renderType = renderType;
        this.timeoutAt = timeoutAt;
    }

    public boolean isDead() {
        return System.currentTimeMillis() >= this.timeoutAt;
    }
}
