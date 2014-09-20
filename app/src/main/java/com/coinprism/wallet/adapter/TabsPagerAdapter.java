package com.coinprism.wallet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinprism.wallet.fragment.BalanceTab;
import com.coinprism.wallet.fragment.SendTab;
import com.coinprism.wallet.fragment.TransactionsTab;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
    public TabsPagerAdapter(FragmentManager manager)
    {
        super(manager);
    }

    @Override
    public Fragment getItem(int index)
    {
        switch (index)
        {
            case 0:
                return new SendTab();
            case 1:
                return new BalanceTab();
            case 2:
                return new TransactionsTab();
        }

        return null;
    }

    @Override
    public int getCount()
    {
        // get item count - equal to number of tabs
        return 3;
    }
}
