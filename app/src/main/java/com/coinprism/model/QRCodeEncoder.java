package com.coinprism.model;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

public class QRCodeEncoder
{
    private static final int BLACK = 0xFF000000;

    public static void createQRCode(String text, ImageView iv, int width, int height, int quietZone, int white)
    {
        // barcode image
        Bitmap bitmap = null;

        try
        {
            bitmap = encodeAsBitmap(text, BarcodeFormat.QR_CODE, width, height, quietZone, white);
            iv.setImageBitmap(bitmap);
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
    }

    private static Bitmap encodeAsBitmap(
        String contents, BarcodeFormat format, int img_width, int img_height, int quietZone, int white)
        throws WriterException
    {
        String contentsToEncode = contents;
        if (contentsToEncode == null)
        {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null)
        {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try
        {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        }
        catch (IllegalArgumentException iae)
        {
            // Unsupported format
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int adjustedWidth = width - (2 * quietZone);
        int adjustedHeight = height - (2 * quietZone);
        int[] pixels = new int[adjustedWidth * adjustedHeight];
        for (int y = quietZone; y < height - quietZone; y++)
        {
            int v = y - quietZone;
            for (int x = quietZone; x < width - quietZone; x++)
            {
                int u = x - quietZone;
                pixels[v * adjustedWidth + u] = result.get(x, y) ? BLACK : white;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(adjustedWidth, adjustedHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, adjustedWidth, 0, 0, adjustedWidth, adjustedHeight);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents)
    {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++)
        {
            if (contents.charAt(i) > 0xFF)
            {
                return "UTF-8";
            }
        }
        return null;
    }
}