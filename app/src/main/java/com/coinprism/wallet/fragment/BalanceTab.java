package com.coinprism.wallet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.coinprism.model.AddressBalance;
import com.coinprism.model.AssetBalance;
import com.coinprism.model.QRCodeEncoder;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.IUpdatable;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetBalanceAdapter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

public class BalanceTab extends Fragment implements IUpdatable
{
    private AssetBalanceAdapter adapter;

    //private TextView btcBalance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_balances, container, false);

        this.adapter = new AssetBalanceAdapter(this.getActivity(), new ArrayList<AssetBalance>());

        ListView listView = (ListView) rootView.findViewById(R.id.assetBalances);

        View listHeaderView = inflater.inflate(R.layout.fragment_tab_balances_bitcoin, listView, false);

        listView.addHeaderView(listHeaderView, null, false);
        listView.setAdapter(adapter);

        //btcBalance = (TextView) listHeaderView.findViewById(R.id.btcBalance);

        this.setupUI(rootView);

        return rootView;
    }

    public void setupUI(View rootView)
    {
        WalletState state = WalletState.getState();

        TextView addressText = (TextView) rootView.findViewById(R.id.address);
        addressText.setText(state.getConfiguration().getAddress());

        ImageView qrCode = (ImageView) rootView.findViewById(R.id.qrAddress);

        QRCodeEncoder.createQRCode(state.getConfiguration().getAddress(), qrCode, 400, 400, 2);
    }

    public void updateWallet()
    {
        AddressBalance balance = WalletState.getState().getBalance();
        if (balance != null)
        {
            BigDecimal bitcoinValue = new BigDecimal(balance.getSatoshiBalance())
                .scaleByPowerOfTen(-8);
            //this.btcBalance.setText(balance.getSatoshiBalance().toString());
            AssetBalanceAdapter.setBalanceItemContents(this.getView(),
                NumberFormat.getNumberInstance().format(bitcoinValue) + " BTC", "");

            this.adapter.clear();
            this.adapter.addAll(balance.getAssetBalances());
        }
    }
}
