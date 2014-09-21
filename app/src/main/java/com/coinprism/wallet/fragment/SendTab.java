package com.coinprism.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;

import com.coinprism.model.AddressBalance;
import com.coinprism.model.AssetBalance;
import com.coinprism.model.AssetDefinition;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetBalanceAdapter;
import com.coinprism.wallet.adapter.AssetSelectorAdapter;

import java.util.ArrayList;

public class SendTab extends Fragment implements IUpdatable
{
    private AssetSelectorAdapter adapter;
    private Spinner assetSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_send, container, false);
        this.assetSpinner = (Spinner) rootView.findViewById(R.id.assetSpinner);

        this.adapter = new AssetSelectorAdapter(this.getActivity(), new ArrayList<AssetDefinition>());

        this.assetSpinner.setAdapter(adapter);

        return rootView;
    }

    public void updateWallet()
    {
        AddressBalance balance = WalletState.getState().getBalance();
        if (balance != null)
        {
            this.adapter.clear();

            for (AssetBalance item : balance.getAssetBalances())
                this.adapter.add(item.getAsset());
        }
    }
}
