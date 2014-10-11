package com.helloworldoffice.wt.helloworldoffices;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.CharacterPickerDialog;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.CustomNetworkImageView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class LocationDetail extends MapBase
{

    RequestQueue queue;
    private ImageLoader mImageLoader;
    private GoogleMap mMap;
    JSONObject Office;
    Boolean bOffLine = true;
    private Boolean bShowMiles = true;
    Button directionBtn;
    Button callBtn;
    Button ShareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);

        bOffLine = getIntent().getBooleanExtra("OffLine", false );

        try {
            Office = new JSONObject( getIntent().getStringExtra("OfficeData"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        queue = Volley.newRequestQueue(getApplicationContext());

        mImageLoader = new ImageLoader(queue, new ImageLoader.ImageCache()
        {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(20);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        CustomNetworkImageView Image = (CustomNetworkImageView)findViewById(R.id.imageView);

        TextView Line1 = (TextView)findViewById(R.id.textView);
        TextView Line2 = (TextView)findViewById(R.id.textView2);
        TextView Line3 = (TextView)findViewById(R.id.textView3);
        TextView Line4 = (TextView)findViewById(R.id.textView4);
        callBtn = (Button)findViewById(R.id.button);
        directionBtn = (Button)findViewById(R.id.button2);
        ShareBtn = ( Button)findViewById(R.id.button3);

        try {

            Image.setImageUrl( Office.getString("office_image"),mImageLoader);
            Line1.setText( Office.getString("name"));
            Line2.setText( Office.getString("address"));
            Line3.setText(Office.getString("city") + " " +  Office.get("state") +", " + Office.getString("zip_postal_code"));

            Location gps = getLastLocation();

            Location TempGPS = new Location("" );
            TempGPS.setLatitude( Office.getDouble("latitude"));
            TempGPS.setLongitude( Office.getDouble("longitude"));

            if ( gps != null )
            {
                float dis = (float)(gps.distanceTo(TempGPS)/1000);

                if ( bShowMiles == false )
                    Line4.setText("Distance " + String.format("%.1f",dis)+ " km");
                else
                    Line4.setText("Distance " + String.format("%.1f", dis*0.621371) + " m");
            }
            else
                Line4.setText("");




        } catch (JSONException e) {
            e.printStackTrace();
        }

        callBtn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String uri = null;
                try {
                    uri = "tel:" + Office.getString("phone");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        directionBtn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                try {

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + Office.getString("latitude") + "," + Office.getString("longitude")));

                    startActivity(intent);

                }catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        ShareBtn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ImageManager IM = null;
                try {
                    IM = new ImageManager(getApplicationContext(),Office.getString("office_image"));


                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                share.putExtra(Intent.EXTRA_SUBJECT, Office.getString("name"));
                share.putExtra(Intent.EXTRA_TEXT, Office.getString("name") + "\n" + Office.getString("address") + "\n" + Office.getString("city") + " " + Office.getString("state") +", " + Office.getString("zip_postal_code") );

                 Uri uri = Uri.fromFile( new File(IM.getLocalPath()));
                share.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(share, "Share " + Office.getString("name")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        ((LinearLayout)findViewById(R.id.layoutMap)).setVisibility(bOffLine ? View.INVISIBLE : View.VISIBLE);
        directionBtn.setVisibility( bOffLine ? View.GONE : View.VISIBLE );


    }


    private void setUpMap()
    {

        if ( Office == null )
            return;

        if ( bOffLine )
            return;

        try {

            CameraUpdate center=
                    CameraUpdateFactory.newLatLngZoom(new LatLng(Office.getDouble("latitude"), Office.getDouble("longitude")), 14);



            int mMapType =  getIntent().getIntExtra("MapType", GoogleMap.MAP_TYPE_NORMAL);

            mMap.setMapType( mMapType);

            mMap.addMarker(new MarkerOptions().position(new LatLng(Office.getDouble("latitude") , Office.getDouble("longitude"))).title(Office.getString("name")));
            mMap.moveCamera(center);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();

    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();



            setUpMap();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.Map_Normal )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_NORMAL);
            item.setChecked(true);

            return true;
        }

        if (id == R.id.Map_Satellite )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_SATELLITE);
            item.setChecked(true);

            return true;
        }

        if (id == R.id.Map_Terrain )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_TERRAIN);
            item.setChecked(true);

            return true;
        }

        if (id == R.id.Map_Hybrid )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_HYBRID);
            item.setChecked(true);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
