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

package com.coinprism.model;

import java.math.BigInteger;

/**
 * Represents the balance of an address with regard to an asset.
 */
public class AssetBalance
{
    private final AssetDefinition asset;
    private final BigInteger quantity;

    public AssetBalance(AssetDefinition asset, BigInteger quantity)
    {
        this.asset = asset;
        this.quantity = quantity;
    }

    /**
     * Gets the definition of the asset.
     *
     * @return the definition of the asset
     */
    public AssetDefinition getAsset()
    {
        return this.asset;
    }

    /**
     * Gets the number of units for that asset.
     * 
     * @return the number of units for that asset
     */
    public BigInteger getQuantity()
    {
        return this.quantity;
    }
}
