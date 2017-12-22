package com.android.ble.sample.model;

import com.mist.android.MSTVirtualBeacon;

/**
 * Created by Entappia on 26-04-2016.
 */
public class NotificationModel {

    protected String bodyMessage; // zone body or beacon body
    protected String forwardUrl;
    protected String notificationID;  // zone id or virtual beacon id
    protected boolean isBeaconNotification;
    protected MSTVirtualBeacon mstVirtualBeacon;

    public String getBodyMessage() {
        return bodyMessage;
    }

    public void setBodyMessage(String bodyMessage) {
        this.bodyMessage = bodyMessage;
    }


    public String getForwardUrl() {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }

    public boolean isBeaconNotification() {
        return isBeaconNotification;
    }

    public void setBeaconNotification(boolean beaconNotification) {
        isBeaconNotification = beaconNotification;
    }


    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public MSTVirtualBeacon getMstVirtualBeacon() {
        return mstVirtualBeacon;
    }

    public void setMstVirtualBeacon(MSTVirtualBeacon mstVirtualBeacon) {
        this.mstVirtualBeacon = mstVirtualBeacon;
    }
}
