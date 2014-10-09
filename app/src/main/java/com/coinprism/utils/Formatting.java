package com.coinprism.utils;

import java.text.NumberFormat;

/**
 * Contains formatting helper functions.
 */
public class Formatting
{
    /**
     * Formats a number as a string.
     *
     * @param number the number to format
     * @return the formatted number
     */
    public static String formatNumber(Number number)
    {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(30);

        return format.format(number);
    }
}
