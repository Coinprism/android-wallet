package com.coinprism.model;

/**
 * Created by Flavien on 9/21/2014.
 */
public class WalletState
{
    private static WalletState state;

    private final WalletConfiguration configuration;

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

    public BalanceLoader getLoader()
    {
        return new BalanceLoader("https://api.coinprism.com");
    }

    public WalletConfiguration getConfiguration()
    {
        return this.configuration;
    }
}
