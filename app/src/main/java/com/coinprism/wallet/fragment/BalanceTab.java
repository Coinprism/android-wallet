package com.coinprism.wallet.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.coinprism.model.AddressBalance;
import com.coinprism.model.AssetBalance;
import com.coinprism.model.QRCodeEncoder;
import com.coinprism.model.WalletState;
import com.coinprism.utils.Formatting;
import com.coinprism.wallet.QRCodeDialog;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetBalanceAdapter;

import org.bitcoinj.core.Wallet;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

public class BalanceTab extends Fragment
{
    private AssetBalanceAdapter adapter;
    private View listHeaderView;
    private View assetHeaderText;
    private ListView listView;
    private View loadingIndicator;
    private View errorMessageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.fragment_tab_balances, container, false);

        this.adapter = new AssetBalanceAdapter(this.getActivity(), new ArrayList<AssetBalance>());

        listView = (ListView) rootView.findViewById(R.id.assetBalances);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    final AssetBalance balance = adapter.getItem(position - 1);
                    final String url = String.format("https://www.coinprism.info/asset/%s",
                        balance.getAsset().getAssetAddress());
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });

        listHeaderView = inflater.inflate(R.layout.fragment_tab_balances_bitcoin, listView, false);
        assetHeaderText = listHeaderView.findViewById(R.id.assetsHeader);
        listView.addHeaderView(listHeaderView, null, false);
        listView.setAdapter(adapter);

        errorMessageView = rootView.findViewById(R.id.errorMessage);
        loadingIndicator = rootView.findViewById(R.id.loadingIndicator);

        this.setupUI(rootView);

        WalletState.getState().setBalanceTab(this);
        return rootView;
    }

    public void setupUI(final View rootView)
    {
        final WalletState state = WalletState.getState();

        final TextView addressText = (TextView) rootView.findViewById(R.id.address);
        addressText.setText(state.getConfiguration().getAddress());

        final ImageView qrCode = (ImageView) rootView.findViewById(R.id.qrAddress);

        QRCodeEncoder.createQRCode(state.getConfiguration().getAddress(), qrCode, 400, 400, 20);

        LinearLayout addressPanel = (LinearLayout)rootView.findViewById(R.id.topArea);
        addressPanel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final QRCodeDialog dialog = new QRCodeDialog();
                dialog.configure(WalletState.getState().getConfiguration().getAddress());
                dialog.show(BalanceTab.this.getActivity().getSupportFragmentManager(), "");
            }
        });
    }

    public void triggerRefresh()
    {
        if (loadingIndicator.getVisibility() == View.GONE)
        {
            loadingIndicator.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);

            WalletState.getState().triggerUpdate();
        }
    }

    public void updateWallet()
    {
        if (!isAdded())
            return;

        final AddressBalance balance = WalletState.getState().getBalance();
        if (balance != null)
        {
            final BigDecimal bitcoinValue = new BigDecimal(balance.getSatoshiBalance())
                .scaleByPowerOfTen(-8);

            final Drawable btc = getResources().getDrawable(R.drawable.btc);

            AssetBalanceAdapter.setBalanceItemContents(this.listHeaderView,
                Formatting.formatNumber(bitcoinValue) + " BTC", "", btc);

            this.adapter.clear();
            this.adapter.addAll(balance.getAssetBalances());

            if (balance.getAssetBalances().isEmpty())
                this.assetHeaderText.setVisibility(View.GONE);
            else
                this.assetHeaderText.setVisibility(View.VISIBLE);

            loadingIndicator.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.GONE);
        }
        else
        {
            loadingIndicator.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
        }
    }
}
