package gg.capybara.mod;

import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;

public class VertexCache {
    public final VertexBuffer vertexBuffer;
    public final int returnValue;
    public final RenderType renderType;

    public VertexCache(VertexBuffer vertexBuffer, int returnValue, RenderType renderType) {
        this.vertexBuffer = vertexBuffer;
        this.returnValue = returnValue;
        this.renderType = renderType;
    }
}
