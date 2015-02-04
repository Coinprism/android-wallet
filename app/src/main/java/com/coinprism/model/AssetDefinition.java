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
