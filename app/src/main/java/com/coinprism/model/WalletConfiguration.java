package com.coinprism.model;

/**
 * Created by Flavien on 9/20/2014.
 */
public class WalletConfiguration
{
    private final String address;

    public WalletConfiguration(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }
}
