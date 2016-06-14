package com.stl.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
 
public class SplashScreen extends Activity {
	public static SplashScreen splashScreenActivityInstance;
	
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 5000;
    private int versionCode = 0;  
    private String version = "";
    ProgressBar pb = null;
    Thread logoTimer = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Utils.SERVER_URL = Utils.getHostURL(getApplicationContext());
        
        splashScreenActivityInstance = SplashScreen.this; 
        
        PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);		
			//get the app version Name for display
			version = pInfo.versionName;
			//get the app version Code for checking
			versionCode = pInfo.versionCode;
		
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}		
		//display the current version in a TextView
		TextView versionText = (TextView) findViewById(R.id.applicationVersion);
		versionText.setText("Version : " + version);
 
		
		
		 
		//1st way to do the task
		pb = (ProgressBar) findViewById(R.id.progress_bar);
		logoTimer = new Thread(){
	        public void run(){
	            try{
	                int logoTimer = 0;
	                int progress = 0;
	                while (logoTimer < SPLASH_TIME_OUT){
	                    sleep(100);
	                    logoTimer = logoTimer + 100;
	                    progress += 2;
	                    pb.setProgress(progress);
	                }
	                Intent i = new Intent(SplashScreen.this, AndroidBuildingMusicPlayerActivity.class);
	                startActivity(i);
	                finish();	                
	            } catch (InterruptedException e) {
	            	e.printStackTrace();
	            }
	        }
		};
	    logoTimer.start();
    }
    
    @Override
	public void onBackPressed(){
    	if(logoTimer!=null){    		
    	    logoTimer.interrupt();
    	}    	
		finish();
	}
}