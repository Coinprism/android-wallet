package com.coinprism.model;

import android.util.Base64;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.security.SecureRandom;

/**
 * Represents the configuration of a wallet.
 */
public class WalletConfiguration
{
    private final ECKey key;
    private final byte[] seed;
    private final String address;
    private final NetworkParameters networkParameters;

    public WalletConfiguration(String seed, NetworkParameters network)
    {
        this.seed = Base64.decode(seed, Base64.DEFAULT);

        DeterministicKey key = HDKeyDerivation.createMasterPrivateKey(this.seed);
        this.key = HDKeyDerivation.deriveChildKey(key, 0);

        this.address = this.key.toAddress(network).toString();
        this.networkParameters = network;
    }

    /**
     * Gets the main address of a wallet.
     * @return the main address of the wallet
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Creates a new wallet.
     * @return the base 64 representation of the wallet seed
     */
    public static String createWallet()
    {
        SecureRandom random = new SecureRandom();
        byte[] seed = random.generateSeed(16);

        return Base64.encodeToString(seed, Base64.DEFAULT);
    }

    /**
     * Gets the main key of the wallet.
     * @return the main key of the wallet
     */
    public ECKey getKey()
    {
        return this.key;
    }

    /**
     * Gets the network parameters of the wallet.
     * @return the network parameters of the wallet
     */
    public NetworkParameters getNetworkParameters()
    {
        return networkParameters;
    }

    /**
     * Gets the seed for the current HD wallet.
     * @return the seed for the current HD wallet
     */
    public byte[] getSeed()
    {
        return seed;
    }
}
