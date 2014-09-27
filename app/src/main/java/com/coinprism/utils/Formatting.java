package com.coinprism.utils;

import java.math.BigInteger;
import java.text.NumberFormat;

/**
 * Created by Flavien on 9/27/2014.
 */
public class Formatting
{
    public static String formatNumber(Number number)
    {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(30);

        return format.format(number);
    }
}
