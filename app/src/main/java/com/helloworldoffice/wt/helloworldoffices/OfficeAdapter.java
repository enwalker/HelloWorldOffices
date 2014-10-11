package com.helloworldoffice.wt.helloworldoffices;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.CustomNetworkImageView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.games.internal.constants.ScoreOrder;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Edwin on 10/8/2014.
 * Simple adapter
 */

public class OfficeAdapter extends BaseAdapter
{
    Context context;
    ArrayList<OfficeData> OfficeData;
    Location gps;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Boolean bShowMiles = true;

    public OfficeAdapter(Context context, JSONObject Offices, final Location gps)
    {

        this.context = context;
        this.gps = gps;

        // setup
        mRequestQueue = Volley.newRequestQueue(this.context);

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(20);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        // all of the json will be passed in, but since we cannot sort a jsonarry
        // we move everything into our own array, then we sort base on distance.
        //

        OfficeData = new ArrayList<OfficeData>();

        try {


            for(Integer I = 0; I < Offices.getJSONArray("locations").length();I++)
            {
               OfficeData OD = new OfficeData();
               OD.Data = Offices.getJSONArray("locations").getJSONObject(I);
               OfficeData.add( OD );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // now sort base on distance.
        // since we only have a few items, this will not take long
        // however, if there are many itmes, we might want to look
        // at performance
        Collections.sort( OfficeData, new Comparator<OfficeAdapter.OfficeData>()
        {
            @Override
            public int compare(OfficeAdapter.OfficeData lhs, OfficeAdapter.OfficeData rhs) {
                try {

                    Location lhsTempGPS = new Location("");
                    lhsTempGPS.setLatitude(lhs.Data.getDouble("latitude"));
                    lhsTempGPS.setLongitude(lhs.Data.getDouble("longitude"));

                    Location rhsTempGPS = new Location("");
                    rhsTempGPS.setLatitude(rhs.Data.getDouble("latitude"));
                    rhsTempGPS.setLongitude(rhs.Data.getDouble("longitude"));

                    if (lhsTempGPS.distanceTo(gps) < rhsTempGPS.distanceTo(gps))
                        return -1;

                    if (lhsTempGPS.distanceTo(gps) > rhsTempGPS.distanceTo(gps))
                        return 1;

                    return 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 0;
            }});

    }

    @Override
    public int getCount() { return OfficeData.size(); }

    @Override
    public Object getItem(int position)
    {
        return OfficeData.get(position).Data;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        // the view, get your objects.
        View view = View.inflate(context, R.layout.officerow, null);

        TextView text1 = (TextView) view.findViewById(R.id.textView);
        TextView text2 = (TextView) view.findViewById(R.id.textView2);
        TextView text3 = (TextView) view.findViewById(R.id.textView3);

        CustomNetworkImageView Image = (CustomNetworkImageView)view.findViewById(R.id.imageView);

        try {

            // get the json object from our array
            JSONObject Office = OfficeData.get(position).Data;

            // set the image, the custom image class will load from the internet or local drive.
            Image.setImageUrl(Office.getString("office_image"),mImageLoader);

            text1.setTextColor(context.getResources().getColor(R.color.B3));
            text2.setTextColor(context.getResources().getColor(R.color.B3));
            text3.setTextColor(context.getResources().getColor(R.color.B3));

            text1.setText(Office.getString("name"));
            text2.setText(Office.getString("address"));

            // do we have a valid gps data
            if ( gps != null )
            {
                // get GPS info of the office.
                Location TempGPS = new Location("" );
                TempGPS.setLatitude( Office.getDouble("latitude"));
                TempGPS.setLongitude( Office.getDouble("longitude"));

                // how far away are we
                float dis = (float)(gps.distanceTo(TempGPS)/1000);

                // display distance
                if ( bShowMiles == false )
                  text3.setText("Distance " + String.format("%.1f",dis)+ " km");
                else
                  text3.setText("Distance " + String.format("%.1f", dis*0.621371) + " m");
            }
            else
                text3.setText("");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return view;
    }

    public void UpdateGPS( Location location )
    {
        gps = location;

    }

    public void setShowMiles( boolean bValue )
    {
        bShowMiles = bValue;
    }

    private class OfficeData
    {
        public JSONObject Data;
    }

}