package com.coinprism.model;

import android.util.Base64;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.security.SecureRandom;

public class WalletConfiguration
{
    private final ECKey key;
    private final String address;
    private final NetworkParameters networkParameters;

    public WalletConfiguration(String seed, NetworkParameters network)
    {
        byte[] seedData = Base64.decode(seed, Base64.DEFAULT);

        DeterministicKey key = HDKeyDerivation.createMasterPrivateKey(seedData);
        this.key = HDKeyDerivation.deriveChildKey(key, 0);

        this.address = this.key.toAddress(network).toString();
        this.networkParameters = network;
    }

    public String getAddress()
    {
        return address;
    }

    public static String createWallet()
    {
        SecureRandom random = new SecureRandom();
        byte[] seed = random.generateSeed(32);

        return Base64.encodeToString(seed, Base64.DEFAULT);
    }

    public ECKey getKey()
    {
        return this.key;
    }

    public NetworkParameters getNetworkParameters()
    {
        return networkParameters;
    }
}
