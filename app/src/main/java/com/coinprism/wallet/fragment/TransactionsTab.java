package com.coinprism.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.R;

public class TransactionsTab extends Fragment implements IUpdatable
{
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_transactions, container, false);

        return rootView;
    }

    public void updateWallet()
    {
    }
}
