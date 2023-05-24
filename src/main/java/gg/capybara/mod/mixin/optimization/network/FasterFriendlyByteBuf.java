package gg.capybara.mod.mixin.optimization.network;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FriendlyByteBuf.class)
public abstract class FasterFriendlyByteBuf {
    @Shadow
    public abstract int readVarInt();

    @Shadow
    public abstract ByteBuf readBytes(byte[] bs);

    /**
     * @author www_makin_cc
     * @reason Optimize readLongArray by reading whole byte array once instead of
     * reading entry by entry (readLong() in for loop).
     */
    @Overwrite
    public long[] readLongArray(long @Nullable [] ls, int i) {
        int j = this.readVarInt();
        if (ls == null || ls.length != j) {
            if (j > i) {
                throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + i);
            }
            ls = new long[j];
        }

        if (j > 0) {
            // read bytes once then create long from ready byte array instead of
            // calling readLong many times
            byte[] longsRaw = new byte[j * Longs.BYTES];
            this.readBytes(longsRaw);
            for (int k = 0; k < ls.length; ++k) {
                int offset = k * 8;
                ls[k] = Longs.fromBytes(longsRaw[offset], longsRaw[offset + 1], longsRaw[offset + 2],
                        longsRaw[offset + 3], longsRaw[offset + 4], longsRaw[offset + 5], longsRaw[offset + 6],
                        longsRaw[offset + 7]);
            }
        }
        return ls;
    }
}
