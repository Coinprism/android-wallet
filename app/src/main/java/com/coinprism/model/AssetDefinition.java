package com.coinprism.model;

/**
 * Represents the definition of an asset.
 */
public class AssetDefinition
{
    private final String assetId;
    private final String name;
    private final String ticker;
    private final int divisibility;
    private final String iconUrl;
    private final Boolean isUnknown;

    public AssetDefinition(String assetId, String name, String ticker, int divisibility,
        String iconUrl)
    {
        this.assetId = assetId;
        this.name = name;
        this.ticker = ticker;
        this.divisibility = divisibility;
        this.iconUrl = iconUrl;
        this.isUnknown = false;
    }

    public AssetDefinition(String assetId)
    {
        this.assetId = assetId;
        this.name = null;
        this.ticker = null;
        this.divisibility = 0;
        this.iconUrl = null;
        this.isUnknown = true;
    }

    public String getAssetId()
    {
        return this.assetId;
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

    public String getIconUrl()
    {
        return iconUrl;
    }
}
