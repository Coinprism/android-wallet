package com.coinprism.wallet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinprism.model.AssetBalance;
import com.coinprism.model.DownloadImageTask;
import com.coinprism.model.SingleAssetTransaction;
import com.coinprism.utils.Formatting;
import com.coinprism.wallet.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<SingleAssetTransaction>
{
    private final Context context;
    private final List<SingleAssetTransaction> values;

    public TransactionAdapter(Context context, List<SingleAssetTransaction> values)
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
            rowView = inflater.inflate(R.layout.transaction_item, parent, false);
        else
            rowView = convertView;

        SingleAssetTransaction balance = values.get(position);

        TextView date = (TextView) rowView.findViewById(R.id.date);
        TextView assetBalance = (TextView) rowView.findViewById(R.id.assetBalance);
        ImageView assetIcon = (ImageView) rowView.findViewById(R.id.assetIcon);

        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        date.setText(DateUtils.formatDateTime(context, balance.getDate().getTime(),
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));

        if (balance.getAsset() == null)
        {
            Drawable btc = context.getResources().getDrawable(R.drawable.btc);

            BigDecimal bitcoinValue = new BigDecimal(balance.getQuantity())
                .scaleByPowerOfTen(-8);

            assetBalance.setText(Formatting.formatNumber(bitcoinValue) + " BTC");
            assetIcon.setImageDrawable(btc);
        }
        else if (balance.getAsset().getIsUnknown())
        {
            Drawable placeholder = this.context.getResources().getDrawable(R.drawable.placeholder);

            assetBalance.setText(Formatting.formatNumber(balance.getQuantity()) + " Units");
            assetIcon.setImageDrawable(placeholder);
        }
        else
        {
            BigDecimal decimalQuantity = new BigDecimal(balance.getQuantity())
                .scaleByPowerOfTen(-balance.getAsset().getDivisibility());

            assetBalance.setText(Formatting.formatNumber(decimalQuantity)
                    + " " + balance.getAsset().getTicker());

            Bitmap defaultBitmap = BitmapFactory.decodeResource(this.context.getResources(),
                R.drawable.placeholder);

            new DownloadImageTask(assetIcon, defaultBitmap)
                .execute(balance.getAsset().getIconUrl());
        }

        if (balance.getQuantity().compareTo(BigInteger.ZERO) < 0)
            assetBalance.setTextColor(
                context.getResources().getColor(R.color.negative_transaction));
        else
            assetBalance.setTextColor(
                context.getResources().getColor(R.color.positive_transaction));

        return rowView;
    }
}
