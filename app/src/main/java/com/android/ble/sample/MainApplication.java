package com.android.ble.sample;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.android.ble.sample.helper.Toaster;
import com.mist.android.AppMode;
import com.mist.android.MSTCentralManager;
import com.mist.android.MSTCentralManagerListener;
import com.mist.android.model.AppModeParams;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class MainApplication extends Application {


    protected MSTCentralManager mstCentralManager;
    private MSTCentralManagerListener mstCentralManagerListener;


    private Activity mainActivity;

    public MainApplication() {
        mainActivity = null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        Toaster.init(getApplicationContext());
    }

    public void setMainActivity(Activity activity) {
        this.mainActivity = activity;
    }

    public void handleConnect(String venueId, String venuePassword, String environment, MSTCentralManagerListener mstCentralManagerListener, String hostname){
        this.mstCentralManagerListener = mstCentralManagerListener;
        mstCentralManager = new MSTCentralManager(this, venueId, venuePassword, this.mstCentralManagerListener);
        mstCentralManager.setHostname(hostname);
        mstCentralManager.setEnvironment(environment);
        	/* Second parameter is how long to send before resting. Default is 1 min
			Third parameter is how long to rest before sending again. Default is 10 mins
			To just enable the background mode and use the default values, one can use above or do mstCentralManager.setBackgroundParams(true, 0, 0);
		*/
        mstCentralManager.setBackgroundParams(true, 1, 5);
        mstCentralManager.start();
    }

    public void handleDisconnect(){
        if(mstCentralManager!=null)
            this.mstCentralManager.stop();
    }


    public void setAppMode(AppModeParams appMode)
    {
        if(this.mstCentralManager!=null)
            this.mstCentralManager.setAppMode(appMode);
    }

    public void saveClientInformation(String clientName) {
        this.mstCentralManager.saveClientInformation(clientName);
    }

}
