package com.coinprism.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.coinprism.model.AssetBalance;
import com.coinprism.model.SingleAssetTransaction;
import com.coinprism.model.TransactionsLoader;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetBalanceAdapter;
import com.coinprism.wallet.adapter.TransactionAdapter;

import java.util.ArrayList;
import java.util.List;

public class TransactionsTab extends Fragment implements IUpdatable
{
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_transactions, container, false);

        this.adapter = new TransactionAdapter(this.getActivity(),
            new ArrayList<SingleAssetTransaction>());

        ListView listView = (ListView) rootView.findViewById(R.id.transactionList);
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        TransactionsLoader loader = new TransactionsLoader(this);
        loader.execute(WalletState.getState().getConfiguration().getAddress());
    }

    public void updateWallet()
    {
    }

    public void updateTransactions(List<SingleAssetTransaction> transactions)
    {
        if (transactions != null)
        {
            this.adapter.clear();
            this.adapter.addAll(transactions);
        }
    }
}
