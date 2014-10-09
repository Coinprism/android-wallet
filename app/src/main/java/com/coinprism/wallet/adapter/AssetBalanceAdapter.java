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

/**
 * Renders asset balance objects.
 */
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
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView;
        if (convertView == null)
            rowView = inflater.inflate(R.layout.asset_balance_item, parent, false);
        else
            rowView = convertView;

        final AssetBalance balance = values.get(position);

        if (balance.getAsset().getIsUnknown())
        {
            // The asset has insufficient metadata

            final Drawable placeholder = this.context.getResources().getDrawable(R.drawable.placeholder);

            setBalanceItemContents(
                rowView,
                String.format(
                    context.getString(R.string.tab_wallet_units_count), Formatting.formatNumber(balance.getQuantity())),
                String.format(context.getString(R.string.tab_wallet_asset_id), balance.getAsset().getAssetAddress()),
                placeholder);
        }
        else
        {
            // The asset has valid metadata

            final BigDecimal decimalQuantity = new BigDecimal(balance.getQuantity())
                .scaleByPowerOfTen(-balance.getAsset().getDivisibility());

            final Bitmap defaultBitmap = BitmapFactory.decodeResource(this.context.getResources(),
                R.drawable.placeholder);

            setBalanceItemContents(
                rowView,
                Formatting.formatNumber(decimalQuantity)
                    + " " + balance.getAsset().getTicker(),
                balance.getAsset().getName(),
                null);

            // Download and display the image for the asset
            new DownloadImageTask(
                (ImageView) rowView.findViewById(R.id.assetIcon), defaultBitmap)
                .execute(balance.getAsset().getIconUrl());
        }

        return rowView;
    }

    public static void setBalanceItemContents(View rootView, String mainText, String subText, Drawable icon)
    {
        final TextView assetName = (TextView) rootView.findViewById(R.id.assetName);
        final TextView assetBalance = (TextView) rootView.findViewById(R.id.assetBalance);
        final ImageView assetIcon = (ImageView) rootView.findViewById(R.id.assetIcon);

        assetName.setText(subText);
        assetBalance.setText(mainText);
        if (icon != null)
            assetIcon.setImageDrawable(icon);
    }
}
