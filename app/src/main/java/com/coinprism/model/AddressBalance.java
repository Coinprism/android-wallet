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

import java.util.List;

/**
 * Represents the balance of an address.
 */
public class AddressBalance
{
    private final Long satoshiBalance;
    private final List<AssetBalance> assetBalances;

    public AddressBalance(Long satoshiBalance, List<AssetBalance> assetBalances)
    {
        this.satoshiBalance = satoshiBalance;
        this.assetBalances = assetBalances;
    }

    /**
     * Gets the balance in satoshis.
     *
     * @return the balance in satoshis
     */
    public Long getSatoshiBalance()
    {
        return this.satoshiBalance;
    }

    /**
     * Gets the balance for assets.
     *
     * @return the balance for assets
     */
    public List<AssetBalance> getAssetBalances()
    {
        return this.assetBalances;
    }
}
