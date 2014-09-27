package com.coinprism.model;

import android.util.Base64;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;

public class WalletConfiguration
{
    private final ECKey key;
    private final String address;

    public WalletConfiguration(String privateKey, String publicKey, NetworkParameters network)
    {
        byte[] privateKeyData = Base64.decode(privateKey, Base64.DEFAULT);
        byte[] publicKeyData = Base64.decode(publicKey, Base64.DEFAULT);
        this.key = new ECKey(privateKeyData, publicKeyData);
        this.address = this.key.toAddress(network).toString();
    }

    public String getAddress()
    {
        return address;
    }

    public static String[] createWallet()
    {
        ECKey key = new ECKey();
        return new String[]
        {
            Base64.encodeToString(key.getPrivKeyBytes(), Base64.DEFAULT),
            Base64.encodeToString(key.getPubKey(), Base64.DEFAULT)
        };
    }

    public ECKey getKey()
    {
        return this.key;
    }
}
