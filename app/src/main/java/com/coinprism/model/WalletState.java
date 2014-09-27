package com.coinprism.model;

import android.content.Context;

import com.coinprism.utils.SecurePreferences;
import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.WalletOverview;
import com.google.bitcoin.core.NetworkParameters;

/**
 * Created by Flavien on 9/21/2014.
 */
public class WalletState
{
    private final static String privateKeyKey = "wallet.private_key";
    private final static String publicKeyKey = "wallet.public_key";
    private static WalletState state;
    private final SecurePreferences preferences;
    private final WalletConfiguration configuration;
    private final APIClient api;
    private AddressBalance walletData;
    private IUpdatable currentActivity;

    public WalletState(WalletConfiguration configuration, APIClient api,
        SecurePreferences preferences)
    {
        this.configuration = configuration;
        this.api = api;
        this.preferences = preferences;
    }

    public static WalletState getState()
    {
        if (state == null)
            state = initialize();

        return state;
    }

    private static WalletState initialize()
    {
        SecurePreferences preferences =
            new SecurePreferences(CoinprismWalletApplication.getContext());

        String privateKey = preferences.getString(privateKeyKey, null);
        String publicKey;

        WalletConfiguration wallet;
        if (privateKey == null)
        {
            String[] key = WalletConfiguration.createWallet();

            SecurePreferences.Editor editor = preferences.edit();
            editor.putString(privateKeyKey, key[0]);
            editor.putString(publicKeyKey, key[1]);
            editor.commit();

            privateKey = key[0];
            publicKey = key[1];
        }
        else
        {
            publicKey = preferences.getString(publicKeyKey, null);
        }

        wallet = new WalletConfiguration(
            privateKey,
            publicKey,
            NetworkParameters.fromID(NetworkParameters.ID_TESTNET));

        return new WalletState(
            wallet,
            new APIClient("https://10.0.2.2:44300"),
            preferences);
    }

    public void triggerUpdate()
    {
        BalanceLoader loader = new BalanceLoader(this);
        loader.execute(configuration.getAddress());
    }

    public void updateData(AddressBalance data)
    {
        this.walletData = data;
        this.currentActivity.updateWallet();
    }

    public AddressBalance getBalance()
    {
        return this.walletData;
    }

    public WalletConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public APIClient getAPIClient()
    {
        return this.api;
    }

    public void setCurrentActivity(WalletOverview value)
    {
        this.currentActivity = value;
    }
}
