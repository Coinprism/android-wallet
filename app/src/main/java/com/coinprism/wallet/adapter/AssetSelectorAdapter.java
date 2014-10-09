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

package com.coinprism.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coinprism.model.AssetDefinition;
import com.coinprism.wallet.R;

import java.util.List;

/**
 * Renders the list of available asset for a spinner control.
 */
public class AssetSelectorAdapter extends ArrayAdapter<AssetDefinition>
{
    private final Context context;
    private final List<AssetDefinition> values;

    public AssetSelectorAdapter(Context context, List<AssetDefinition> values)
    {
        super(context, R.layout.asset_selector_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView rowView;
        if (convertView == null)
            rowView = (TextView)inflater.inflate(R.layout.asset_selector_item, parent, false);
        else
            rowView = (TextView)convertView;

        AssetDefinition item = values.get(position);

        if (item == null)
            rowView.setText(context.getString(R.string.tab_send_selector_short_bitcoin));
        else if (item.getTicker() != null)
            rowView.setText(item.getTicker());
        else
            rowView.setText(context.getString(R.string.tab_send_selector_short_unknown_asset));

        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView rowView = (TextView)inflater.inflate(R.layout.asset_selector_item_padding, parent,
            false);

        AssetDefinition item = values.get(position);

        if (item == null)
            rowView.setText(context.getString(R.string.tab_send_selector_long_bitcoin));
        else if (item.getName() != null)
            rowView.setText(item.getName());
        else
            rowView.setText(String.format(context.getString(R.string.tab_send_selector_long_unknown_asset), item.getAssetAddress().substring(0, 10)));

        return rowView;
    }
}
