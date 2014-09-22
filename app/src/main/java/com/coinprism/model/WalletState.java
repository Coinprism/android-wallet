package com.coinprism.model;

import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.WalletOverview;

/**
 * Created by Flavien on 9/21/2014.
 */
public class WalletState
{
    private static WalletState state;

    private final WalletConfiguration configuration;
    private final APIClient api;
    private AddressBalance walletData;
    private IUpdatable currentActivity;

    public WalletState(WalletConfiguration configuration, APIClient api)
    {
        this.configuration = configuration;
        this.api = api;
    }

    public static WalletState getState()
    {
        if (state == null)
        {
            state = initialize();
        }

        return state;
    }

    private static WalletState initialize()
    {
        return new WalletState(
            new WalletConfiguration("mpb6mkVeNqUD2q6YrBnBfYv8ejPTuuyUrH"),
            new APIClient("https://10.0.2.2:44300"));
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
