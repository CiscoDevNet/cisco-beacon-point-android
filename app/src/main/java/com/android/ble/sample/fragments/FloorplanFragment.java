package com.android.ble.sample.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.android.ble.sample.MainApplication;
import com.android.ble.sample.R;
import com.android.ble.sample.activity.OrgActivity;
import com.android.ble.sample.constants.PreferenceConstants;
import com.android.ble.sample.customview.DrawLine;
import com.android.ble.sample.customview.ZoomLayout;
import com.android.ble.sample.helper.AppImageLoader;
import com.android.ble.sample.helper.PreferenceHelper;
import com.android.ble.sample.helper.Toaster;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.Callback;
import com.android.ble.sample.listeners.FragmentActivityListener;
import com.android.ble.sample.mist.MSTEdges;
import com.android.ble.sample.mist.MSTGraph;
import com.android.ble.sample.mist.MSTNode;
import com.android.ble.sample.mist.MSTPath;
import com.android.ble.sample.mist.MSTWayfinder;
import com.android.volley.VolleyLog;
import com.android.volley.cache.plus.ImageLoader;
import com.android.volley.cache.plus.SimpleImageLoader;
import com.android.volley.error.VolleyError;
import com.mist.android.MSTMap;
import com.mist.android.MSTPoint;
import com.mist.android.MSTVirtualBeacon;
import com.mist.android.MapType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class FloorplanFragment extends Fragment implements ZoomLayout.ZoomViewTouchListener{

    Context mContext;

    FragmentActivityListener fragmentActivityListener;
    String mapName = "";

    MSTMap mstMap = null;
    MSTPoint mstPoint = null;

    public boolean addedMap;
    public MSTMap currentMap;
    private ImageView floorplanImageView;
    private FrameLayout floorplanBluedotView;
    private RelativeLayout floorplanLayout;
    private RelativeLayout floorplanVBLayout;
    private double scaleXFactor;
    private double scaleYFactor;
    private boolean scaleFactorCalled;
    private float markerViewWidth;
    private float markerViewHeight;

    private float floorImageLeftMargin;
    private float floorImageTopMargin;

    private float nearestMarkerViewWidth;
    private float nearestMarkerViewHeight;

    private float zoomScaleFactor=1;


    private PreferenceHelper preferenceHelper;
    private ArrayList<MSTVirtualBeacon> mstVirtualBeaconArrayList;
    private boolean isMSTVirtualBeaconShown = false;
    private MSTGraph graph;
    private HashMap<String, Object> nodes;
    private boolean hasAddedWayfinding;
    private MSTWayfinder wayfinder;
    private ArrayList<String> _previousPathArr;
    private MSTPoint startingPoint;
    private MSTPoint endingPoint;
    private MSTPoint preHasMotionPoint;
    private MSTPoint showXYMstPoint;

    ImageView bluedot_flashlight_image;
    ImageView bluedot_flashlight;


    private ZoomLayout floorplan_zoomlayout;
    private ToggleButton snaptopathToggleButton, showallpathToggleButton, wayfindingToggleButton;


    private boolean isActualData = false;
    private boolean isWayfindingAdded = false;
    private boolean isNewPath = false;

    Handler handler;
    RenderWayfindingAsyncTask renderWayfindingAsyncTask;

    SimpleImageLoader modelLoader;


    public static FloorplanFragment newInstance(boolean isMSTVirtualBeaconShown ) {

        FloorplanFragment mFloorplanFragment = new FloorplanFragment();
        Bundle mBundle = new Bundle();
        mBundle.putBoolean("isMSTVirtualBeaconShown", isMSTVirtualBeaconShown);
        mFloorplanFragment.setArguments(mBundle);
        return mFloorplanFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_floorplanlayout, container, false);


        fragmentActivityListener = (FragmentActivityListener) getActivity();
        preferenceHelper = new PreferenceHelper(getActivity());
        // get sensorManager and initialise sensor listeners
        handler = new Handler();

        Bundle mBundle = getArguments();
        if (mBundle != null) {
            isMSTVirtualBeaconShown = mBundle.getBoolean("isMSTVirtualBeaconShown");
        }

        if (modelLoader == null) {
            modelLoader = AppImageLoader.getImageLoaderInstance(getActivity(), R.mipmap.ic_launcher, "FLOOR_PLAN_IMAGES");
        }

        OrgActivity activity = (OrgActivity) getActivity();
        if (activity != null) {
            this.mstMap = activity.mstMap;
            this.mstPoint = activity.mstPoint;
            this.mstVirtualBeaconArrayList = activity.mstVirtualBeaconList;
        }

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        mContext = getActivity().getApplicationContext();
        ((MainApplication) getActivity().getApplication()).setMainActivity(getActivity());

        this.floorplan_zoomlayout = (ZoomLayout) view.findViewById(R.id.floorplan_zoomlayout) ;
        this.floorplanVBLayout = (RelativeLayout) view.findViewById(R.id.floorplanVBLayout) ;

        floorplanImageView = (ImageView) view.findViewById(R.id.floorplan_image);
        bluedot_flashlight_image = (ImageView) view.findViewById(R.id.bluedot_flashlight_image);
        bluedot_flashlight = (ImageView) view.findViewById(R.id.bluedot_flashlight);

        floorplanLayout = (RelativeLayout) view.findViewById(R.id.floorplan_layout);
        floorplanBluedotView = (FrameLayout) view.findViewById(R.id.floorplan_bluedot);

        floorplanVBLayout.setVisibility(isMSTVirtualBeaconShown? View.VISIBLE: View.GONE);

        snaptopathToggleButton = (ToggleButton)view.findViewById(R.id.snaptopathToggleButton);
        showallpathToggleButton = (ToggleButton)view.findViewById(R.id.showallpathToggleButton);
        wayfindingToggleButton = (ToggleButton)view.findViewById(R.id.wayfindingToggleButton);


        snaptopathToggleButton.setChecked(preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH, true));
        showallpathToggleButton.setChecked(preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false));
        wayfindingToggleButton.setChecked(preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, false));

        snaptopathToggleButton.setOnCheckedChangeListener(onCheckedChangeListener);
        showallpathToggleButton.setOnCheckedChangeListener(onCheckedChangeListener);
        wayfindingToggleButton.setOnCheckedChangeListener(onCheckedChangeListener);

        floorplan_zoomlayout.setListener(this);
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()) {

                case R.id.wayfindingToggleButton:
                    Toaster.toast("Save settings");
                    preferenceHelper.saveBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, isChecked);
                    fragmentActivityListener.performFragmentActivityAction(PreferenceConstants.KEY_ENABLE_WAYFINDING, isChecked);
                    break;

                case R.id.snaptopathToggleButton:
                    preferenceHelper.saveBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH, isChecked);
                    Toaster.toast("Save settings");
                    fragmentActivityListener.performFragmentActivityAction(PreferenceConstants.KEY_SNAP_TO_PATH, isChecked);
                    break;

                case R.id.showallpathToggleButton:
                    Toaster.toast("Save settings");
                    preferenceHelper.saveBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, isChecked);
                    fragmentActivityListener.performFragmentActivityAction(PreferenceConstants.KEY_SHOW_PATHS, isChecked);
                    break;

            }
        }
    };

    /**
     * Get Virtual Beacon position and add it to the view
     */
    private void addVirtualBeacon() {
        if (isMSTVirtualBeaconShown && getActivity()!=null && !Utility.isEmpty(mstVirtualBeaconArrayList) && mstVirtualBeaconArrayList.size() > 0) {

            floorplanVBLayout.setVisibility(View.VISIBLE);
            floorplanVBLayout.removeAllViews();
            for (MSTVirtualBeacon mstVirtualBeacon : mstVirtualBeaconArrayList) {

                final View view = getActivity().getLayoutInflater().inflate(R.layout.marker_layout, floorplanLayout, false);
                //view.setTag("VirtualBeacon");
                final RelativeLayout floorplanBluedotLayout = (RelativeLayout) view.findViewById(R.id.floorplanBluedotLayout);
                floorplanBluedotLayout.setTag(mstVirtualBeacon);

                view.setScaleX(this.zoomScaleFactor);
                view.setScaleY(this.zoomScaleFactor);

                // If scaleX and scaleY are not defined, check again
                if (!scaleFactorCalled && (this.scaleXFactor == 0 || this.scaleYFactor == 0)) {
                    setupScaleFactorForFloorplan();
                }

                final float xPos = (float) ((mstVirtualBeacon.getX() * this.scaleXFactor));
                final float yPos = (float) ((mstVirtualBeacon.getY() * this.scaleYFactor));

                markerViewWidth = preferenceHelper.getFloatValue("markerViewWidth");
                markerViewHeight = preferenceHelper.getFloatValue("markerViewHeight");


                if (markerViewWidth == 0 || markerViewHeight == 0) {
                    ViewTreeObserver vto = floorplanBluedotLayout.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            floorplanBluedotLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                            markerViewWidth  = floorplanBluedotLayout.getWidth() / 2;
                            markerViewHeight = floorplanBluedotLayout.getHeight() / 2;

                            preferenceHelper.saveFloatValue("markerViewWidth", markerViewWidth);
                            preferenceHelper.saveFloatValue("markerViewHeight", markerViewHeight);

                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = (int) (floorImageLeftMargin + xPos - markerViewWidth);
                            params.topMargin = (int) (floorImageTopMargin + yPos - markerViewHeight);
                            floorplanBluedotLayout.setLayoutParams(params);
                        }
                    });
                } else {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = (int) (floorImageLeftMargin + xPos - markerViewWidth);
                    params.topMargin = (int) (floorImageTopMargin + yPos - markerViewHeight);
                    floorplanBluedotLayout.setLayoutParams(params);
                }

                floorplanVBLayout.addView(view);
            }
        }else
        {
            floorplanVBLayout.setVisibility(View.GONE);
        }
    }


    /**
     * add nearest location view
     */
    private void renderNearestBluedot(MSTPoint mstPoint) {

        removeViewByTagname("renderNearestBluedot");

        if( getActivity()!=null && !Utility.isEmpty(mstPoint) && !Utility.isEmpty(this.currentMap)  ) {

            final View view = getActivity().getLayoutInflater().inflate(R.layout.nearest_marker_layout, floorplanLayout, false);
            view.setTag("renderNearestBluedot");
            view.setScaleX(this.zoomScaleFactor);
            view.setScaleY(this.zoomScaleFactor);

            // If scaleX and scaleY are not defined, check again
            if (!scaleFactorCalled && (this.scaleXFactor == 0 || this.scaleYFactor == 0)) {
                setupScaleFactorForFloorplan();
            }

           /* final float xPos = (float) ((mstPoint.getX() * this.scaleXFactor) * this.currentMap.getPpm());
            final float yPos = (float) ((mstPoint.getY() * this.scaleYFactor) * this.currentMap.getPpm());*/

            final float xPos = (float) (mstPoint.getX() ) ;
            final float yPos = (float) (mstPoint.getY() ) ;

            nearestMarkerViewWidth  = preferenceHelper.getFloatValue("nearestMarkerViewWidth");
            nearestMarkerViewHeight = preferenceHelper.getFloatValue("nearestMarkerViewHeight");


            if(nearestMarkerViewWidth==0 || nearestMarkerViewHeight ==0) {
                ViewTreeObserver vto = view.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        nearestMarkerViewWidth = view.getWidth() / 2;
                        nearestMarkerViewHeight = view.getHeight() / 2;

                        preferenceHelper.saveFloatValue("nearestMarkerViewWidth", nearestMarkerViewWidth);
                        preferenceHelper.saveFloatValue("nearestMarkerViewHeight", nearestMarkerViewHeight);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.leftMargin = (int) (floorImageLeftMargin + xPos - nearestMarkerViewWidth);
                        params.topMargin = (int) (floorImageTopMargin + yPos - nearestMarkerViewHeight);
                        view.setLayoutParams(params);
                    }
                });
            }else
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.leftMargin = (int) (floorImageLeftMargin + xPos - nearestMarkerViewWidth);
                params.topMargin = (int) (floorImageTopMargin + yPos - nearestMarkerViewHeight);
                view.setLayoutParams(params);
            }

            floorplanLayout.addView(view);
            view.bringToFront();
        }
    }

    private void loadWayfindingData(JSONObject mapJSON) {

        if (this.graph == null) {
            this.graph = new MSTGraph();
        }
        if (this.nodes == null) {
            this.nodes = new HashMap<>();
        }

        try{

            String sCoordinate = mapJSON.optString("coordinate");
            if(!Utility.isEmptyString(sCoordinate) && sCoordinate.equals("actual"))
                isActualData = true;
            else
                isActualData = false;

            JSONArray nodesFromFile = mapJSON.getJSONArray("nodes");

            for (int i = 0 ; i < nodesFromFile.length() ; i++){
                JSONObject node = (JSONObject)nodesFromFile.get(i);
                String name = node.getString("name");
                JSONObject position = node.getJSONObject("position");
                JSONObject edges = node.getJSONObject("edges");
                double x = position.getDouble("x");
                double y = position.getDouble("y");
                this.nodes.put(name, new MSTNode(name,new MSTPoint(x,y),edges));
            }

            Iterator<Map.Entry<String, Object>> it = this.nodes.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Object> node = it.next();
                String nodeName = node.getKey();
                MSTNode mMSTNode = (MSTNode)node.getValue();
                // this.calculateEdgeDistanceForNode(mMSTNode);
                this.graph.addVertex(nodeName, mMSTNode.getEdges());
            }

            this.hasAddedWayfinding = true;
            this.wayfinder = new MSTWayfinder(mapJSON, isActualData, preferenceHelper);
        } catch (JSONException e) {
            Log.e("tag","Encountered JSONException: " + e.getLocalizedMessage());
        }

    }


    public void addIndoorMap(MSTMap mstMap) {

        // If the map type is an image.
        if (mstMap != null && mstMap.getMapType() == MapType.IMAGE) {

            // If the currentMap has never been set.
            // If the currentMap is not equal to the new map from LE.
            if (this.currentMap == null || !this.currentMap.getMapId().equals(mstMap.getMapId())) {

                Toaster.toast("Render floorplan image");

                // Set the current map
                this.currentMap = mstMap;
                isNewPath = true;
                mapName = mstMap.getMapName();

                loadMap();


            }
        }
    }

    private void loadMap()
    {
        if(this.currentMap!=null )
        {
            try
            {
                modelLoader.get(this.currentMap.getMapImageUrl(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {

                        BitmapDrawable rBitamp = imageContainer.getBitmap();
                        if (rBitamp != null) {
                            floorplanImageView.setImageBitmap(rBitamp.getBitmap());
                            if (rBitamp.getBitmap().isRecycled())
                                rBitamp.getBitmap().recycle();
                            rBitamp = null;

                            addedMap = true;

                            // Defining the scaleX and scaleY for the map image
                            if (!scaleFactorCalled)
                                setupScaleFactorForFloorplan(new Callback() {

                                    /**
                                     * Once everything is scaled, render the bluedot.
                                     */
                                    @Override
                                    public void onComplete() {
                                        new wayfinerAsyncTask().execute();
                                    }
                                });
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        floorplanImageView.setImageDrawable(null);
                    }
                });
            } catch (OutOfMemoryError oome) {
                VolleyLog.e("OutOfMemoryError in performRequest");
            }
        }
    }


    /**
     * Once the floorplan is been drawn, compute the x, y scale factors.
     */
    private void setupScaleFactorForFloorplan() {

        ViewTreeObserver vto = this.floorplanImageView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                floorplanImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                floorImageLeftMargin = floorplanImageView.getLeft();
                floorImageTopMargin = floorplanImageView.getTop();
                scaleFactorCalled = false;
                scaleXFactor = (floorplanImageView.getWidth() / (double) floorplanImageView.getDrawable().getIntrinsicWidth());
                scaleYFactor = (floorplanImageView.getHeight() / (double) floorplanImageView.getDrawable().getIntrinsicHeight());
            }
        });
    }

    /**
     * Once the floorplan is been drawn, compute the x, y scale factors.
     */
    private void setupScaleFactorForFloorplan(final Callback cb) {
        if (scaleXFactor == 0 || scaleYFactor == 0) {
            scaleFactorCalled = true;
            ViewTreeObserver vto = this.floorplanImageView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floorplanImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    floorImageLeftMargin = floorplanImageView.getLeft();
                    floorImageTopMargin = floorplanImageView.getTop();
                    scaleFactorCalled = false;
                    scaleXFactor = (floorplanImageView.getWidth() / (double) floorplanImageView.getDrawable().getIntrinsicWidth());
                    scaleYFactor = (floorplanImageView.getHeight() / (double) floorplanImageView.getDrawable().getIntrinsicHeight());
                    cb.onComplete();
                }
            });
        } else {
            cb.onComplete();
        }
    }

    /**
     * Render the bluedot based on relative location.
     *
     * @param mstPoint
     */
    public void renderBluedot(MSTPoint mstPoint) {

        if (this.floorplanImageView != null &&
                this.floorplanImageView.getDrawable() != null &&
                this.currentMap != null && mstPoint != null
                ) {

            float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
            float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());
            this.mstPoint = mstPoint;
            if ( this.scaleXFactor == 0 || this.scaleYFactor ==0)
            {
                // Defining the scaleX and scaleY for the map image
                if (!scaleFactorCalled)
                    setupScaleFactorForFloorplan(new Callback() {
                        /**
                         * Once everything is scaled, render the bluedot.
                         */
                        @Override
                        public void onComplete() {
                            new wayfinerAsyncTask().execute();
                        }
                    });

                return;
            }


            setStartingPoint(mstPoint);
            if (this.floorplanBluedotView.getAlpha() == 0.0) {
                this.floorplanBluedotView.setAlpha((float) 1.0);
            }

            float leftMargin = floorImageLeftMargin + (xPos - (this.floorplanBluedotView.getWidth() / 2));
            float topMargin  = floorImageTopMargin + (yPos - (this.floorplanBluedotView.getHeight() / 2));

            this.floorplanBluedotView.setX(leftMargin);
            this.floorplanBluedotView.setY(topMargin);

            showXYMstPoint = new MSTPoint(leftMargin, topMargin );

            if(!hasAddedWayfinding)
                getWayFindingData();

            /*if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
                removeViewByTagname("show_path_view");
                removeViewByTagname("edgesPointLayout");
                isNewPath = true;
            }else*/
            renderShowPath();

            if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_PATHS, false)) {
                hideView("show_path_view");
                hideView("edgesPointLayout");
            }else
            {
                visibleView("show_path_view");
                visibleView("edgesPointLayout");
            }

            if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SHOW_BLUEDOT, true))
            {
                removeViewByTagname("motionLayout");
                preHasMotionPoint =null;
                this.floorplanBluedotView.setAlpha((float) 0.0);
            }
            else {

                this.floorplanBluedotView.setAlpha((float) 1.0);
                if ( preferenceHelper.getIntValue(PreferenceConstants.KEY_BREAD_CRUMB)>0) {
                    if(mstPoint.isHasMotion()) {
                        if (preHasMotionPoint != null)
                            addMotionViews(preHasMotionPoint);
                        preHasMotionPoint = mstPoint;
                        bluedot_flashlight_image.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffd942")));
                    }else
                    {
                        bluedot_flashlight_image.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0085c3")));
                    }
                } else {
                    removeViewByTagname("motionLayout");
                    bluedot_flashlight_image.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0085c3")));
                }
            }

            if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, false) &&
                    this.hasAddedWayfinding && this.isWayfindingAdded && this.endingPoint != null) {
                renderWayfinding();
            }else
            {
                if(this.isWayfindingAdded) {
                    removeViewByTagname("wayfindingpath");
                    removeViewByTagname("snapPathDestinationView");
                    removeViewByTagname("renderNearestBluedot");
                    isWayfindingAdded = false;
                }
                renderSnapToPath();
            }

        }else if (this.floorplanImageView != null &&
                this.floorplanImageView.getDrawable() == null &&
                this.currentMap != null){
            loadMap();
        }
    }

    // Hide VIEW
    private void hideView(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if (wayfindingLineView != null)
            wayfindingLineView.setVisibility(View.GONE);
    }

    // Visible VIEW
    private void visibleView(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if (wayfindingLineView != null)
            wayfindingLineView.setVisibility(View.VISIBLE);
    }

    private void renderSnapToPath() {
        // ADD SHOW PATH VIEW

        if(hasAddedWayfinding && mstPoint!=null) {

            this.wayfinder.setStartingPoint(mstPoint);

            String startingName = this.wayfinder.getNearestPositionName(this.startingPoint, this.currentMap);

            removeViewByTagname("renderNearestBluedot");

            ArrayList<MSTPath> pathArrayList = this.wayfinder.getPathArrayList();
            MSTPoint nearestMstPoint = null;
            if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH, true)) {
                float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
                float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());
                nearestMstPoint = this.wayfinder.getSnapPathPosition(xPos, yPos, pathArrayList, startingName );
                renderNearestBluedot(nearestMstPoint);
            }


        }else
            removeViewByTagname("renderNearestBluedot");
    }

    private void addMotionViews(MSTPoint mstPoint) {

        if(getActivity()==null)
            return;

        View view1 = floorplanLayout.findViewWithTag("motionLayout");
        RelativeLayout motionLayout;
        if(view1==null) {
            motionLayout = new RelativeLayout(getActivity());
            motionLayout.setTag("motionLayout");
            RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            lineParams.topMargin  = (int) floorImageTopMargin;
            lineParams.leftMargin = (int) floorImageLeftMargin;
            motionLayout.setLayoutParams(lineParams);
            floorplanLayout.addView(motionLayout);
        }else
            motionLayout = (RelativeLayout) view1;

        if(mstPoint!=null)
        {
            View view = new View(getActivity());
            view.setBackgroundResource(R.drawable.motion_dot_bg);

            float xPos = this.convertCloudPointToFloorplanXScale(mstPoint.getX());
            float yPos = this.convertCloudPointToFloorplanYScale(mstPoint.getY());

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(12, 12);
            params.leftMargin = (int) ((xPos - 6));
            params.topMargin  = (int) ((yPos - 6));

            view.setLayoutParams(params);

            motionLayout.addView(view);
        }

        int childCount = motionLayout.getChildCount();
        int breadCrumb = preferenceHelper.getIntValue(PreferenceConstants.KEY_BREAD_CRUMB);

        if(breadCrumb==1 && childCount>50)
        {
            for(int i=0; i<childCount-50;i++)
            {
                motionLayout.removeViewAt(i);
            }

        }
    }

    private void getWayFindingData()
    {
        if(currentMap==null)
            return;

        //JSONObject wayfindingPath = FloorplanFragment.this.currentMap.getWayfindingPath();

        JSONObject wayfindingPath = null;

        if(!Utility.isEmptyString(FloorplanFragment.this.currentMap.getWayfindingPath()))
        {
            try {
                wayfindingPath = new JSONObject(FloorplanFragment.this.currentMap.getWayfindingPath());
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        if (wayfindingPath != null){

            loadWayfindingData(wayfindingPath);
        } else {
            Log.w("Mist", "Wayfinding path is set");
        }



    }

    private void setStartingPoint(MSTPoint mstPoint) {
        this.startingPoint = mstPoint;
        if (wayfinder != null)
            this.wayfinder.setStartingPoint(this.startingPoint);
    }

    private void setDestinationPoint(MSTPoint mstPoint) {
        this.endingPoint = mstPoint;
        if (wayfinder != null)
            this.wayfinder.setDestinationPoint(this.endingPoint);
    }

    private boolean hasPathChanged(ArrayList<String> pathArr) {
        ArrayList<String> copyArr = new ArrayList<>(pathArr);
        Collections.reverse(copyArr);

        if (this._previousPathArr.size() != copyArr.size()) {
            return true;
        }

        // Compare each of the value from path array to make sure they're identical.
        // If any of the values match return false.
        for (int i = 0; i < this._previousPathArr.size(); i++) {
            String currentNodeNameAtIndex = (String) this._previousPathArr.get(i);
            String newNodeNameAtIndex = (String) copyArr.get(i);
            if (!currentNodeNameAtIndex.equals(newNodeNameAtIndex)) {
                return true;
            }
        }
        return false;
    }

    private void renderWayfinding() {

        // ADD WAYFINDING PATH VIEW
        if (getActivity()!=null && preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, false) &&
                this.hasAddedWayfinding && this.mstPoint != null && this.endingPoint != null) {

            this.wayfinder.setStartingPoint(mstPoint);
            isWayfindingAdded = true;

            if (renderWayfindingAsyncTask != null && renderWayfindingAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
            {
                renderWayfindingAsyncTask.cancel(true);
            }

            renderWayfindingAsyncTask = null;
            renderWayfindingAsyncTask = new RenderWayfindingAsyncTask();
            renderWayfindingAsyncTask.execute();


        } else if (!preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, false)) {
            removeViewByTagname("wayfindingpath");
            hideView("edgesPointLayout");
            removeViewByTagname("snapPathDestinationView");
            removeViewByTagname("renderNearestBluedot");
        }
    }

    private void addSnapPathDestinationPoint(MSTPoint mstPoint)
    {
        if(mstPoint!=null && getActivity()!=null) {
            View snapPathDestinationView = new View(getActivity());
            snapPathDestinationView.setTag("snapPathDestinationView");
            snapPathDestinationView.setScaleX(this.zoomScaleFactor);
            snapPathDestinationView.setScaleY(this.zoomScaleFactor);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 30);
            snapPathDestinationView.setBackgroundResource(R.drawable.snap_destination_pointer);

            if (!isActualData) {
                params.leftMargin = (int) (floorImageLeftMargin +(mstPoint.getX() * scaleXFactor * currentMap.getPpm()) - 15);
                params.topMargin = (int) (floorImageTopMargin +(mstPoint.getY() * scaleYFactor * currentMap.getPpm()) - 15);
            } else {
                params.leftMargin = (int) (floorImageLeftMargin + (mstPoint.getX() * scaleXFactor) - 15);
                params.topMargin = (int) (floorImageTopMargin +(mstPoint.getY() * scaleYFactor) - 15);
            }

            snapPathDestinationView.setLayoutParams(params);
            floorplanLayout.addView(snapPathDestinationView);
        }
    }


    private void addEdges(ArrayList<MSTEdges> edgesArrayList)
    {
        RelativeLayout edges = new RelativeLayout(getActivity());
        edges.setTag("edgesPointLayout");
        RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lineParams.topMargin = (int) floorImageTopMargin;
        lineParams.leftMargin = (int) floorImageLeftMargin;
        edges.setLayoutParams(lineParams);
        for(MSTEdges mstEdges: edgesArrayList)
        {
            MSTPoint mstPoint = mstEdges.getMstPoint();
            View view = new View(getActivity());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(12, 12);
            view.setBackgroundColor(Color.parseColor("#5699f6"));
            if(!isActualData)
            {
                params.leftMargin = (int) ((mstPoint.getX() * scaleXFactor * currentMap.getPpm()) - 6);
                params.topMargin = (int) ((mstPoint.getY() * scaleYFactor * currentMap.getPpm()) - 6);
            }else {
                params.leftMargin = (int) ((mstPoint.getX() * scaleXFactor) - 6);
                params.topMargin = (int) ((mstPoint.getY() * scaleYFactor) - 6);
            }
            view.setLayoutParams(params);

            mstEdges.setMstScreenPoint(new MSTPoint(params.leftMargin+floorImageLeftMargin, params.topMargin+floorImageTopMargin));

            edges.addView(view);
        }

        floorplanLayout.addView(edges);
    }

    private void renderShowPath() {
        // ADD SHOW PATH VIEW
        if(hasAddedWayfinding && mstPoint!=null) {

            removeViewByTagname("show_path_view");
            removeViewByTagname("edgesPointLayout");

            ArrayList<MSTPath> mstPathArrayList = this.wayfinder.getShowPathArrayList();

            if (mstPathArrayList!=null && getActivity()!=null) {
                ArrayList<MSTEdges> edgesArrayList = new ArrayList<>();
                edgesArrayList.addAll(this.wayfinder.getEdges());
                if(edgesArrayList!=null)
                    addEdges(edgesArrayList);

                DrawLine drawLine = new DrawLine(getActivity(), mstPathArrayList, null, null, scaleXFactor, scaleYFactor, currentMap, isActualData);
                drawLine.setTag("show_path_view");
                RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lineParams.topMargin = (int) floorImageTopMargin;
                lineParams.leftMargin = (int) floorImageLeftMargin;
                drawLine.setLayoutParams(lineParams);
                floorplanLayout.addView(drawLine);
                isNewPath = false;
            }
        }
    }

    // REMOVE THE VIEW FROM PARENT LAYOUT BY TAG NAME
    private void removeViewByTagname(String sTagName) {

        View wayfindingLineView = floorplanLayout.findViewWithTag(sTagName);
        if(wayfindingLineView != null)
            floorplanLayout.removeView(wayfindingLineView);
    }

    /**
     * Takes the cloud points in meters and convert this into floorplan image scale using ppm and device display units
     *
     * @param meter
     * @return
     */
    private float convertCloudPointToFloorplanXScale(double meter){
        return (float)(meter*this.scaleXFactor*this.currentMap.getPpm());
    }

    private float convertCloudPointToFloorplanYScale(double meter){
        return (float)(meter*this.scaleYFactor*this.currentMap.getPpm());
    }

    public void drawTouchedDot(float x, float y) {

        if(this.currentMap!=null  && preferenceHelper.getBooleanValue(PreferenceConstants.KEY_ENABLE_WAYFINDING, false) ) {
          /*  float pointX = (float) (x / (this.scaleXFactor * this.currentMap.getPpm()));
            float pointY = (float) (y / (this.scaleYFactor * this.currentMap.getPpm()));
*/
            setDestinationPoint(new MSTPoint(x, y));
            renderWayfinding();
        }
    }

