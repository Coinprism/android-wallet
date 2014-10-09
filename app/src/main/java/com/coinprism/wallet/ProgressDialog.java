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

package com.coinprism.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * A dialog showing a progress indicator.
 */
public class ProgressDialog extends DialogFragment
{
    private String title;
    private String content;
    private Boolean canCancel;
    private Boolean isCancelled = false;

    public void configure(String title, String content, Boolean canCancel)
    {
        this.title = title;
        this.content = content;
        this.canCancel = canCancel;
    }

    @android.support.annotation.NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_progress, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        if (canCancel)
        {
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    ProgressDialog.this.getDialog().cancel();
                    isCancelled = true;
                }
            });
        }

        Dialog result = builder.create();
        result.setTitle(this.title);

        TextView text = (TextView) view.findViewById(R.id.dialogContent);
        text.setText(this.content);

        return result;
    }

    public Boolean getIsCancelled()
    {
        return isCancelled;
    }
}
