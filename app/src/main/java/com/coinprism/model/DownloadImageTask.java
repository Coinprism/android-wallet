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

package com.coinprism.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * A task that can asynchronously download an image from the web, and set it as the source of an ImageView.
 */
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
            e.printStackTrace();
            return this.defaultBitmap;
        }
    }

    protected void onPostExecute(Bitmap result)
    {
        bmImage.setImageBitmap(result);
    }
}
