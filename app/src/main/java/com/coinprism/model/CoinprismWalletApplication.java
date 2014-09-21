package com.coinprism.model;

import android.app.Application;

import com.coinprism.utils.FontsOverride;

/**
 * Created by Flavien on 9/20/2014.
 */
public class CoinprismWalletApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/OpenSans-Regular.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF_BOLD", "fonts/OpenSans-Bold.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF_BOLD_ITALIC", "fonts/OpenSans-BoldItalic.ttf");
    }
}
