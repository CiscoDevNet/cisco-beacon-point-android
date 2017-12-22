![](http://www.ciscolive.com/global/wp-content/uploads/cisco-logo-blue.png)



#Releases:
- 11/29/17 - Released MistSDK v1.4.1
- 11/21/17 - Released MistSDK v1.4.0
- 10/10/17 - Released MistSDK v1.3.1
- 08/29/17 - Released SDK v1.3.0
- 07/24/17 - Released SDK v1.2.0
- 10/06/16 - Released Sample App v1.0 and SDK v1.0


|  Contents |  Description | Status |
|---|---|---|
|  /app/libs/sdk_framework.aar | The core framework to start receiving location data using Cisco technology. | Current |
|  app | The source code for a Sample app that can get you started with indoor location - including blue dot, snap to path, notifications and way finding. | Current |
 

## Getting Started ##
This is a sample Android mobile app that demonstrates indoor bluedot and indoor way finding experience using Cisco's latest bluetooth Beacon Point - AIR-VBLE1-K9. Before we start using this app, you need to create an account at Cisco CMX Cloud https://cmxcloud.cisco.com installed the Beacon Points (BPs) at your indoor venue, configured them properly and integrate them with the Cisco CMX Cloud. You need to setup some paths and some virtual beacons at your indoor venue using the Cisco CMX Cloud. The aim of this app, is to track your device running this app inside your indoor venue. More details on this will be added soon. Once you have created the organization at Cisco CMX Cloud you will have a QR Code generated for your organization that will be used by the mobile app to connect to this organization.

## How to get started with this sample app
•	Checkout this project 

•	Open the project using ‘Open an existing Android Studio project’ option and selecting the checkout folder.

<img src="http://gdurl.com/TFQi" height="30%" width="30%">

	
•	When project get loaded you can see your project structure something like below.

<img src="http://gdurl.com/giUZ" height="30%" width="30%">
	
•	Now just run the app to connected device using that green play button.

•	Please provide all the permission needed for the app to get started

•	Now you can use ‘Add Organization’ Button followed by ‘QR Code’ button to scan the QR code of your organization to add your organization.

•	If QR code was correct you will see the entry of the environment something like this

<img src="http://gdurl.com/Mgxg" height="30%" width="30%">
	
•	You can click on the configuration added in this case ‘jag test’. For entering to your org.

•	For blue dot experience select ‘Floor Plan Demo’ option

•	You are now able to get the blue dot on your floorplan.


<img src="http://gdurl.com/ks1m" height="30%" width="30%">
 
##  Integrating Mist SDK 

   - Download Mist SDK aar from [here](https://github.com/mistsys/cisco-android/blob/master/app/libs/sdk_framework.aar)

  **Adding SDK AAR in App project**

	-Add downloaded aar to libs folder of app module

	-Add following in build.gradle of app module.
	
``` 
	repositories {
   	   flatDir {
            dirs 'libs'
   	    }
	 } 
``` 
	-Add the following in dependencies of build.gradle of app module
```
	compile(name:'sdk_framework', ext:'aar')
```
	Note: ‘sdk_framework’ is the name of the mist sdk you downloaded if you changed the name, please change here also.
	
	- Build the project



**Initializing Mist SDK**
  
    	1. To initialize Mist SDK we need  Org Secret and Org Id.To get these details we have to enroll the Device with the invitation secret.
	
```	
MSTOrgCredentialsManager mstOrgCredentialsManager = new MSTOrgCredentialsManager(getApplication(), new MSTOrgCredentialsCallback({
	/**
	* @param orgName   Organization Name for the QR code
	* @param orgID     Organization ID for the QR code
	* @param orgSecret SDK Secret token to be used to connect to Mist
	* @param error     Error, if the information was not obtainable
	*/
	@Override
	public void onReceivedSecret(String orgName, String orgID, String orgSecret, String error,String envType) {
				
	}
});


mstOrgCredentialsManager.enrollDeviceWithToken(invitation_secret);
```
      	-invitation_secret : you can create one from mist portal Organization -> Mobile SDK



    	2. Initialize Mist SDK with org secret(received with above API), by creating MSTCentralManager object
	
```
MSTCentralManager mstCentralManager = new MSTCentralManager (application, orgID,orgSecret, mstCentralManagerListener);
```

    	3. Call start API of MSTCentralManager
		mstCentralManager.start();
	
**Listening to Location response from Mist SDK**
  
    	To get location information from Mist SDK you need to implement **MSTCentralManagerListener** callbacks in your application

**Major callbacks are**
   
   		/**
		* Returns updated location of the mobile client (as a point (X, Y) measured in meters from the map origin, i.e., relative X, Y)
		*/
		public void onRelativeLocationUpdated(MSTPoint relativeLocation, MSTMap[] maps, Date dateUpdated);


		/**
		* Returns updated location of the mobile client (as Latitude, Longitude – LatLng object, if the geolocation of map origin is available) 
		*/
		public void onLocationUpdated(LatLng location, MSTMap[] maps, SourceType locationSource, Date dateUpdated);


		/**
		* Returns updated map for the mobile client 
		*/
		public void onMapUpdated(MSTMap map, Date dateUpdated);


		/**
		* Returns the notification received that is targetted for the mobile client
		*/
		public void onNotificationReceived(Date dateReceived, String message);


		/**
		* Returns the Arraylist of beacons that were found in the region.
		*/
		public void onBeaconDetected(MSTBeacon[] beaconArray, String region, Date dateUpdated);


		/**
		* Returns the HashMap of beacons with UUID as the key, that are found in the organization
		*/
		public void onBeaconListUpdated(HashMap<String, HashMap<Integer, Integer[]>> beaconList, Date dateUpdated);
 
  		/**
		*Returns updated pressure when available to the app. The pressure will be a double measured in millibars
		*/
		public void onPressureUpdated(double pressure, Date dateUpdated);
        
        /**
        *Returns the JSONArray of beacons that were heard in the second
        */
        public void onBeaconDetected(JSONArray jsonArray, Date date);

**MSTPoint :**

	 double x    		    : relative x
	 double y    		    : relative y
	 boolean hasMotion   	    : is user in motion
	 MSTPointType mstPointType  : type Cloud,LastKnown,Device
	 double latency    	    : latency in network
	 double heading             : compass heading
	 boolean headingFlag        : availability of compass heading
	 
		
**Other callbacks are**

		/**
		* Returns an array of virtual beacons associated with the current map
		*/
		public void onVirtualBeaconListUpdated(MSTVirtualBeacon[] virtualBeacons, Date dateUpdated);

		/**
		*
		* @param clientName Client name that was updated
		*/
		public void onClientInformationUpdated(String clientName);


		/**
		* Notifies the host application about the state of the MSTCentralManager status
		*/
		public void receivedLogMessageForCode(String message, MSTCentralManagerStatusCode code);

		/**
		* Notifies the host application about the verbose state of the MSTCentralManager status
		*/
		public void receivedVerboseLogMessage(String message);

		/**
		* Notifies the host application about any errors encountered
		*/
		public void onMistErrorReceived(String message, Date date);

		/**
		* Notifies the host application about any actions that need to be taken
		*/
		public void onMistRecommendedAction(String message);


		/**
		* TBA
		*/
		public void onZoneStatsUpdated(MSTZone[] zones, Date dateUpdated);

		/**
		* TBA
		*/
		public void onClientUpdated(MSTClient[] clients, MSTZone[] zones, Date dateUpdated);

		/**
		* TBA
		*/
		public void onAssetUpdated(MSTAsset[] assets, MSTZone[] zones, Date dateUpdated);

		/**
		Not used , Will be removed in future
		*/
		public void onReceivedSecret(String orgName, String orgID, String sdkSecret, String error);

**Updating App State**

  	Background state:
	
		AppModeParams(AppMode mAppMode,  BatteryUsage mBatteryUsage,boolean mShouldWorkInBackground, double mSendDuration, double mRestDuration)
	
		mstCentralManager.setAppMode(new AppModeParams(AppMode.BACKGROUND,BatteryUsage.LOW_BATTERY_USAGE_LOW_ACCURACY,true,1d,5d))

 	Foreground state:
	
		AppModeParams(AppMode mAppMode, BatteryUsage mBatteryUsage)
	
		mstCentralManager.setAppMode(new AppModeParams(AppMode.FOREGROUND, BatteryUsage.HIGH_BATTERY_USAGE_HIGH_ACCURACY))
	
**Stop SDK**

	To stop receiving callbacks from mist cloud use following
		mstCentralManager.stop();
