package com.coinprism.wallet;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentManager;

import com.coinprism.model.QRCodeEncoder;
import com.coinprism.model.WalletState;

public class QRCodeDialog extends DialogFragment
{
    private String address;

    public void configure(String address)
    {
        this.address = address;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_qr_code, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        builder.setNegativeButton(getString(R.string.tab_wallet_dialog_qr_close), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                QRCodeDialog.this.getDialog().cancel();
            }
        });

        builder.setNeutralButton(
            getString(R.string.tab_wallet_dialog_qr_copy_address),
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    ClipboardManager clipboard = (ClipboardManager) getActivity()
                        .getSystemService(getActivity().CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", QRCodeDialog.this.address);
                    clipboard.setPrimaryClip(clip);
                }
            });

        final Dialog result = builder.create();
        result.setTitle(getString(R.string.tab_wallet_dialog_qr_title));

        final ImageView qrCode = (ImageView) view.findViewById(R.id.qrCode);
        QRCodeEncoder.createQRCode(this.address, qrCode, 592, 592, 0, 0x00FFFFFF);

        return result;
    }
}
