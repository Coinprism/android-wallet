/*
 * Copyright (c) 2014 Flavien Charlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
