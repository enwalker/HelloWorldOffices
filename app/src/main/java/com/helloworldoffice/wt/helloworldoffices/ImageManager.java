package com.helloworldoffice.wt.helloworldoffices;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.io.File;

/**
 * Created by Edwin on 10/9/2014.
 * Simple class to download image if we have to.
 */
public class ImageManager  extends Object
{
    String URL;
    Context context;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    public ImageManager( Context context, String URL )
    {
        this.context = context;
        this.URL = URL;

        mRequestQueue = Volley.newRequestQueue(this.context);

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });
    }

    public Boolean IsLocalImageExist()
    {
       File FileName = new File( URL );

       File file = new File(context.getExternalFilesDir(null),FileName.getName());

       return file.exists();
    }
    public void StartDownload()
    {
        DownloadManager.Request RQ = new DownloadManager.Request( Uri.parse(URL));

        File FileName = new File( URL );

        DownloadManager DM = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        RQ.setDestinationInExternalFilesDir(context,"",FileName.getName());

        DM.enqueue( RQ );

    }

    public String getLocalPath()
    {
        File FileName = new File( URL );

        File file = new File(context.getExternalFilesDir(null),FileName.getName());

        return file.getAbsolutePath();
    }



}
