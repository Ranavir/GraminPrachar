package com.stl.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

public class PowerConnectionReceiver2  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
    	
    	//int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        //boolean isCharging = status == BatteryManager.BATTERY_PLUGGED_AC || status == BatteryManager.BATTERY_PLUGGED_USB;
                //BATTERY_STATUS_CHARGING; || status == BatteryManager.BATTERY_STATUS_FULL;
        /*
         * int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1); 
         * boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB; 
         * boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
         */
 
    	/*
        int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    	//isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
        boolean isCharging = batteryStatus == BatteryManager.BATTERY_PLUGGED_AC || batteryStatus == BatteryManager.BATTERY_PLUGGED_USB;
        
        Toast.makeText(context, "batteryStatus : " + batteryStatus + "\nCharging : " + isCharging, Toast.LENGTH_SHORT).show();
     	*/
    
    	        
        
        if (intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
        	Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
        	
        	AndroidBuildingMusicPlayerActivity.closeActivity();        	
        	
        	Utils.androidBuildingMusicPlayerActivity = context;
    		Utils.SERVER_URL = Utils.getHostURL(context);
    		
    		/****** For Start Activity *****/
    		Intent i = new Intent(context, SplashScreen.class);  
            //i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
        	Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show();        	
        	AndroidBuildingMusicPlayerActivity.closeActivity(); 
        	
        }
    
    }

}