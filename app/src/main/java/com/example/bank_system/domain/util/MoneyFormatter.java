package com.example.bank_system.domain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats long cents values into display strings.
 *
 * Rules (BR-01, Phase 3 §3.2):
 *  - All amounts are integer ZAR cents (long).
 *  - Display always uses exactly two decimal places with an R prefix.
 *  - Never converts through binary floating point.
 *  - Negative safe-to-spend is shown as "Projected shortfall: R..."
 */
public final class MoneyFormatter {

    private MoneyFormatter() { }

    /**
     * Formats cents as "R1,234.56".
     * Negative values produce e.g. "−R1,234.56".
     */
    public static String format(long cents) {
        boolean negative = cents < 0;
        long    abs      = Math.abs(cents);

        BigDecimal bd = BigDecimal.valueOf(abs, 2); // e.g. 123456 → 1234.56
        // Format with group separator
        long   rand      = bd.longValue();
        long   remainder = abs % 100L;
        String decimal   = String.format("%02d", remainder);
        String whole     = formatWithGrouping(rand);

        return (negative ? "−R" : "R") + whole + "." + decimal;
    }

    /**
     * Formats a signed cents value for a ledger entry:
     * positive → "+R..." (teal), negative → "−R..." (red).
     * Returns the string; callers apply colour separately.
     */
    public static String formatSigned(long cents) {
        if (cents >= 0) {
            return "+" + format(cents);
        } else {
            return format(cents); // already has − prefix from format()
        }
    }

    /**
     * Formats a projected shortfall for Safe-to-Spend display.
     * e.g. −40000 → "Projected shortfall: R400.00"
     */
    public static String formatShortfall(long negativeCents) {
        long abs = Math.abs(negativeCents);
        return "Projected shortfall: " + format(abs);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private static String formatWithGrouping(long value) {
        // Manual grouping — avoids locale-dependent DecimalFormat behaviour
        String raw     = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int offset = raw.length() % 3;
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && (i - offset) % 3 == 0) sb.append(',');
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }
}
