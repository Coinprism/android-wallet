package com.coinprism.model;

public class AssetDefinition
{

    private final String assetAddress;
    private final String name;
    private final String ticker;
    private final int divisibility;
    private final Boolean isUnknown;

    public AssetDefinition(String assetAddress, String name, String ticker, int divisibility)
    {
        this.assetAddress = assetAddress;
        this.name = name;
        this.ticker = ticker;
        this.divisibility = divisibility;
        this.isUnknown = false;
    }

    public AssetDefinition(String assetAddress)
    {
        this.assetAddress = assetAddress;
        this.name = null;
        this.ticker = null;
        this.divisibility = 0;
        this.isUnknown = true;
    }

    public String getAssetAddress()
    {
        return this.assetAddress;
    }

    public String getName()
    {
        return this.name;
    }

    public String getTicker()
    {
        return this.ticker;
    }

    public int getDivisibility()
    {
        return this.divisibility;
    }

    public Boolean getIsUnknown()
    {
        return this.isUnknown;
    }
}
