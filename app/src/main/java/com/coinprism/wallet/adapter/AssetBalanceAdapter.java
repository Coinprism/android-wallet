package com.coinprism.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinprism.model.AssetBalance;
import com.coinprism.wallet.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.List;

public class AssetBalanceAdapter extends ArrayAdapter<AssetBalance>
{
    private final Context context;
    private final List<AssetBalance> values;

    public AssetBalanceAdapter(Context context, List<AssetBalance> values)
    {
        super(context, R.layout.asset_balance_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.asset_balance_item, parent, false);
        AssetBalance balance = values.get(position);

        TextView assetName = (TextView) rowView.findViewById(R.id.assetName);
        TextView assetBalance = (TextView) rowView.findViewById(R.id.assetBalance);
        ImageView assetIcon = (ImageView) rowView.findViewById(R.id.assetIcon);

        if (balance.getAsset().getIsUnknown())
        {
            setBalanceItemContents(
                rowView,
                NumberFormat.getNumberInstance().format(balance.getQuantity()) + " Units",
                "Unknown colored coins");
        }
        else
        {
            BigDecimal decimalQuantity = new BigDecimal(balance.getQuantity())
                .scaleByPowerOfTen(-balance.getAsset().getDivisibility());
            setBalanceItemContents(
                rowView,
                NumberFormat.getNumberInstance().format(decimalQuantity)
                    + " " + balance.getAsset().getTicker(),
                balance.getAsset().getName());
        }

        return rowView;
    }

    public static void setBalanceItemContents(View rootView, String mainText, String subText)
    {
        TextView assetName = (TextView) rootView.findViewById(R.id.assetName);
        TextView assetBalance = (TextView) rootView.findViewById(R.id.assetBalance);
        ImageView assetIcon = (ImageView) rootView.findViewById(R.id.assetIcon);

        assetName.setText(subText);
        assetBalance.setText(mainText);
    }
}
