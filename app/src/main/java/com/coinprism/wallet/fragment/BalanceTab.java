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

package com.coinprism.wallet.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.coinprism.model.AddressBalance;
import com.coinprism.model.AssetBalance;
import com.coinprism.model.QRCodeEncoder;
import com.coinprism.model.WalletState;
import com.coinprism.utils.Formatting;
import com.coinprism.wallet.QRCodeDialog;
import com.coinprism.wallet.R;
import com.coinprism.wallet.adapter.AssetBalanceAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * The tab showing the wallet address and the balance for that address.
 */
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
                    final String url = getString(R.string.link_asset_definition, balance.getAsset().getAssetAddress());
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

    /**
     * Constructs the UI when the activity is first rendered.
     *
     * @param rootView the rootView of the fragment
     */
    public void setupUI(final View rootView)
    {
        final WalletState state = WalletState.getState();

        final TextView addressText = (TextView) rootView.findViewById(R.id.address);
        final ImageView qrCode = (ImageView) rootView.findViewById(R.id.qrAddress);

        final ToggleButton addressTypeSelector = (ToggleButton)rootView.findViewById(R.id.addressTypeSelector);

        final View.OnClickListener enlarge = new View.OnClickListener()
        {
            // Enlarge the QR code
            @Override
            public void onClick(View view)
            {
                final QRCodeDialog dialog = new QRCodeDialog();
                if (addressTypeSelector.isChecked()) {
                    dialog.configure(
                        WalletState.getState().getConfiguration().getReceiveAssetAddress(),
                        getString(R.string.tab_wallet_dialog_qr_title_assets));
                }
                else {
                    dialog.configure(
                        WalletState.getState().getConfiguration().getAddress(),
                        getString(R.string.tab_wallet_dialog_qr_title_bitcoin));
                }

                dialog.show(BalanceTab.this.getActivity().getSupportFragmentManager(), "");
            }
        };

        LinearLayout addressPanel = (LinearLayout)rootView.findViewById(R.id.addressArea);
        addressPanel.setOnClickListener(enlarge);
        qrCode.setOnClickListener(enlarge);

        addressTypeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (addressTypeSelector.isChecked()) {
                    addressText.setText(state.getConfiguration().getReceiveAssetAddress());
                    QRCodeEncoder.createQRCode(
                        state.getConfiguration().getReceiveAssetAddress(), qrCode, 148, 148, 8, 0xFFFFFFFF);
                }
                else {
                    addressText.setText(state.getConfiguration().getAddress());
                    QRCodeEncoder.createQRCode(state.getConfiguration().getAddress(), qrCode, 148, 148, 8, 0xFFFFFFFF);
                }
            }
        });

        addressTypeSelector.setChecked(true);
    }

    /**
     * Triggers a refresh of the balance.
     */
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

    /**
     * Notifies the UI of an available balance update.
     */
    public void updateWallet()
    {
        if (!isAdded())
            return;

        final AddressBalance balance = WalletState.getState().getBalance();
        if (balance != null)
        {
            // The balance was successfully updated

            final BigDecimal bitcoinValue = new BigDecimal(balance.getSatoshiBalance())
                .scaleByPowerOfTen(-8);

            final Drawable btc = getResources().getDrawable(R.drawable.btc);

            AssetBalanceAdapter.setBalanceItemContents(this.listHeaderView,
                String.format(
                    getString(R.string.tab_wallet_bitcoin_count), Formatting.formatNumber(bitcoinValue)), "", btc);

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
            // There was an error while retrieving the balance, show an error message

            loadingIndicator.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.VISIBLE);
        }
    }
}