//    private float getNormalizeFloorplanPosition(){
//
//    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onTouchZoomView(float x, float y) {
        FloorplanFragment.this.drawTouchedDot(x, y);
        fragmentActivityListener.performFragmentActivityAction("close_vble_notification", null);
    }

    @Override
    public void onZoomScaleValue(float scale) {
        float scale1 = 0;
        if(scale<=1.5)
            scale1 = 1;
        else
        {
            if(scale>3)
            {
                scale1 = (float) 0.3;
            }else if(scale>=1.5)
                scale1 = (float) 0.5;
            else
                scale1 = (float) 0.8;
        }

        this.zoomScaleFactor = scale1;

        View view1 = floorplanLayout.findViewById(R.id.floorplan_bluedot);
        View view2 = floorplanLayout.findViewWithTag("snapPathDestinationView");
        View view3 = floorplanLayout.findViewWithTag("renderNearestBluedot");
        View view4 = floorplanLayout.findViewWithTag("wayfindingpath");

        setScaleValue(view1, scale1);
        setScaleValue(view2, scale1);
        setScaleValue(view3, scale1);
        setScaleValue(view4, scale1);
        // setScaleValue(view5, scale1);


        int childcount = floorplanVBLayout.getChildCount();
        for (int i=0; i < childcount; i++){
            View v = floorplanVBLayout.getChildAt(i);
            setScaleValue(v, scale1);
        }
    }

    private void setScaleValue(View view, float scale)
    {
        if(view!=null && view.getVisibility()== View.VISIBLE)
        {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }


    private class wayfinerAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            if(!hasAddedWayfinding)
                getWayFindingData();
            if(hasAddedWayfinding && wayfinder!=null)
            {
                FloorplanFragment.this.wayfinder.getShowPathList(nodes, scaleXFactor, scaleYFactor, currentMap);
                FloorplanFragment.this.wayfinder.drawShowPath(nodes, scaleXFactor, scaleYFactor, currentMap);
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            renderBluedot(FloorplanFragment.this.mstPoint);
            addVirtualBeacon();
        }
    }

    private class RenderWayfindingAsyncTask extends AsyncTask<String, Void, String> {
        DrawLine drawLine;
        String startingName,   endingName;
        ArrayList<String> pathArr;
        ArrayList<MSTPath> pathArrayList;
        MSTPoint nearestMstPoint = null;
        MSTPoint snapPathMstPoint;

        @Override
        protected String doInBackground(String... urls) {
           /* startingName = FloorplanFragment.this.wayfinder.getNearestPositionName(FloorplanFragment.this.startingPoint, FloorplanFragment.this.currentMap);
            endingName = FloorplanFragment.this.wayfinder.getNearestPositionName(FloorplanFragment.this.endingPoint, FloorplanFragment.this.currentMap);
*/
            startingName = FloorplanFragment.this.wayfinder.getNearestPositionName(FloorplanFragment.this.showXYMstPoint);
            endingName = FloorplanFragment.this.wayfinder.getNearestPositionName(FloorplanFragment.this.endingPoint);


            System.out.println("wayfinding startingPoint: " + startingName + "," + "endingPoint: " + endingName);
            pathArr = FloorplanFragment.this.graph.findPathFrom(startingName, endingName);
            if (FloorplanFragment.this._previousPathArr == null || (pathArr.size() != 0 && FloorplanFragment.this.hasPathChanged(pathArr))) {

                if (!scaleFactorCalled && (FloorplanFragment.this.scaleXFactor == 0 || FloorplanFragment.this.scaleYFactor == 0)) {
                    setupScaleFactorForFloorplan();
                }

                FloorplanFragment.this._previousPathArr = pathArr;

                pathArrayList = FloorplanFragment.this.wayfinder.drawWayfingPath( pathArr, scaleXFactor, scaleYFactor, currentMap);

                if (preferenceHelper.getBooleanValue(PreferenceConstants.KEY_SNAP_TO_PATH, true)) {
                    float xPos = FloorplanFragment.this.convertCloudPointToFloorplanXScale(mstPoint.getX());
                    float yPos = FloorplanFragment.this.convertCloudPointToFloorplanYScale(mstPoint.getY());
                    ArrayList<MSTPath> pathList = FloorplanFragment.this.wayfinder.getPathArrayList();
                    nearestMstPoint = FloorplanFragment.this.wayfinder.getSnapPathPosition(xPos, yPos, pathList, startingName);
                }

                snapPathMstPoint  = FloorplanFragment.this.wayfinder.getNearestPosition(endingName);
            }



            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            if(getActivity()==null)
                return;

            System.out.println("wayfinding rendering: " + startingName + "," + "endingPoint: " + endingName);

            FloorplanFragment.this._previousPathArr = pathArr;
            removeViewByTagname("wayfindingpath");
            removeViewByTagname("snapPathDestinationView");
            removeViewByTagname("renderNearestBluedot");

            if (nearestMstPoint != null) {
                renderNearestBluedot(nearestMstPoint);
            }

            drawLine = new DrawLine(getActivity(), pathArrayList, pathArr, nearestMstPoint, scaleXFactor, scaleYFactor, currentMap, isActualData);
            if(drawLine!=null) {
                drawLine.setTag("wayfindingpath");
                RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                lineParams.topMargin = (int) floorImageTopMargin;
                lineParams.leftMargin = (int) floorImageLeftMargin;
                drawLine.setLayoutParams(lineParams);
                floorplanLayout.addView(drawLine);
            }



            if (snapPathMstPoint != null)
                addSnapPathDestinationPoint(snapPathMstPoint);

            FloorplanFragment.this.floorplanBluedotView.bringToFront();

        }
    }

}
