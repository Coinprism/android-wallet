package com.coinprism.model;

public class AddressBalance
{
    private final Long satoshiBalance;
    private final AssetBalance[] assetBalances;

    public AddressBalance(Long satoshiBalance, AssetBalance[] assetBalances)
    {
        this.satoshiBalance = satoshiBalance;
        this.assetBalances = assetBalances;
    }

    public Long getSatoshiBalance()
    {
        return this.satoshiBalance;
    }

    public AssetBalance[] getAssetBalances()
    {
        return this.assetBalances;
    }
}
