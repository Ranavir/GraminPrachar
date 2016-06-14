package com.stl.musicplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;


public class ServiceBootComplete extends Service{
	private final String LOGTAG = "ServiceBootComplete";

	private final long mDelay = 5000;
	private final long mPeriod = 120000;
	private Timer mTimer;
	    
    private class LogTask extends TimerTask {
		public void run() {
			Log.i(LOGTAG, "Downloading Playlist after boot");
			//Toast.makeText(getApplicationContext(), "scheduled-", Toast.LENGTH_LONG).show();
			DatabaseUtil util = null;
			try {				
				if(Utils.checkiInternet(getApplicationContext())){
					TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
					mngr.getDeviceId();
					String UID = mngr.getDeviceId();
					String msg = Utils.getInputDataFromServer(UID, getApplicationContext());
					System.out.println("msg: "+ msg);
					if("SUCCESS".equals(msg) || "NODATA".equals(msg)){
						//mTimer.cancel();
					}
					
					
					//get instrunction from server and delete files from device
					Utils.getFileNames();
					
					
					//upload playing history which were failed to upload previously.
					util = new DatabaseUtil(getApplicationContext());
					util.getPlaySongDetDataToUpload(getApplicationContext());
					util.getPauseResumeDetailDataToUpload(getApplicationContext());
					util.getJackRemoveDetailDataToUpload(getApplicationContext());
					util.getVolumeDataToUpload(getApplicationContext()); 
					
					//delete already send data of previous date
					util.deleteOldPlaylistAndData();
					
					
					 
					//get flash news from server
					/*String status = Utils.getFlashNewsFromServer(UID, getApplicationContext());
					if("SUCCESS".equals(status)){
						ArrayList<HashMap<String, String>> flashNewsList = util.getDownloadedFlashNews("F");
						for(int i=0;i<flashNewsList.size();i++){
							System.out.println("News Name : " + flashNewsList.get(i).get("news_name"));
							
							new DownloadFileAsync().execute(flashNewsList.get(i).get("news_name"), flashNewsList.get(i).get("id"), UID);
						}
					}*/
					
					
				}else{
					System.out.println("wait internet connection to activated");
				}				
			} catch (Exception e) {
				System.out.println("ERROR: "+ e);
				e.printStackTrace();
				//mTimer.cancel();
			}finally{
				if(util!=null){
					util.close();
				}
			}
		}
	}
    
    private LogTask mLogTask;
    
	@Override
	public IBinder onBind(Intent arg0) { 
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();		
		System.out.println("1. Service class call...............");
		
		Log.i(LOGTAG, "created");
		mTimer = new Timer();
		mLogTask = new LogTask();

	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(final Intent intent, final int startId) {
		
		super.onStart(intent, startId);
		Log.i(LOGTAG, "started");
		mTimer.schedule(mLogTask, mDelay, mPeriod);
		
		//download media mp3 files from server
    	new DownloadMediaFileTask(getApplicationContext()).execute("");
    	
	}
	
	
	class DownloadFileAsync extends AsyncTask<String, String, String> {
		   
		boolean downloaded = false;
		String flashNewsPath = null;
		long id;
		String flashNews = null;
		String imei = null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... aurl) {			
			
			try{
				System.out.println("downloading...............");
				String path = Environment.getExternalStorageDirectory().toString() + "/FlashNews";
				File dir = new File(path);
				if(!dir.exists())
					dir.mkdirs();
				flashNews = aurl[0];
				id = Long.parseLong(aurl[1]);
				imei = aurl[2];
				
				flashNewsPath = path + "/" + flashNews;
				File file = new File(flashNewsPath);
				URI uri = null;    	
				try {
					DatabaseUtil util1 = new DatabaseUtil(getApplicationContext());
		        	JSONObject obj = util1.getHostDetails();
		        	String host = obj.getString("ip_address");
		        	int port_no = (obj.getString("port_no")==null||obj.getString("port_no").trim().equals(""))?80:Integer.parseInt(obj.getString("port_no"));
					uri = new URI("http", null, host, port_no, "/MeasuReach/ROOT/FlashNews/" + aurl[0], null, null);
					util1.close();
				} catch (Exception e) {			
					e.printStackTrace();
				}
				
				URL u = new URL(uri.toString());  
		        URLConnection con = u.openConnection();  
		        con.setRequestProperty("Range", "bytes=" + (file.length()) + "-");        
		        con.setDoInput(true);
		        con.setDoOutput(true);
		        
		        int respCode = ((HttpURLConnection)con).getResponseCode();
		        System.out.println("respCode: " + respCode);
		        if(respCode == 206){
	
			        InputStream fis = con.getInputStream();  
			        OutputStream out;
			        
			        if(file.exists()){
			        	out = new FileOutputStream(file,true); //makes the stream append if the file exists        	
			        }else{
			        	out = new FileOutputStream(file); //creates a new file.
			        }
			        int lenghtOfFile = con.getContentLength();
			        long total = 0;
			        
			        byte buf[] = new byte[1024];  
			        int len;  
			        while((len = fis.read(buf))>0){  
			        	total += len;
						publishProgress(""+(int)((total*100)/lenghtOfFile));
						
			            out.write(buf, 0, len);  
			        }	  
			        fis.close();  
			        out.close();				
					
					
					System.out.println("download complete...............");
					
					downloaded = true;
		        }else if(respCode == 404){
		        	System.out.println("File not found in server");	        	
		        }else {
		        	System.out.println("File already downloaded");	 
		        	downloaded = true;
		        }	        
		        
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			
			return null;
	
		}
		protected void onProgressUpdate(String... progress) {
			 Log.d("ANDRO_ASYNC", progress[0]);			
		}
	
		@Override
		protected void onPostExecute(String unused) {
			if(downloaded == true){
				Utils.sendFlashNewsDownloadAcknowladgement(getApplicationContext(), imei, id, flashNews, "T", flashNewsPath);				
			}
		}
	}
		

}