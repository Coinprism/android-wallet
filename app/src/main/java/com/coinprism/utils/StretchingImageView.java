package com.coinprism.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.coinprism.wallet.R;

/**
 * Created by Flavien on 10/6/2014.
 */
public class StretchingImageView extends ImageView
{
    public StretchingImageView(Context context)
    {
        super(context);
    }

    public StretchingImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public StretchingImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (height > width)
            setMeasuredDimension(width, width);
        else
            setMeasuredDimension(height, height);
    }
}
