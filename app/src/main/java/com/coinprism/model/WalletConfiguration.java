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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.VersionedChecksummedBytes;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Represents the configuration of a wallet.
 */
public class WalletConfiguration
{
    public static final String passwordKey = "wallet.password";
    private final byte openAssetsNamespace = 19;
    private final ECKey key;
    private final byte[] seed;
    private final String address;
    private final String receiveAssetsAddress;
    private final NetworkParameters networkParameters;

    public WalletConfiguration(String seed, NetworkParameters network)
    {
        this.seed = Base64.decode(seed, Base64.DEFAULT);

        final DeterministicKey key = HDKeyDerivation.createMasterPrivateKey(this.seed);
        this.key = HDKeyDerivation.deriveChildKey(key, 0);

        final Address address = this.key.toAddress(network);
        this.address = address.toString();
        this.networkParameters = network;

        byte[] forAssetsAddress = new byte[21];
        System.arraycopy(address.getHash160(), 0, forAssetsAddress, 1, 20);
        forAssetsAddress[0] = (byte)address.getVersion();

        this.receiveAssetsAddress = new VersionedChecksummedBytes(openAssetsNamespace, forAssetsAddress) { }.toString();
    }

    /**
     * Gets the main address of a wallet (for receiving Bitcoins).
     * @return the main address of the wallet
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Gets the main address of a wallet (for receiving assets).
     * @return the main address of the wallet
     */
    public String getReceiveAssetAddress()
    {
        return receiveAssetsAddress;
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

    public Boolean isPasswordEnabled()
    {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
            CoinprismWalletApplication.getContext());

        return sharedPrefs.getString(passwordKey, null) != null;
    }

    public Boolean comparePassword(String password)
    {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
            CoinprismWalletApplication.getContext());

        String storedValue = sharedPrefs.getString(passwordKey, null);
        return storedValue != null && storedValue.equals(computeSHAHash(password));
    }

    public void setPassword(String value)
    {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
            CoinprismWalletApplication.getContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (value != null)
            editor.putString(passwordKey, computeSHAHash(value));
        else
            editor.putString(passwordKey, null);

        editor.commit();
    }

    private static String computeSHAHash(String password)
    {
        try
        {
            MessageDigest mdSha1 = MessageDigest.getInstance("SHA-1");
            mdSha1.update(password.getBytes("ASCII"));
            byte[] data = mdSha1.digest();
            return Base64.encodeToString(data, Base64.NO_WRAP);
        }
        catch (NoSuchAlgorithmException ex)
        {
            return password;
        }
        catch (UnsupportedEncodingException ex)
        {
            return password;
        }
    }

}
