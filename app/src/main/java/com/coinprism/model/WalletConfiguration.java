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

import android.util.Base64;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.script.Script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public static Script getV2RedeemScript(Script baseScript) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(new byte[] { 0x02, 0x4f, 0x41, 0x75 });
        outputStream.write(baseScript.getProgram());
        return new Script(outputStream.toByteArray());
    }
}
