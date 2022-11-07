package gg.capybara.mod.optimization.font;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

public class CharSeqHashCodeCalculator implements FormattedCharSink {
    private int hashCode = 1;

    private CharSeqHashCodeCalculator() {
    }

    @Override
    public boolean accept(int index, Style style, int charCode) {
        hashCode = 31 * hashCode + style.hashCode();
        hashCode = 31 * hashCode + charCode;
        return true;
    }

    public static int calculate(FormattedCharSequence charSequence) {
        CharSeqHashCodeCalculator calculator = new CharSeqHashCodeCalculator();
        charSequence.accept(calculator);
        return calculator.hashCode;
    }
}
