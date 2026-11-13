package com.diogocorreadcs.bounty.utils;

import com.diogocorreadcs.bounty.BountyPlugin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberFormatter {

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q"};
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));

    private NumberFormatter() {
    }

    public static String format(double value) {
        BountyPlugin plugin = BountyPlugin.getInstance();
        String symbol = plugin != null ? plugin.getConfigManager().getCurrencySymbol() : "$";
        boolean useShort = plugin == null || plugin.getConfigManager().isUseShortNumbers();

        if (!useShort || value < 1000.0D) {
            return symbol + DECIMAL_FORMAT.format(value);
        }

        int index = 0;
        double scaled = value;
        while (scaled >= 1000.0D && index < SUFFIXES.length - 1) {
            scaled /= 1000.0D;
            index++;
        }

        return symbol + DECIMAL_FORMAT.format(scaled) + SUFFIXES[index];
    }
}
