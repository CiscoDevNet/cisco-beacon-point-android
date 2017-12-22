package com.android.ble.sample.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.android.ble.sample.R;
import com.android.ble.sample.activity.OrgActivity;
import com.android.ble.sample.adapters.OrganizationAdapter;
import com.android.ble.sample.dialogs.AddVenueDialog;
import com.android.ble.sample.helper.EnvHelper;
import com.android.ble.sample.helper.Toaster;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.AdapterListener;
import com.android.ble.sample.listeners.DialogListener;
import com.android.ble.sample.listeners.FragmentActivityListener;
import com.android.ble.sample.model.PlaceNameModel;
import com.mist.android.MistIDGenerator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Entappiainc on 21-05-2016.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, DialogListener, AdapterListener {

    private String[] fileList;
    private String[] configFileList;

    private FragmentActivityListener fragmentActivityListener;
    ArrayList<PlaceNameModel> placeNameModelArrayList = new ArrayList<>();
    Button addOrganizationButton;
    ListView organizationListView;
    OrganizationAdapter organizationAdapter;

    public static HomeFragment newInstance(){

        HomeFragment mHomeFragment = new HomeFragment();
        Bundle mBundle = new Bundle();
        mHomeFragment.setArguments(mBundle);
        return mHomeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        addOrganizationButton = (Button)view.findViewById(R.id.addOrganizationButton);
        organizationListView = (ListView)view.findViewById(R.id.organizationListView);

        addOrganizationButton.setOnClickListener(this);
        addPlaceNames();
        organizationAdapter = new OrganizationAdapter(getActivity(), placeNameModelArrayList);
        organizationListView.setAdapter(organizationAdapter);
        organizationListView.setOnItemClickListener(onItemClickListener);
        organizationAdapter.notifyDataSetChanged();

        return view;
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                PlaceNameModel placeNameModel = placeNameModelArrayList.get(position);
                performAdapterAction("gotoMap_event", placeNameModel);
            }else {
                showAlert();
            }

        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity()!=null)
            new getPlaceNamesAsyncTask().execute();
    }


    private void getFileList() {
        if(getActivity()==null)
            return;

        File[] fileList = getActivity().getApplicationContext().getFilesDir().listFiles();
        this.fileList = new String[fileList.length];
        for (int i = 0; i < fileList.length; i++) {
            this.fileList[i] = fileList[i].getName();
        }
    }

    public void addMultipleOrgs(String sOrg)
    {
        if(!Utility.isEmptyString(sOrg))
        {
            String deviceID = MistIDGenerator.getMistUUID(getActivity());
            String[] orgArarry = sOrg.split(",");
            for(String orgValue : orgArarry)
            {
                String[] params = new String[2];
                if(orgValue.startsWith("P"))
                    params[0] = "https://api.mist.com/api/v1/mobile/verify/"+orgValue;
                else if(orgValue.startsWith("S") || orgValue.startsWith("D"))
                    params[0] = "https://api.mistsys.com/api/v1/mobile/verify/"+orgValue;

                params[1] = deviceID;
                callEnrollDeviceHttpAsyncTask(params, false);
            }
        }
    }


    private void addPlaceNames( )
    {
        getFileList();
        placeNameModelArrayList.clear();

        try{
            // add PRODUCTION Places
            if(!Utility.isEmpty(fileList) && fileList.length>0) {
                for (String name : this.fileList) {
                    //read the file and get the name from inside the file
                    if (name.contains("config")&& getActivity()!=null ) {
                        try{
                            JSONObject obj = new JSONObject(getOrgContentFromFile(name));
                            String env = obj.getString("env");
                            if(env.equals("Production"))
                            {
                                placeNameModelArrayList.add(new PlaceNameModel((String) obj.get("name"), name, "PRODUCTION", obj));
                            }
                            else if(env.equals("Staging"))
                            {
                                placeNameModelArrayList.add(new PlaceNameModel((String) obj.get("name"), name, "STAGING",  obj));
                            }
                            else if(env.equals("Dev"))
                            {
                                placeNameModelArrayList.add(new PlaceNameModel((String) obj.get("name"), name, "DEV", obj));
                            }

                        } catch (JSONException e) {
                            Log.e("JSONException",e.getMessage());
                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.e("Exception",e.getMessage());
        }finally {
            if(organizationAdapter!=null)
                organizationAdapter.notifyDataSetChanged();
        }


    }

    private String getNameFromFile(String file) {
        String name = null;
        try{
            JSONObject org = new JSONObject(this.getOrgContentFromFile(file));
            return (String) org.get("name");
        } catch (JSONException e) {
            System.out.println("JSONException: " + e.getMessage());
            return name;
        }
    }

    private String getOrgContentFromFile(String filename){
        String content = null;
        try {
            InputStream inputStream = getActivity().openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                content = bufferedReader.readLine();
                bufferedReader.close();
                inputStream.close();
                return content;
            }
        } catch (FileNotFoundException e) {
            Log.e("Mist Error", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Mist Error", "Can not read file: " + e.toString());
        }
        return content;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.addOrganizationButton:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AddVenueDialog addVenueDialog = new AddVenueDialog();
                addVenueDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Transparent );
                addVenueDialog.show(fragmentManager, "DIALOG_PROGRESS_VIEW");
                break;
        }
    }


    public void callEnrollDeviceHttpAsyncTask(String[] params, boolean isFirstTime)
    {
        if(Utility.isNetworkAvailable(getActivity()))
            new EnrollDeviceHttpAsyncTask(getActivity(), isFirstTime).execute(params);
        else
        {
            Toaster.toast("No network connection");
        }
    }

    @Override
    public void performAdapterAction(String tagName, Object data) {
        if(tagName.equals("viewStore_event"))
        {

        }else if(tagName.equals("gotoMap_event"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    PlaceNameModel placeNameModel = (PlaceNameModel) data;
                    Intent intent = new Intent(getActivity(), OrgActivity.class);
                    intent.putExtra("placeNameModel", placeNameModel);
                    startActivity(intent);
                }else
                {
                    fragmentActivityListener.performFragmentActivityAction("check_location_permission", true);
                }
            }else {
                PlaceNameModel placeNameModel = (PlaceNameModel) data;
                fragmentActivityListener.performFragmentActivityAction("load_org_activity", placeNameModel);
            }

        }
    }

    @Override
    public void performDialogAction(String tagName, Object data) {

    }

    private class getPlaceNamesAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toaster.toast("Enrolling Device, Please wait");
        }

        @Override
        protected String doInBackground(String... urls) {
            addPlaceNames();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }


    private class EnrollDeviceHttpAsyncTask extends AsyncTask<String, Void, String> {
        String url = null;
        ProgressDialog progress = null;
        boolean isFirstTime;

        public EnrollDeviceHttpAsyncTask(Context mContext, boolean isFirstTime) {
            this.isFirstTime = isFirstTime;

            progress = new ProgressDialog(mContext);
            progress.setMessage("Enrolling Device, Please wait");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toaster.toast("Enrolling Device, Please wait");
        }

        @Override
        protected String doInBackground(String... urls) {
            url = urls[0];
            String deviceID = urls[1];
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("device_id",deviceID);
                StringEntity jsonEntity;
                try{
                    jsonEntity = new StringEntity(jsonObject.toString());
                } catch (UnsupportedEncodingException e){
                    throw new RuntimeException(e);
                }

                HttpPost post = new HttpPost(url);
                post.setEntity(jsonEntity);
                post.setHeader("Content-type", "application/json");
                HttpResponse response = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    response = client.execute(post); // enroll the device
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");
                    return responseString;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            if(isFirstTime)
                fragmentActivityListener.performFragmentActivityAction("check_location_permission", true);
            if (result != null) {
                Toaster.toast("Received credential");
                JSONObject resultJSON;
                try {
                    resultJSON = new JSONObject(result);
                    if (resultJSON.has("secret") && resultJSON.has("org_id") && resultJSON.has("name"))
                        createConfigFile(resultJSON.getString("name"), resultJSON.getString("org_id"), resultJSON.getString("secret"), url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    addPlaceNames();
                }
            } else {
                Toaster.toast("Did not received credentials");
            }
        }
    }

    private void createConfigFile(String name, String orgId, String secret, String url) {
        String fileToDelete = "config_dont_export_" + name.replace(" ", "_").replace("'s", "").replace("/", "_").toLowerCase() + ".txt";

        try{
            JSONObject org = new JSONObject();

            // get the environment and topic from the given url
            String env = EnvHelper.getEnvForURL(url);
            String topic = EnvHelper.getTopicForEnv(env);

            org.put("name",name);
             org.put("env",env);
            org.put("venueID",orgId);
            org.put("venuePassword",secret);
            org.put("host",name);
             org.put("topic",topic);

            System.out.println("Work? " + org.toString());

            String data = org.toString();

            System.out.println("Data stored :" + data);
            String filename = "config_dont_export_" + name.replace(" ", "_").replace("/", "_").replace("'s", "").toLowerCase() + ".txt";

            File dir = getActivity().getFilesDir();
            File file = new File(dir, fileToDelete);
            file.delete();

            file = new File(dir, filename);
            file.delete();

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());

                Toaster.toast("Configuration failed to add. Cannot write file.");
            }

            if (this.configFileList == null) {

                getFileList();

                ArrayList<String> nameList = new ArrayList<String>();
                ArrayList<String> filenameList = new ArrayList<String>();
                for (String nameFile : this.fileList) {
                    //read the file and get the name from inside the file
                    if (nameFile.contains("config")) {
                        filenameList.add(nameFile);
                        nameList.add(getNameFromFile(nameFile));
                    }
                }

                final String[] filenames = filenameList.toArray(new String[filenameList.size()]);

                this.configFileList = new String[filenames.length];
                this.configFileList = filenames;
            }

            ArrayList<String> files = new ArrayList<String>(Arrays.asList(this.configFileList));

            if (!files.contains(filename)) {
                files.add(0, filename);
                this.configFileList = new String[files.size()];
                this.configFileList = files.toArray(this.configFileList);
            }

            Toaster.toast("Configuration Added.");
        } catch (JSONException e) {
            Log.e(this.getClass().toString(),"JSONException: " + e.toString());
            Toaster.toast("Configuration failed to add. JSON is invalid or doesn't exist");
        }
    }

    private void showAlert() {

        String sTitle, sButton;
        sTitle = "Blue Tooth is disabled in your device. Would you like to enable it?";
        sButton = "Goto Settings Page To Enable Blue Tooth";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(sTitle)
                .setCancelable(false)
                .setPositiveButton(sButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Intent intentOpenBluetoothSettings = new Intent();
                                intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                                startActivity(intentOpenBluetoothSettings);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}
