package com.coinprism.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coinprism.model.AssetBalance;
import com.coinprism.wallet.R;

public class AssetAdapter extends ArrayAdapter<AssetBalance>
{
    private final Context context;
    private final AssetBalance[] values;

    public AssetAdapter(Context context, AssetBalance[] values)
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
        TextView textView = (TextView) rowView.findViewById(R.id.assetName);

        textView.setText(values[position].getAsset().getName());
        // change the icon for Windows and iPhone

        return rowView;
    }
}
