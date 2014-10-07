package com.coinprism.model;

import android.app.Application;
import android.content.Context;

public class CoinprismWalletApplication extends Application
{
    private static CoinprismWalletApplication instance;

    public CoinprismWalletApplication()
    {
        instance = this;
    }

    public static Context getContext()
    {
        return instance;
    }
}
