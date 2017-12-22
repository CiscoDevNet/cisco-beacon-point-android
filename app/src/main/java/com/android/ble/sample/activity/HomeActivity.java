package com.android.ble.sample.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.android.ble.sample.R;
import com.android.ble.sample.dialogs.AddVenueDialog;
import com.android.ble.sample.fragments.HomeFragment;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.DialogListener;
import com.android.ble.sample.listeners.FragmentActivityListener;
import com.android.ble.sample.model.PlaceNameModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mist.android.MistIDGenerator;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class HomeActivity extends FragmentActivity  implements FragmentActivityListener, DialogListener {


    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Adding Crashlytics to get crash reports - Optional
        //   Fabric.with(this, new Crashlytics());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        checkAppPermission();
        setHomeFragment();
    }

    private void checkAppPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(
                        new DialogInterface.OnDismissListener() {
                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_FINE_LOCATION
                                );
                            }
                        }
                );
                builder.show();
            }
        }
    }


    private void setHomeFragment()
    {
        HomeFragment homeFragment = HomeFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contentFrame, homeFragment, "SIMPLE_FRAGMENT_CENTER_TAG").commit();
    }



    @Override
    public void performDialogAction(String tagName, Object data) {
      if(tagName.equals("qr_code_event"))
        {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Scan something");
            integrator.setOrientationLocked(true );
            integrator.setBeepEnabled(true);
            integrator.initiateScan();

        }else if(tagName.equals("secret_key_add_event"))
        {
            String sSecretKey = (String) data;
            if(!Utility.isEmptyString(sSecretKey)) {
                Fragment mainFragment = getSupportFragmentManager().findFragmentByTag("SIMPLE_FRAGMENT_CENTER_TAG");

                if (mainFragment instanceof HomeFragment) {
                    HomeFragment mHomeFragment = (HomeFragment) mainFragment;
                    mHomeFragment.addMultipleOrgs(sSecretKey);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Mist", "coarse location permission granted");

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }


    //on ActivityResult method
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(HomeActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                String contents = intent.getStringExtra("SCAN_RESULT").replaceAll("orgs/mobile/activate", "mobile/verify");

                String deviceID = MistIDGenerator.getMistUUID(HomeActivity.this);

                String[] params = new String[2];
                params[0] = contents;
                params[1] = deviceID;

                Fragment mainFragment = getSupportFragmentManager().findFragmentByTag("SIMPLE_FRAGMENT_CENTER_TAG");

                if (mainFragment instanceof HomeFragment){
                    HomeFragment mHomeFragment = (HomeFragment)mainFragment;
                    mHomeFragment.callEnrollDeviceHttpAsyncTask(params, false);
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void showAddDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddVenueDialog addVenueDialog = new AddVenueDialog();
        addVenueDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Transparent );
        addVenueDialog.show(fragmentManager, "DIALOG_PROGRESS_VIEW");
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void performFragmentActivityAction(String tagName, Object data) {

        if(tagName.equals("show_add_dialog"))
        {
            if((boolean)data) {
                showAddDialog();
            }
        }else if(tagName.equals("load_org_activity"))
        {
            PlaceNameModel placeNameModel = (PlaceNameModel) data;
            Intent intent = new Intent(HomeActivity.this, OrgActivity.class);
            intent.putExtra("placeNameModel", placeNameModel);
            startActivity(intent);
        }else if(tagName.equals("check_location_permission"))
        {
            if((boolean)data) {
                checkAppPermission();
            }
        }


    }


}
