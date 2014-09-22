package com.coinprism.model;

import java.util.List;

public class AddressBalance
{
    private final Long satoshiBalance;
    private final List<AssetBalance> assetBalances;

    public AddressBalance(Long satoshiBalance, List<AssetBalance> assetBalances)
    {
        this.satoshiBalance = satoshiBalance;
        this.assetBalances = assetBalances;
    }

    public Long getSatoshiBalance()
    {
        return this.satoshiBalance;
    }

    public List<AssetBalance> getAssetBalances()
    {
        return this.assetBalances;
    }
}
