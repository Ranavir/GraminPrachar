package com.stl.musicplayer.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootUpReceiver extends BroadcastReceiver{
	
    @Override
    public void onReceive(Context context, Intent intent) {   
    	System.out.println("1. Boot complete...............");
    	
    	
    	if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
    		System.out.println("2. Boot complete...............");
    		Utils.androidBuildingMusicPlayerActivity = context;
    		Utils.SERVER_URL = Utils.getHostURL(context);
    		
    		/****** For Start Activity *****/
            Intent i = new Intent(context, SplashScreen.class);  
            //i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
    		
            
             
           /***** For start Service  ****/
            Intent myIntent = new Intent(context, ServiceBootComplete.class);
            context.startService(myIntent); 
            
    	}            
    }

}