package com.coinprism.wallet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinprism.model.AssetBalance;
import com.coinprism.model.DownloadImageTask;
import com.coinprism.utils.Formatting;
import com.coinprism.wallet.R;

import java.math.BigDecimal;
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
        View rowView;
        if (convertView == null)
            rowView = inflater.inflate(R.layout.asset_balance_item, parent, false);
        else
            rowView = convertView;

        AssetBalance balance = values.get(position);

        TextView assetName = (TextView) rowView.findViewById(R.id.assetName);
        TextView assetBalance = (TextView) rowView.findViewById(R.id.assetBalance);
        ImageView assetIcon = (ImageView) rowView.findViewById(R.id.assetIcon);

        if (balance.getAsset().getIsUnknown())
        {
            Drawable placeholder = this.context.getResources().getDrawable(R.drawable.placeholder);

            setBalanceItemContents(
                rowView,
                Formatting.formatNumber(balance.getQuantity()) + " Units",
                String.format("Asset ID: %s", balance.getAsset().getAssetAddress()),
                placeholder);
        }
        else
        {
            BigDecimal decimalQuantity = new BigDecimal(balance.getQuantity())
                .scaleByPowerOfTen(-balance.getAsset().getDivisibility());

            Bitmap defaultBitmap = BitmapFactory.decodeResource(this.context.getResources(),
                R.drawable.placeholder);

            setBalanceItemContents(
                rowView,
                Formatting.formatNumber(decimalQuantity)
                    + " " + balance.getAsset().getTicker(),
                balance.getAsset().getName(),
                null);

            new DownloadImageTask(
                (ImageView) rowView.findViewById(R.id.assetIcon), defaultBitmap)
                .execute(balance.getAsset().getIconUrl());
        }

        return rowView;
    }

    public static void setBalanceItemContents(View rootView, String mainText, String subText,
        Drawable icon)
    {
        TextView assetName = (TextView) rootView.findViewById(R.id.assetName);
        TextView assetBalance = (TextView) rootView.findViewById(R.id.assetBalance);
        ImageView assetIcon = (ImageView) rootView.findViewById(R.id.assetIcon);

        assetName.setText(subText);
        assetBalance.setText(mainText);
        if (icon != null)
            assetIcon.setImageDrawable(icon);
    }
}
