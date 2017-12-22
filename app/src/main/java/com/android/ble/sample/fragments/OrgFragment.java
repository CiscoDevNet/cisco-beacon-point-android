package com.android.ble.sample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.ble.sample.R;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.FragmentActivityListener;
import com.android.ble.sample.model.PlaceNameModel;
import com.mist.android.MSTVirtualBeacon;

import java.util.ArrayList;

/**
 * Created by Entappiainc on 30-03-2016.
 */
public class OrgFragment extends Fragment implements View.OnClickListener  {


     PlaceNameModel placeNameModel;
    String sPlaceName, sEenvironment ;

    public Button  showFloorPlanButton, showvBeaconsButton ;
    private LinearLayout orgLinearLayout;

    FragmentActivityListener fragmentActivityListener;
    ArrayList<MSTVirtualBeacon> mstVirtualBeaconList = new ArrayList<>();

    String sSearchText = "";
    public OrgFragment() {
    }

    public static OrgFragment newInstance(PlaceNameModel placeNameModel, ArrayList<MSTVirtualBeacon> mstVirtualBeaconList ) {

        OrgFragment mOrgFragment = new OrgFragment();
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("placeNameModel", placeNameModel);
        mBundle.putSerializable("mstVirtualBeaconArrayList", mstVirtualBeaconList);
        mOrgFragment.setArguments(mBundle);
        return mOrgFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            fragmentActivityListener = (FragmentActivityListener) getActivity();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_orglayout, container, false);

        Bundle mBundle = getArguments();
        if (mBundle != null) {
            placeNameModel = (PlaceNameModel) mBundle.getSerializable("placeNameModel");
            mstVirtualBeaconList = (ArrayList<MSTVirtualBeacon>) mBundle.getSerializable("mstVirtualBeaconArrayList");
            if(!Utility.isEmpty(placeNameModel)) {
                sPlaceName    = placeNameModel.getsPlaceName();
                sEenvironment = placeNameModel.getsHeaderName();
            }
        }


        initViews(v);
        return v;
    }

    private void initViews(View view)
    {
        orgLinearLayout = (LinearLayout) view.findViewById(R.id.orgLinearLayout);
        showFloorPlanButton = (Button) view.findViewById(R.id.showFloorPlanButton);
        showvBeaconsButton  = (Button) view.findViewById(R.id.showvBeaconsButton);

        orgLinearLayout.setOnClickListener(this);
        showFloorPlanButton.setOnClickListener(this);
        showvBeaconsButton.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.orgLinearLayout:
                fragmentActivityListener.performFragmentActivityAction("close_vble_notification", null);
                break;

            case R.id.showFloorPlanButton:
                fragmentActivityListener.performFragmentActivityAction("load_show_floor_fragment", null);
                break;

            case R.id.showvBeaconsButton:
                fragmentActivityListener.performFragmentActivityAction("load_show_vb_fragment", null);
                break;

        }
    }




}
