package com.android.ble.sample.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.ble.sample.MainApplication;
import com.android.ble.sample.R;
import com.android.ble.sample.adapters.NotifiListAdapter;
import com.android.ble.sample.fragments.FloorplanFragment;
import com.android.ble.sample.fragments.OrgFragment;
import com.android.ble.sample.helper.Toaster;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.AdapterListener;
import com.android.ble.sample.listeners.FragmentActivityListener;
import com.android.ble.sample.model.NotificationModel;
import com.android.ble.sample.model.PlaceNameModel;
import com.google.android.gms.maps.model.LatLng;
import com.mist.android.AppMode;
import com.mist.android.BatteryUsage;
import com.mist.android.MSTAsset;
import com.mist.android.MSTBeacon;
import com.mist.android.MSTCentralManagerListener;
import com.mist.android.MSTCentralManagerStatusCode;
import com.mist.android.MSTClient;
import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.MSTVirtualBeacon;
import com.mist.android.MSTZone;
import com.mist.android.SourceType;
import com.mist.android.model.AppModeParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class OrgActivity extends FragmentActivity implements MSTCentralManagerListener, FragmentActivityListener,
        AdapterListener, View.OnClickListener {

    private final static String sFragmentTag = "SIMPLE_FRAGMENT_TAG";
    private PlaceNameModel placeNameModel;

    private ArrayList<NotificationModel> notificationList = new ArrayList<>();
    private ArrayList<NotificationModel> notificationVbleArrayList = new ArrayList<>();

    private String hostUri;
    private String venueId;
    private String venuePassword;
    private String topic;
    private String name;
    private String environment = "Production";

    private MainApplication app;
    private boolean isAppInBackground = false;

    public MSTMap mstMap = null;
    public MSTPoint mstPoint = null;
    public MSTVirtualBeacon mstVirtualBeacon = null;
    public ArrayList<MSTVirtualBeacon> mstVirtualBeaconList = new ArrayList<>();
    NotifiListAdapter notifiListAdapter;

    ImageView infoImageView;
    LinearLayout micelloNotificationLayout, micelloInfoLayout;
    ListView notilistView;
    private int screenWidth, screenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up notitle
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(getIntent()!=null)
        {
            placeNameModel    = (PlaceNameModel)  getIntent().getSerializableExtra("placeNameModel");
        }

        loadFragment();

        if(placeNameModel!=null && !placeNameModel.getsHeaderName().equalsIgnoreCase("Demo")) {
            this.venueId = placeNameModel.getVenueID();
            this.venuePassword = placeNameModel.getVenuePassword();
            this.environment = placeNameModel.getEnv();
            this.hostUri = placeNameModel.getHost();
            this.name = placeNameModel.getsPlaceName();


            this.app = ((MainApplication) getApplication());
            this.app.setMainActivity(this);
            connectToMist();
        }

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth  = display.getWidth();
        screenHeight = display.getHeight();

        infoImageView = (ImageView) findViewById(R.id.info_imageview);
        micelloNotificationLayout = (LinearLayout)  findViewById(R.id.micelloNotificationLayout);
        micelloInfoLayout = (LinearLayout)  findViewById(R.id.micelloInfoLayout);
        micelloInfoLayout.setVisibility(View.VISIBLE);

        notilistView = (ListView) findViewById(R.id.notilistView);
        micelloNotificationLayout.setFocusable(false);
        micelloNotificationLayout.setFocusableInTouchMode(false);

        LinearLayout.LayoutParams listParams = (LinearLayout.LayoutParams) notilistView.getLayoutParams();
        listParams.height = (int) (screenHeight*0.30);
        listParams.width = (int) (screenWidth*0.65);
        notilistView.setLayoutParams(listParams);

        infoImageView.setOnClickListener(this);
    }

    private void connectToMist() {
        MainApplication app = (MainApplication) this.getApplication();
        app.handleConnect(this.venueId, this.venuePassword, this.environment, this, this.hostUri);
    }


    private void loadFragment()
    {
        OrgFragment homeFragment = OrgFragment.newInstance(placeNameModel, mstVirtualBeaconList);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame , homeFragment, sFragmentTag).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(placeNameModel!=null) {
            MainApplication app = (MainApplication) this.getApplication();
            app.handleDisconnect();
        }
    }


    @Override
    public void onBeaconDetected(MSTBeacon[] beaconArray, String region, Date dateUpdated) {
        System.out.println("Entappiainc: onBeaconDetected");
    }

    @Override
    public void onBeaconDetected(JSONArray jsonArray, Date date) {
        if(jsonArray != null) {
            Log.v(OrgActivity.class.getSimpleName(),"onBeaconDetected: jsonArray size" + jsonArray.length());
        }
    }

    @Override
    public void onBeaconListUpdated(HashMap hashMap, Date date) {
        System.out.println("Entappiainc: onBeaconListUpdated");
    }

    @Override
    public void onLocationUpdated(final LatLng location, MSTMap[] maps, SourceType locationSource, Date dateUpdated) {
        System.out.println("Entappiainc: onLocationUpdated");

    }

    @Override
    public void onRelativeLocationUpdated(MSTPoint relativeLocation, MSTMap[] maps, Date dateUpdated) {
        System.out.println("Entappiainc: onRelativeLocationUpdated " + relativeLocation.getX() + ", " + relativeLocation.getY());

        if(relativeLocation!=null && maps!=null) {
            boolean isNewMap = false;
            if (this.mstMap == null || !this.mstMap.getMapId().equals(maps[0].getMapId())) {
                mstMap = maps[0];
                isNewMap = true;
            }

            mstPoint = relativeLocation;
            updateRelativeLocation(isNewMap);
        }
    }

    /**
     * Returns updated pressure when available to the app. The pressure will be a double measured in millibars
     * @param pressure Updated pressure
     * @param dateUpdated Date when the pressure was updated
     */
    @Override
    public void onPressureUpdated(double pressure, Date dateUpdated){

    }
    @Override
    public void onZoneStatsUpdated(MSTZone[] zones, Date dateUpdated) {
        System.out.println("Entappiainc: onZoneStatsUpdated");
    }

    @Override
    public void onClientUpdated(MSTClient[] clients, MSTZone[] zones, Date dateUpdated) {
        System.out.println("Entappiainc: onClientUpdated");
    }

    @Override
    public void onAssetUpdated(MSTAsset[] assets, MSTZone[] zones, Date dateUpdated) {
        System.out.println("Entappiainc: onAssetUpdated");
    }

    @Override
    public void onMapUpdated(final MSTMap map, Date dateUpdated) {
        System.out.println("Entappiainc: onMapUpdated " + map);

        // Received map has changed, update the mstMap.
        this.mstMap = map;

        // Therefore update the map
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment leftFragment = getSupportFragmentManager().findFragmentByTag(sFragmentTag);

                if (leftFragment instanceof FloorplanFragment){
                    FloorplanFragment mOrgFragment = (FloorplanFragment)leftFragment;
                    mOrgFragment.addIndoorMap(map);
                }
            }
        });
    }

    @Override
    public void onVirtualBeaconListUpdated(MSTVirtualBeacon[] virtualBeacons, Date dateUpdated) {
        System.out.println("Entappiainc: onVirtualBeaconListUpdated: " + virtualBeacons.toString());

        if(virtualBeacons!=null) {
            for(MSTVirtualBeacon mstVirtualBeacon : virtualBeacons)
            {
                if(mstVirtualBeacon!=null  )
                {
                    if(!Utility.isEmptyString(mstVirtualBeacon.getVbid()) && ! checkMSTVirtualBeacon(mstVirtualBeacon.getVbid(), mstVirtualBeaconList) )
                    {
                        mstVirtualBeaconList.add(mstVirtualBeacon);
                    }
                }
            }




        }
    }

    @Override
    public void onNotificationReceived(final Date dateReceived, final String message) {
        System.out.println("Entappiainc: onNotificationReceived");
        try {

            JSONObject notificationJSONObject = new JSONObject(message);

            String type = notificationJSONObject.getString("type");
            if (type.equalsIgnoreCase("zone-event-vb")) {
            JSONObject messageObject = notificationJSONObject.optJSONObject("message");

            String vbID = messageObject.optString("vbID");
            int listVblePosition = notificationVBLEListPosition(vbID);

            if (listVblePosition == -1) {

                MSTVirtualBeacon mstVirtualBeacon = getVirtualBeaconUrl(vbID);
                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setNotificationID(vbID);
                if (mstVirtualBeacon != null)
                    notificationModel.setForwardUrl(mstVirtualBeacon.getUrl());
                else
                    notificationModel.setForwardUrl("");

                notificationModel.setBeaconNotification(true);

                if (mstVirtualBeacon != null && !Utility.isEmptyString(mstVirtualBeacon.getName())) {
                    notificationModel.setBodyMessage(mstVirtualBeacon.getName());
                    notificationModel.setMstVirtualBeacon(mstVirtualBeacon);
                    notificationVbleArrayList.add(notificationModel);
                }
            }
            visibileInfoView();

            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onClientInformationUpdated(String clientName) {
        System.out.println("Entappiainc: onClientInformationUpdated "+clientName);
    }

    @Override
    public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error) {
        System.out.println("Entappiainc: onReceivedSecret");
    }

    @Override
    public void receivedLogMessageForCode(final String message, MSTCentralManagerStatusCode code) {
        if (code != (MSTCentralManagerStatusCode.MSTCentralManagerStatusCodeSentJSON) &&
                code != (MSTCentralManagerStatusCode.MSTCentralManagerStatusCodeReceivedLE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("receivedLogMessageForCode: " + message);
                    Toaster.toast("log:"+message);
                }
            });
        }
    }

    @Override
    public void receivedVerboseLogMessage(String message) {

    }

    @Override
    public void onMistErrorReceived(String s, Date date) {

    }

    @Override
    public void onMistRecommendedAction(String s) {

    }

    public void visibileInfoView()
    {
        if(this.notificationVbleArrayList!=null && this.notificationVbleArrayList.size()>0)
        {
            try {
                //setVBDistance();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mstPoint!=null && mstMap!=null) {
                            infoImageView.setVisibility(View.VISIBLE);
                            float xPos = convertCloudPointToFloorplanXScale1(mstPoint.getX());
                            float yPos =  convertCloudPointToFloorplanYScale1(mstPoint.getY());
                            MSTPoint cur_mstPoint = new MSTPoint(xPos, yPos);
                            notifiListAdapter = new NotifiListAdapter(OrgActivity.this,
                                    OrgActivity.this.notificationVbleArrayList, cur_mstPoint);
                            notilistView.setAdapter(notifiListAdapter);
                        }else
                            infoImageView.setVisibility(View.GONE);
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }else
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    infoImageView.setVisibility(View.GONE);
                    micelloNotificationLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    private float convertCloudPointToFloorplanXScale1(double meter) {
        return (float) (meter *  this.mstMap.getPpm());
    }

    private float convertCloudPointToFloorplanYScale1(double meter) {
        return (float) (meter * this.mstMap.getPpm());
    }

    // get notification list postion by zone id
    private int notificationListPosition(String sZoneId) {
        for (int i = 0; i < notificationList.size(); i++) {
            NotificationModel notificationModel = notificationList.get(i);

            if (!Utility.isEmptyString(notificationModel.getNotificationID())
                    && notificationModel.getNotificationID().equals(sZoneId)) {
                return i;
            }
        }
        return -1;
    }

    // get notification list postion by zone id
    private int notificationVBLEListPosition(String sZoneId) {
        for (int i = 0; i < notificationVbleArrayList.size(); i++) {
            NotificationModel notificationModel = notificationVbleArrayList.get(i);

            if (!Utility.isEmptyString(notificationModel.getNotificationID())
                    && notificationModel.getNotificationID().equals(sZoneId)) {
                return i;
            }
        }
        return -1;
    }

    public MSTVirtualBeacon getVirtualBeaconUrl(String mstVirtualBeaconId) {
        if (!Utility.isEmptyString(mstVirtualBeaconId) && mstVirtualBeaconList.size() != 0) {
            for (final MSTVirtualBeacon mstVirtualBeacon : mstVirtualBeaconList) {
                if (!Utility.isEmpty(mstVirtualBeacon) &&
                        mstVirtualBeacon.getVbid().equals(mstVirtualBeaconId)) {
                    /*Object[] objects = new Object[2];
                    objects[0] = mstVirtualBeacon.getUrl();
                    objects[1] = mstVirtualBeacon.getMessage();*/
                    return mstVirtualBeacon;
                }
            }
        }
        return null;

    }

    private void updateRelativeLocation(final boolean isNewMap)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(sFragmentTag);
                    if (fragment instanceof FloorplanFragment){
                        FloorplanFragment mFloorplanFragment = (FloorplanFragment)fragment;

                        if (isNewMap  || mFloorplanFragment.currentMap == null || (!mFloorplanFragment.addedMap && OrgActivity.this.mstMap != null)) {
                            // If the map hasn't been added and the mstMap has already been downloaded, add the map
                           if(isNewMap || mFloorplanFragment.currentMap == null)
                                mFloorplanFragment.addIndoorMap(OrgActivity.this.mstMap);
                            else
                                mFloorplanFragment.renderBluedot(mstPoint);
                        } else {
                            // If the map has been added, then render the bluedot
                            mFloorplanFragment.renderBluedot(mstPoint);
                        }
                    } else {
                        System.out.println("onRelativeLocationUpdated: Left fragment is not an instance of FloorplanFragment");
                    }
            }
        });
    }


    public boolean checkMSTVirtualBeacon(String mstVirtualBeaconId, ArrayList<MSTVirtualBeacon> mstVirtualBeaconList)
    {
        if(!Utility.isEmptyString(mstVirtualBeaconId)) {
            for (final MSTVirtualBeacon mstVirtualBeacon : mstVirtualBeaconList) {
                if (!Utility.isEmpty(mstVirtualBeacon)) {
                    if (!Utility.isEmptyString(mstVirtualBeacon.getVbid()) &&
                            mstVirtualBeacon.getVbid().contains(mstVirtualBeaconId)) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    @Override
    public void performFragmentActivityAction(String tagName, Object data) {
        if(tagName.equals("load_show_floor_fragment"))
        {
            loadShowFloorFragment(false);
        }else if(tagName.equals("load_show_vb_fragment"))
        {
            loadShowFloorFragment(true);
        }else if(tagName.equals("close_vble_notification"))
        {
            if(micelloNotificationLayout.isShown())
                micelloNotificationLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        //TODO: Added for background mode
        if(this.app!=null) {
            app.setAppMode(new AppModeParams(AppMode.BACKGROUND,BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,true,1d,5d));
            isAppInBackground = true; //Flag that app is in the background
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Added for background mode
        //Avoids the app startup calling this
        if (isAppInBackground && this.app!=null) {
            app.setAppMode(new AppModeParams(AppMode.FOREGROUND, BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY));
            isAppInBackground = false;
        }
    }

    private void loadShowFloorFragment(boolean isMSTVirtualBeaconShown)
    {
        FloorplanFragment floorplanFragment =  FloorplanFragment.newInstance(isMSTVirtualBeaconShown);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, floorplanFragment, sFragmentTag).commit();
    }

    @Override
    public void performAdapterAction(String tagName, Object data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.info_imageview:
                micelloNotificationLayout.setVisibility(View.VISIBLE);
                micelloNotificationLayout.setFocusable(true);
                micelloNotificationLayout.setFocusableInTouchMode(true);
                break;
        }
    }
}
