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
    private AddressBalance walletData;
    private IUpdatable currentActivity;

    public WalletState(WalletConfiguration configuration)
    {
        this.configuration = configuration;
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
        //BalanceLoader loader = new BalanceLoader("https://10.0.2.2:44300");

        return new WalletState(new WalletConfiguration("mpb6mkVeNqUD2q6YrBnBfYv8ejPTuuyUrH"));
    }

    public void triggerUpdate()
    {
        BalanceLoader loader = new BalanceLoader("https://api.coinprism.com", this);
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

    public void setCurrentActivity(WalletOverview value)
    {
        this.currentActivity = value;
    }
}
