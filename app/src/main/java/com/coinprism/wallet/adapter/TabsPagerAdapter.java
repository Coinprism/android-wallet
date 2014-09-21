package com.coinprism.wallet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinprism.wallet.fragment.BalanceTab;
import com.coinprism.wallet.fragment.SendTab;
import com.coinprism.wallet.fragment.TransactionsTab;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
    private final Fragment[] fragments;

    public TabsPagerAdapter(FragmentManager manager)
    {
        super(manager);
        this.fragments = new Fragment[]
        {
            new SendTab(),
            new BalanceTab(),
            new TransactionsTab()
        };
    }

    @Override
    public Fragment getItem(int index)
    {
        return this.fragments[index];
    }

    @Override
    public int getCount()
    {
        // get item count - equal to number of tabs
        return this.fragments.length;
    }
}
