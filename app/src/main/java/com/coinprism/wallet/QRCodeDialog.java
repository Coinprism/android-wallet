package com.coinprism.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.coinprism.model.QRCodeEncoder;

/**
 * A dialog showing a full screen QR code.
 */
public class QRCodeDialog extends DialogFragment
{
    private String address;

    public void configure(String address)
    {
        this.address = address;
    }

    @android.support.annotation.NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_qr_code, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        builder.setNegativeButton(getString(R.string.tab_wallet_dialog_qr_close), new DialogInterface.OnClickListener()
        {
            // Close the dialog
            public void onClick(DialogInterface dialog, int id)
            {
                QRCodeDialog.this.getDialog().cancel();
            }
        });

        builder.setNeutralButton(
            getString(R.string.tab_wallet_dialog_qr_copy_address),
            new DialogInterface.OnClickListener()
            {
                // Copy the address into the clipboard
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    ClipboardManager clipboard = (ClipboardManager) getActivity()
                        .getSystemService(FragmentActivity.CLIPBOARD_SERVICE);
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
