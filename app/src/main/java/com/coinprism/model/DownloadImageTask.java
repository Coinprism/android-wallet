package com.coinprism.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private final ImageView bmImage;
    private final Bitmap defaultBitmap;

    public DownloadImageTask(ImageView bmImage, Bitmap defaultBitmap)
    {
        this.bmImage = bmImage;
        this.defaultBitmap = defaultBitmap;
    }

    protected Bitmap doInBackground(String... urls)
    {
        String urlDisplay = urls[0];
        Bitmap mIcon11 = null;
        try
        {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            return mIcon11;
        }
        catch (Exception e)
        {
            //Log.e("Error", e.getMessage());
            e.printStackTrace();
            return this.defaultBitmap;
        }
    }

    protected void onPostExecute(Bitmap result)
    {
        bmImage.setImageBitmap(result);
    }
}
