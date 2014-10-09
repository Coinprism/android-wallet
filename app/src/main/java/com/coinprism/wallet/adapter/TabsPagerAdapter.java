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

package com.coinprism.wallet.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coinprism.wallet.fragment.BalanceTab;
import com.coinprism.wallet.fragment.SendTab;
import com.coinprism.wallet.fragment.TransactionsTab;

/**
 * Renders the main UI tabs.
 */
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
