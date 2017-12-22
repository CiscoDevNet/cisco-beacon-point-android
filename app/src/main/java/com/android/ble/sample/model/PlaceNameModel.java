package com.android.ble.sample.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Entappiainc on 02-03-2016.
 */
public class PlaceNameModel implements Serializable {

    String sPlaceName;
    String sConfigFileName;
    String sHeaderName;

    String venueID;
    String venuePassword;
    String host;
    String env;



    public PlaceNameModel(String sPlaceName, String sConfigFileName, String sHeaderName,  JSONObject raw) {
        this.sPlaceName = sPlaceName;
        this.sConfigFileName = sConfigFileName;
        this.sHeaderName = sHeaderName;

        try{
            this.venueID = (String)raw.get("venueID");
            this.venuePassword = (String)raw.get("venuePassword");
            this.host = (String)raw.get("host");
            this.env = (String)raw.get("env");
        } catch (JSONException e) {
            Log.e("PlaceNameModel",e.getMessage());
        }
    }


    public String getsPlaceName() {
        return sPlaceName;
    }

    public void setsPlaceName(String sPlaceName) {
        this.sPlaceName = sPlaceName;
    }

    public String getsConfigFileName() {
        return sConfigFileName;
    }

    public void setsConfigFileName(String sConfigFileName) {
        this.sConfigFileName = sConfigFileName;
    }

    public String getsHeaderName() {
        return sHeaderName;
    }

    public void setsHeaderName(String sHeaderName) {
        this.sHeaderName = sHeaderName;
    }

    public String getEnv() {
        return env;
    }

    public String getHost() {
        return host;
    }

    public String getVenueID() {
        return venueID;
    }

    public String getVenuePassword() {
        return venuePassword;
    }


}
