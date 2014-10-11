package com.android.volley.toolbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import com.helloworldoffice.wt.helloworldoffices.ImageManager;

/**
 * Created by Edwin on 5/7/2014.
 */
public class CustomNetworkImageView extends NetworkImageView {

    private Bitmap  mLocalBitmap;
    private boolean mShowLocal;

    public void setLocalImageBitmap(Bitmap bitmap)
    {
        if (bitmap != null) {
            mShowLocal = true;
        }
        this.mLocalBitmap = bitmap;
        requestLayout();
    }

    @Override
    public void setImageUrl(String url, ImageLoader imageLoader)
    {
        mShowLocal = false;

        ImageManager IM = new ImageManager ( getContext(), url);

        if ( IM.IsLocalImageExist() == false )
        {
            super.setImageUrl(url, imageLoader);
        }
        else
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(IM.getLocalPath());

            setLocalImageBitmap( myBitmap );
        }

        super.setImageUrl(url, imageLoader);
    }

    public CustomNetworkImageView(Context context) {
        this(context, null);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);
        if (mShowLocal) {
            setImageBitmap(mLocalBitmap);
        }
    }

}