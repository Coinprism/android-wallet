package com.coinprism.model;

import java.math.BigInteger;

public class AssetBalance
{

    private final AssetDefinition asset;
    private final BigInteger quantity;

    public AssetBalance(AssetDefinition asset, BigInteger quantity)
    {
        this.asset = asset;
        this.quantity = quantity;
    }

    public AssetDefinition getAsset()
    {
        return this.asset;
    }

    public BigInteger getQuantity()
    {
        return this.quantity;
    }
}
