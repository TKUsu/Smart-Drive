package com.su.Tap;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Nicole on 2016/5/17.
 */
public interface AsyncResponse {
    void processFinish(ArrayList<LatLng> output);
}
