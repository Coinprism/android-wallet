package com.coinprism.model;

public class AssetDefinition
{

    private final String assetAddress;
    private final String name;
    private final String ticker;
    private final int divisibility;

    public AssetDefinition(String assetAddress, String name, String ticker, int divisibility)
    {
        this.assetAddress = assetAddress;
        this.name = name;
        this.ticker = ticker;
        this.divisibility = divisibility;
    }

    public AssetDefinition(String assetAddress)
    {
        this(assetAddress, null, null, 0);
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
}
