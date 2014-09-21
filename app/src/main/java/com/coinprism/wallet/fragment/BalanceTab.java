package com.coinprism.wallet.fragment;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.coinprism.model.AddressBalance;
import com.coinprism.model.AssetBalance;
import com.coinprism.model.AssetDefinition;
import com.coinprism.model.BalanceLoader;
import com.coinprism.model.CoinprismWalletApplication;
import com.coinprism.model.QRCodeEncoder;
import com.coinprism.model.WalletState;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetAdapter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BalanceTab extends Fragment
{
//    private List<AssetBalance> assetBalances = new ArrayList<AssetBalance>();
    private AssetAdapter adapter;
    private TextView btcBalance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_balances, container, false);

        this.adapter = new AssetAdapter(this.getActivity(), new ArrayList<AssetBalance>());

        ListView listView = (ListView) rootView.findViewById(R.id.assetBalances);

        View listHeaderView = inflater.inflate(R.layout.fragment_tab_balances_bitcoin, listView, false);

        listView.addHeaderView(listHeaderView, null, false);
        listView.setAdapter(adapter);

        btcBalance = (TextView) listHeaderView.findViewById(R.id.btcBalance);

        this.setupUI(rootView);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.triggerUpdate();

        Timer updateTimer = new Timer();
        updateTimer.schedule(new BalanceUpdateTask(new Handler(), this), 0, 1000);
    }

    public void setupUI(View rootView)
    {
        WalletState state = WalletState.getState();

        TextView addressText = (TextView) rootView.findViewById(R.id.address);
        addressText.setText(state.getConfiguration().getAddress());

        ImageView qrCode = (ImageView) rootView.findViewById(R.id.qrAddress);

        QRCodeEncoder.createQRCode(state.getConfiguration().getAddress(), qrCode, 300, 300);
    }

    private void triggerUpdate()
    {
        WalletState state = WalletState.getState();
        BalanceLoader loader = state.getLoader();
        loader.setBalanceTab(this);
        loader.execute(state.getConfiguration().getAddress());
    }

    public void updateData(AddressBalance balance)
    {
        if (balance != null)
        {
            this.btcBalance.setText(balance.getSatoshiBalance().toString());

            this.adapter.clear();

            for (AssetBalance item : balance.getAssetBalances())
            {
                this.adapter.add(item);
            }

            this.adapter.notifyDataSetChanged();
        }
    }

    private class BalanceUpdateTask extends TimerTask
    {
        Handler handler;
        BalanceTab parent;

        public BalanceUpdateTask(Handler handler, BalanceTab parent)
        {
            super();
            this.handler = handler;
            this.parent = parent;
        }

        @Override
        public void run()
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    parent.triggerUpdate();
                }
            });
        }
    }

}
