package com.helloworldoffice.wt.helloworldoffices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapsActivity extends MapBase {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    ConnectionDetector CD;
    RequestQueue queue;
    OfficeAdapter OA;
    ListView OfficeList;
    Boolean bOffLine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        OfficeList = (ListView)findViewById(R.id.listView);

        OfficeList.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                JSONObject Office = (JSONObject)parent.getItemAtPosition( position );

                Intent LD = new Intent(getApplicationContext(),LocationDetail.class);

                int mType =  mMap.getMapType();

                LD.putExtra("MapType",mType);
                LD.putExtra("OffLine", bOffLine);
                LD.putExtra("OfficeData", Office.toString() );

                startActivity( LD );

            }
        });

        queue = Volley.newRequestQueue( getApplicationContext() );

        Button twitterBtn = (Button)findViewById(R.id.button);

        twitterBtn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(getApplicationContext(), Twitter.class);
                startActivity( intent);

            }
        });

        if ( CD == null )
            CD = new ConnectionDetector( getApplicationContext() );

        SharedPreferences prefs = getSharedPreferences("HelloWorld", MODE_PRIVATE);
        bOffLine = prefs.getBoolean("bOffLine", false);

        if ( CD.isConnectingToInternet() == false )
            bOffLine = false;



        ((LinearLayout)findViewById(R.id.ButtonLayout)).setVisibility( bOffLine ? View.GONE : View.VISIBLE);
        ((LinearLayout)findViewById(R.id.layoutMap)).setVisibility( bOffLine ? View.GONE : View.VISIBLE);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_maps, menu);
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

            return true;
        }

        if (id == R.id.Map_Terrain )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_TERRAIN);

            return true;
        }

        if (id == R.id.Map_Hybrid )
        {
            mMap.setMapType( GoogleMap.MAP_TYPE_HYBRID);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();


        if ( (bOffLine == false  ) || ( CD.isConnectingToInternet() ) )
        {
            StringRequest Object = new StringRequest(Request.Method.GET,"http://www.helloworld.com/helloworld_locations.json",new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    SharedPreferences prefs = getSharedPreferences("HelloWorld", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Officejson", response);
                    editor.commit();
                    setUpMapIfNeeded();
                    LoadMapAndPins( response );

                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {

                        }
                    });

            queue.add(Object);

        }
        else
        {
            SharedPreferences prefs = getSharedPreferences("HelloWorld", MODE_PRIVATE);
            String OfficeJsonString = prefs.getString("Officejson","");

            if ( OfficeJsonString.length() == 0 )
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getApplicationContext());

                // set title
                alertDialogBuilder.setTitle("Internet Data");

                // set dialog message
                alertDialogBuilder
                        .setMessage("You must first connect to the internet.")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id)
                            {

                            }
                        });


                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

            LoadMapAndPins( OfficeJsonString );

        }

    }


    private void setUpMapIfNeeded()
    {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        }
    }



    private void LoadMapAndPins( String JsonString )
    {
        JSONObject MainJson = null;

        try {

            MainJson = new JSONObject( JsonString );

            OA = new OfficeAdapter(getApplicationContext(), MainJson,getLastLocation());

            OfficeList.setAdapter( OA );

            if ( bOffLine )
                return;

            JSONArray Locations = MainJson.getJSONArray("locations");

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for( Integer I = 0; I < Locations.length(); I++ )
            {
                JSONObject Office = Locations.getJSONObject(I);

                if ( mMap != null )
                {
                    LatLng pt = new LatLng(Office.getDouble("latitude"), Office.getDouble("longitude"));

                    mMap.addMarker(new MarkerOptions().position(pt).title(Office.getString("name")));

                    builder.include( pt );
                }

                ImageManager IM = new ImageManager ( getApplicationContext(), Office.getString("office_image"));

                if ( IM.IsLocalImageExist() == false ) {
                    IM.StartDownload();
                }
            }

            if ( mMap != null )
            {
                LatLngBounds bounds = builder.build();
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                mMap.moveCamera(cu);
                mMap.animateCamera(cu);
            }


            }
        catch (JSONException e)
            {
                e.printStackTrace();
            }

    }



}
