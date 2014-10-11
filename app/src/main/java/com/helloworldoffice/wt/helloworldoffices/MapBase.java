package com.helloworldoffice.wt.helloworldoffices;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;

import java.util.List;

/**
 * Created by Edwin on 10/10/2014.
 */
public class MapBase extends FragmentActivity
{

    protected Location getLastLocation()
    {

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);

        if ( lm == null )
            return null;

        List<String> matchingProviders = lm.getAllProviders();

        if ( matchingProviders.size() == 0 )
            return null;

        Location bestResult = null;

        float bestAccuracy=1000;
        long bestTime=0;
        long minTime=0;


        for (String provider: matchingProviders)
        {
            Location location = lm.getLastKnownLocation(provider);

            if (location != null)
            {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy))
                {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime &&  bestAccuracy == Float.MAX_VALUE && time > bestTime)
                {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        return bestResult;

    }


}
