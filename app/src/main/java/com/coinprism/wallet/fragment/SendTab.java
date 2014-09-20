package com.coinprism.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.coinprism.model.AssetBalance;
import com.coinprism.model.AssetDefinition;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetAdapter;

import java.math.BigInteger;

public class SendTab extends Fragment
{
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_send, container, false);

        return rootView;
    }
}
