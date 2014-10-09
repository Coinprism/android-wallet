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
import java.util.Date;

/**
 * Represents a fraction of a transaction, either transferring an asset or bitcoins.
 */
public class SingleAssetTransaction
{
    private final String transactionId;
    private final Date date;
    private final AssetDefinition asset;
    private final BigInteger quantity;

    public SingleAssetTransaction(String transactionId, Date date, AssetDefinition asset,
        BigInteger quantity)
    {
        this.transactionId = transactionId;
        this.date = date;
        this.asset = asset;
        this.quantity = quantity;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public AssetDefinition getAsset()
    {
        return asset;
    }

    public BigInteger getQuantity()
    {
        return quantity;
    }

    public Date getDate()
    {
        return date;
    }
}
