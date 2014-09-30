package com.coinprism.model;

import android.util.Base64;

import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;

public class WalletConfiguration
{
    private final ECKey key;
    private final String address;
    private final NetworkParameters networkParameters;
    private final Wallet wallet;

    public WalletConfiguration(String privateKey, String publicKey, NetworkParameters network)
    {
        byte[] privateKeyData = Base64.decode(privateKey, Base64.DEFAULT);
        byte[] publicKeyData = Base64.decode(publicKey, Base64.DEFAULT);
        this.key = new ECKey(privateKeyData, publicKeyData);
        this.address = this.key.toAddress(network).toString();
        this.networkParameters = network;
        this.wallet = new Wallet(network);
        wallet.addKey(this.key);
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

    public NetworkParameters getNetworkParameters()
    {
        return networkParameters;
    }

    public Wallet getWallet()
    {
        return wallet;
    }
}
