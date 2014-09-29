package com.coinprism.model;

import android.app.Application;
import android.content.Context;

import com.coinprism.utils.FontsOverride;

/**
 * Created by Flavien on 9/20/2014.
 */
public class CoinprismWalletApplication extends Application
{
    private static CoinprismWalletApplication instance;

    public CoinprismWalletApplication()
    {
        instance = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        FontsOverride.setDefaultFont(this, "SANS", "fonts/OpenSans-Regular.ttf");
        FontsOverride.setDefaultFont(this, "SANS_BOLD", "fonts/OpenSans-Bold.ttf");
        FontsOverride.setDefaultFont(this, "SANS_BOLD_ITALIC", "fonts/OpenSans-BoldItalic.ttf");
    }

    public static Context getContext()
    {
        return instance;
    }
}
