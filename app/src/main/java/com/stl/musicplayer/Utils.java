package com.stl.musicplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.telephony.TelephonyManager;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class Utils {
	//public static String MEDIA_PATH="";
	
	//public static String SERVER_URL="http://172.16.1.14:8080/MeasuReach/sc"; // local server
	//public static String SERVER_URL="http://172.16.1.38/MeasuReach/sc"; // 38 server
	public static String SERVER_URL="http://208.109.208.91/MeasuReach/sc"; // host gater
	
	public static String version;
	public static int versionCode;	
	public static String playDate;
	public static Context androidBuildingMusicPlayerActivity ;
	public static boolean playlistUpdate;
	
	private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;
    
	
	public static String getHostURL(Context context){
		String URL = null;		
		try{
			DatabaseUtil util = new DatabaseUtil(context);	
			JSONObject hostDetail = util.getHostDetails();	
			util.close();
			if(hostDetail!=null){
				String ip_address = hostDetail.getString("ip_address");
				String port_no = hostDetail.getString("port_no");
				if(port_no==null || port_no.equals("")){
					URL = "http://"+ ip_address + "/MeasuReach/sc";
				}else{
					URL = "http://"+ ip_address + ":" + port_no + "/MeasuReach/sc";
				}				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return URL;
	}
	
	public static String getImeiNo(Context context){
		TelephonyManager mngr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); 
		//mngr.getDeviceId();
		return mngr.getDeviceId();
	}
	public static String sendBulkDataToServer(JSONObject obj,String id) {
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
		}
		String status = null;
		try {		
			 System.out.println("Bulk Data ::::::::::::::::: ");
			 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
			 namevaluepair.add(new BasicNameValuePair("reqId",id));
			 namevaluepair.add(new BasicNameValuePair("data",obj.toString()));
			 
			 int timeoutConnection = 20000;

			 HttpClient httpClient = new DefaultHttpClient();
			 HttpParams httpParameters = httpClient.getParams();
			 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);			 
			 
			 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
			 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
			 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
			 
			 HttpPost httpPost = new HttpPost(SERVER_URL);
			 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
			 HttpResponse response = httpClient.execute(httpPost);
			 
			 StatusLine statusLine = response.getStatusLine(); 
	         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				 String line = "";
				 while ((line= rd.readLine()) != null){
					 System.out.println("status........"+line);
					 status = line;
				 }
	         }else{
	        	 status = "FAILED";
	         }
		} catch (Exception e) {
			status = "FAILED";
			e.printStackTrace();
		}
		return status;
	}
	public static boolean checkiInternet(Context context) {
		//check Internet connection is availabe
		ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
		   if ( netInfo!= null && netInfo.isAvailable() &&   netInfo.isConnected()) {
		         return true;
		   } else {
		         System.out.println("Internet Connection Not Present");
		       return false;
		   }
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getDate(String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		String to_date = dateFormat.format(date);
		return to_date;
	}
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String currTime = sdf.format(c.getTime());
		System.out.println("utils currTime ::  "+currTime);
		return currTime;
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public static String getInputDataFromServer(String imei,Context context) {
		 if (android.os.Build.VERSION.SDK_INT > 9) {
			 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	         StrictMode.setThreadPolicy(policy);
		 }
		String status = null;
		String newStatus="FAILED";
		String msg = null;
		if(checkiInternet(context)){
			try {
				
				PackageInfo pInfo = null;
				try {
					pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);					
					//get the app version Name for display
					version = pInfo.versionName;
					//get the app version Code for checking
					versionCode = pInfo.versionCode;
				}catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
				//System.out.println("Bulk Data ::::::::::::::::: "+gprs_data.toString());
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId","1"));
				 //namevaluepair.add(new BasicNameValuePair("mrcode",mrcode));
				 namevaluepair.add(new BasicNameValuePair("imei", imei));
				 namevaluepair.add(new BasicNameValuePair("swVersion", version));

				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 //httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				 				 
				 System.out.println("SERVER URL :: ------- "+SERVER_URL);
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 while ((line= rd.readLine()) != null){
						 //System.out.println("Bulk data sending Status : "+line);
						 status = line;
						 if(line.equals("FAILED")){
							msg=" Failed to getData........";
							//Toast.makeText(context, " Failed to getData........", Toast.LENGTH_LONG).show();
						 }else if(line.equals("NODATA")){
							msg="No Record Found........";
							newStatus="NODATA";
							//Toast.makeText(context, " No Record Found........", Toast.LENGTH_LONG).show();
						 }else if(line.equals("INVALID")){
							 msg="No playlist assigned to you.";
						 } else{
							newStatus="SUCCESS";
							status = line;
							System.out.println("status :: "+status);
							DatabaseUtil util = new DatabaseUtil(context);							
							
							String downloadTime = Utils.getDate("yyyy-MM-dd HH:mm:ss");
							long recordcount=0;
							long recordFail=0;
							JSONArray mainArr = new JSONArray(status);
							JSONArray playlistArr = new JSONArray();
							
							for(int i=0;i<mainArr.length();i++){
								
								JSONObject obj = mainArr.getJSONObject(i);
								
								String playlistid = obj.getString("playlistid");
								String playdate = obj.getString("playdate");
								String created_date = obj.getString("created_date");								
								String play_option = obj.getString("play_option");
								
								if(play_option.trim().equalsIgnoreCase("1")){ //1 for With Existing Playlist
									//if want to play With Existing Playlist(append download playlist at last)
									//delete existing playlist of assigned play date(if don't want to keep same playlist)
									//util.deleteExistingPlayList(playlistid, playdate);
									
									
									//don't want to delete existing playlist, only append playlist at last
								
								}else{ //2 for Without Existing Playlist
									//if want to play With Out Existing Playlist(Play downloaded playlist only) 
									//delete all existing playlist of assigned play date
									//after delete all existing playlist initialize currentSongIndex to first index
									util.deleteExistingAllPlayListOfPlayDate(playdate);										
									Utils.playDate = Utils.getDate("yyyy-MM-dd");				    				
									int currentSongIndex = 0;
									int ret = util.updatePlaySongIndex(Utils.playDate, currentSongIndex);
									ArrayList<HashMap<String, String>> songList = util.checkSong();
									AndroidBuildingMusicPlayerActivity.currentSongIndex = songList.size()-1;
								}							
								
								
								JSONObject plObj = new JSONObject();
								plObj.put("playlistid", playlistid);
								playlistArr.put(plObj);
								
								JSONArray songsArr = obj.getJSONArray("songs");
								for (int j=0;j<songsArr.length();j++){
									JSONObject songObj = songsArr.getJSONObject(j);

									long rowId = util.logSongScheduleData(songObj, created_date, downloadTime);
									System.out.println("row id : "+rowId);
									if(rowId>0){
										recordcount++;
									}else{
										recordFail++;
									}
								}							
							}						
							util.close();							
													
							if(playlistArr.length()>0){
								sendAcknowladgement("10", imei, context, playlistArr.toString());
							}
							msg="Total "+recordcount+" Downloaded Successfully And "+recordFail+" Failed";							
							
						 }
					 }
		         }else{
		        	 status = "FAILED";
		        	 msg="" + statusLine.getStatusCode() + " : " + statusLine.getReasonPhrase();
		         }
				 
			} catch (Exception e) {
				status = "FAILED";
				msg="Unable to connect to the server.";
				e.printStackTrace();
			}
		}else{
			msg="Sorry, No Internet Connection";
		}
		System.out.println("Playlist Download Status :: "+msg);
		return newStatus;		
	}
	public static void sendAcknowladgement(String id,String imei,Context context,String playlist) {
		 if (android.os.Build.VERSION.SDK_INT > 9) {
	            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	            StrictMode.setThreadPolicy(policy);
		 }
		if(checkiInternet(context)){
			try {
				
				//System.out.println("Bulk Data ::::::::::::::::: "+gprs_data.toString());
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId",id));
				 namevaluepair.add(new BasicNameValuePair("playlist",playlist));
				 namevaluepair.add(new BasicNameValuePair("imei",imei));
				 
				 int timeoutConnection = 20000;
				 
				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				   
				 
				 
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 while ((line= rd.readLine()) != null){
					 }
		         }else{
		        	 
		         }
				 
			} catch (Exception e) {
				//msg="Unable to connect to the server.";
				e.printStackTrace();
			}
		}else{
			//msg="Sorry, No Internet Connection";
			//Toast.makeText(context, "Sorry, No Internet Connection", Toast.LENGTH_LONG).show();
		}
		//return msg;
		
	}
	public static String downLoadMediaFile(Context context, String id, String imei){
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
		}
		String status = null;
		if(checkiInternet(context)){
			try {
				 int count;
				 String MEDIA_PATH = Environment.getExternalStorageDirectory().toString()+"/Music/";
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId","9"));
				 namevaluepair.add(new BasicNameValuePair("imei",imei));
				 
				 int timeoutConnection = 10000;
				 
				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				   
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 String songName = response.getFirstHeader("filename").getValue();
					 //System.out.println("111111111 :: "+songName);
					// OutputStream output = new FileOutputStream(MEDIA_PATH+songName);
					 InputStream input = new BufferedInputStream(response.getEntity().getContent());					 
					 ZipInputStream zis = new ZipInputStream(input);					 
					// System.out.println("9999999999 "+zis.available());
					 ZipEntry ze = zis.getNextEntry();
					 byte data[] = new byte[1024];
					 JSONArray arr = new JSONArray();
					 while(ze!=null){
						// System.out.println("111111111111111");
						 String fileName = ze.getName();
						 JSONObject obj = new JSONObject();
						 obj.put("fileName", fileName);
						 arr.put(obj);
				         File newFile = new File(MEDIA_PATH + fileName);
				         FileOutputStream fos = new FileOutputStream(newFile);             
				         int len;
				         while ((len = zis.read(data)) > 0) {
				       		fos.write(data, 0, len);
				         }
				         fos.close();   
				         ze = zis.getNextEntry();
				         //System.out.println("ze.......... "+ze);
					 }
					 //System.out.println("");
					 zis.closeEntry();
				     zis.close();
					 System.out.println("arr : "+arr.length());
					 if(arr.length()>0){
						 sendAcknowladgement("11",getImeiNo(context),context,arr.toString());
					 }
					 status=arr.length() +" File downloade successfully to music folder.";
					 System.out.println("download complete......");
		         }else{
		        	 status="" + statusLine.getStatusCode() + " : " + statusLine.getReasonPhrase();
		         }
				 
			} catch (Exception e) {
				System.out.println("error :"+e.getMessage());
				status="No new file for download. ";
				e.printStackTrace();
			}
		}else{
			status="No network connection";
			System.out.println("no internet connection.................");
		}
		
		return status;
	}
	
	public static String sendImagesToServer(Context context,String imei,String imgName,String cap_time){
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
		}
		String status = null;
		if(checkiInternet(context)){
			try {
				 String MEDIA_PATH = Environment.getExternalStorageDirectory().toString()+"/GraminPrachar/Picture/"+imgName;
				 System.out.println("image path :: "+MEDIA_PATH);
		        
				 Bitmap bitmapOrg = BitmapFactory.decodeFile(MEDIA_PATH);
				 ByteArrayOutputStream bao = new ByteArrayOutputStream();
				 bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 70, bao);
				//resizedBitmap=Bitmap.createScaledBitmap(resizedBitmap, 160, 160, true);
				 byte [] ba = bao.toByteArray();
							
				 String imgString=Base64.encodeBytes(ba);
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId","12"));
				 namevaluepair.add(new BasicNameValuePair("image",imgString));
				 namevaluepair.add(new BasicNameValuePair("imei",imei));
				 namevaluepair.add(new BasicNameValuePair("imgName",imgName));
				 namevaluepair.add(new BasicNameValuePair("capture_time",cap_time));
				 
				 int timeoutConnection = 10000;

				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				   
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 while ((line= rd.readLine()) != null){
						 System.out.println("status :: "+line);
						 status=line;
					 } 
		         }else{
		        	 status="FAILED";
		         }				 
			}  catch (Exception e) {
				status="FAILED";
				System.out.println("error :"+e.getMessage());
				e.printStackTrace();
			}
		}else{
			status="FAILED";
			status="No network connection";
			System.out.println("no internet connection.................");
		}
		return status;
	}
	public static boolean deletePhoto(String filename) {
		String DirectoryPath = Environment.getExternalStorageDirectory().toString()+"/GraminPrachar/Picture/";
	    File f = new File(DirectoryPath);
	    f.mkdirs();
	    File dltf= new File(DirectoryPath,filename);
	    boolean status=dltf.delete();
	    return status;
	}
	
	public static File[] GetFiles() {
		String DirectoryPath = Environment.getExternalStorageDirectory().toString()+"/Music";
		System.out.println("=================================: external drive: "+ DirectoryPath);
		
		String externalStorage = System.getenv("EXTERNAL_STORAGE");
		String secondaryStorage = System.getenv("SECONDARY_STORAGE"); 
		System.out.println("=================================: externalStorage" + externalStorage +"    secondaryStorage: "+secondaryStorage);
		
				
	    File f = new File(DirectoryPath);
	    
	    if(!f.exists()){
	    	System.out.println("=================================: external drive not exist");
	    	f.mkdirs();   
	    }
	     
	    File[] files = f.listFiles();   
	    return files;
	}

	public static String getFileNames(){
		System.out.println("Get file names to be deleted............");
		File[] files = GetFiles();
	    JSONArray arr = new JSONArray();
	    try{	    		
            for (int i=0; i<files.length; i++) {
                JSONObject obj = new JSONObject();
                obj.put("clip_name", files[i].getName());
                arr.put(obj);
            }         
	     
		    System.out.println("List of mp3 files           : "+arr.toString());	    
		    System.out.println("Total files in the directory: " + arr.length());
		    
		    sendMediaFileNamesToServer("13", arr.toString());
		    
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	     
	    return arr.toString();
	}
	public static void sendMediaFileNamesToServer(String id, String arrayFiles){
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
		}
		if(checkiInternet(androidBuildingMusicPlayerActivity)){
			try {
				
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId",id));
				 namevaluepair.add(new BasicNameValuePair("data",arrayFiles));
				 namevaluepair.add(new BasicNameValuePair("imei",getImeiNo(androidBuildingMusicPlayerActivity)));
				 int timeoutConnection = 6000;

				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				   
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 String status="";
					 while ((line= rd.readLine()) != null){
						 System.out.println("Media status :: "+line);
						 status=line;
					 }
					 if(status.equalsIgnoreCase("SUCCESS")){
						 Utils.getListOfFilesToBeDlt(); // get media clip name list to be deleted from server
					 }
		         }else{
		        	 
		         }
				 
			}  catch (Exception e) {
				System.out.println("error :"+e.getMessage());
				e.printStackTrace();
			}
		}else{
			System.out.println("no internet connection.................");
		}
	}
	public static void getListOfFilesToBeDlt(){
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
		}
		if(checkiInternet(androidBuildingMusicPlayerActivity)){
			try {
				
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId","14"));
				 namevaluepair.add(new BasicNameValuePair("imei",getImeiNo(androidBuildingMusicPlayerActivity)));
				 int timeoutConnection = 6000;

				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				   
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 while ((line= rd.readLine()) != null){
						 System.out.println("status :: "+line);
						 if(line.equals("FAILED")){
							 System.out.println("Failed to get dlt details..");
						 }else if(line.equals("NODATA")){
							 System.out.println("No data for delete...");
						 }else{
							 //ArrayList<String> arrayFiles = new ArrayList<String>(Arrays.asList(line.split(",")));
							 JSONArray arr = new JSONArray(line.trim());
							 
							 deleteFiles(arr);
						 }
					 }
		         }else{
		        	 
		         }
				 
			}  catch (Exception e) {
				System.out.println("error :"+e.getMessage());
				e.printStackTrace();
			}
		}else{
			System.out.println("no internet connection.................");
		}
	}
	public static void deleteFiles(JSONArray arrayFiles) {
		//ArrayList<String> dltArr = new ArrayList<String>();
		JSONArray dltArr = new JSONArray();
		
		String DirectoryPath = Environment.getExternalStorageDirectory().toString()+"/Music";
	    File f = new File(DirectoryPath);
	    if(f.isDirectory()){
	    	for(int i=0;i<arrayFiles.length();i++){	    		
	    		try{
	    			JSONObject obj = arrayFiles.getJSONObject(i);
	    			String fileName = obj.getString("clip_name").trim();
	    			String dltPath = Environment.getExternalStorageDirectory().toString()+"/Music/"+fileName;
	    			File dltf= new File(dltPath);
	    			
	    			if(dltf.exists()){
	    				boolean status = dltf.delete();
				    	if(status==true){
				    		//dltArr.add(fileName);
				    		JSONObject dltObj = new JSONObject();
				    		dltObj.put("clip_name", fileName);
				    		dltArr.put(dltObj);
				    	}
				    	System.out.println("delete status : "+status);
	    			}			    	
			    	
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}
	    		
	    	}
	    }
	    System.out.println("dlt array size :: "+dltArr.length());
		if(dltArr.length()>0){
			//String tempStr = dltArr.toString().substring(1,dltArr.toString().lastIndexOf("]"));
			
			sendMediaFileNamesToServer("15",dltArr.toString());
		}
	}
	
	
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public static String getFlashNewsFromServer(String imei, Context context) {
		 if (android.os.Build.VERSION.SDK_INT > 9) {
			 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	         StrictMode.setThreadPolicy(policy);
		 }
		String status = null;
		String newStatus="FAILED";
		String msg = null;
		if(checkiInternet(context)){
			try {				

				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId","17"));
				 namevaluepair.add(new BasicNameValuePair("imei", imei));

				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				 				 
				 System.out.println("SERVER URL :: ------- "+SERVER_URL);
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = "";
					 while ((line= rd.readLine()) != null){
						 status = line;
						 if(line.equals("FAILED")){
							msg=" Failed to getData........";
						 } else if(line.equals("NODATA")) {
							msg="No flash news assigned to you.";
							newStatus="NODATA";
						 } else {
							newStatus="SUCCESS";
							status = line;
							System.out.println("status :: " + status);
							DatabaseUtil util = new DatabaseUtil(context);							
							
							String downloadTime = Utils.getDate("yyyy-MM-dd HH:mm:ss");
							long recordcount= 0;
							long recordFail = 0;
							JSONArray mainArr = new JSONArray(status);
							
							for(int i=0;i<mainArr.length();i++){								
								JSONObject newsObj = mainArr.getJSONObject(i);								
								long rowId = util.logFlashNews(newsObj, downloadTime);
								System.out.println("row id : "+rowId);
								if(rowId>0){
									recordcount++;
								}else{
									recordFail++;
								}															
							}						
							util.close();							
							
							msg="Total "+recordcount+" Downloaded Successfully And "+recordFail+" Failed";							
							
						 }
					 }
		         }else{
		        	 status = "FAILED";
		        	 msg="" + statusLine.getStatusCode() + " : " + statusLine.getReasonPhrase();
		         }
				 
			} catch (Exception e) {
				status = "FAILED";
				msg="Unable to connect to the server.";
				e.printStackTrace();
			}
		}else{
			msg="Sorry, No Internet Connection";
		}
		
		System.out.println("Flash News Download Status :: "+msg);
		return newStatus;		
	}
	public static void sendFlashNewsDownloadAcknowladgement(final Context context, String imei, final long id, String flashNews, String downloadStatus, final String flashNewsPath) {
		 if (android.os.Build.VERSION.SDK_INT > 9) {
	            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	            StrictMode.setThreadPolicy(policy);
		 }
		if(checkiInternet(context)){
			try {
				
				//System.out.println("Bulk Data ::::::::::::::::: "+gprs_data.toString());
				 ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
				 namevaluepair.add(new BasicNameValuePair("reqId", ""+18));
				 namevaluepair.add(new BasicNameValuePair("imei", imei));
				 namevaluepair.add(new BasicNameValuePair("id", ""+id));
				 namevaluepair.add(new BasicNameValuePair("flashNews", flashNews));
				 namevaluepair.add(new BasicNameValuePair("downloadStatus", downloadStatus));
				 
				 int timeoutConnection = 20000;
				 
				 HttpClient httpClient = new DefaultHttpClient();
				 HttpParams httpParameters = httpClient.getParams();
				 //HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				 
				 HttpConnectionParams.setConnectionTimeout(httpParameters, REGISTRATION_TIMEOUT);
				 HttpConnectionParams.setSoTimeout(httpParameters, WAIT_TIMEOUT);
				 ConnManagerParams.setTimeout(httpParameters, WAIT_TIMEOUT);
				 
				 HttpPost httpPost = new HttpPost(SERVER_URL);
				 httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
				 HttpResponse response = httpClient.execute(httpPost);
				 
				 StatusLine statusLine = response.getStatusLine(); 
		         if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        	 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					 String line = rd.readLine();
					 if("SUCCESS".equalsIgnoreCase(line)){
						 DatabaseUtil util = new DatabaseUtil(context);
				         util.updateFlashNewsDownloadStatus(id, flashNews, "T");
				         util.close();
				         
				         
				         ////// for testing purpose only//////////////////////////////////////////////////////
				         /*try {
				            MediaPlayer player = new MediaPlayer();
						    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
						    player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);							
						    player.setDataSource(flashNewsPath);  
						    player.prepare();
						    player.setOnCompletionListener(new OnCompletionListener() {
				
					            @Override
					            public void onCompletion(MediaPlayer mp) {
					            	System.out.println("Flash new playing complete......................");					            	
							        deleteFlashNewsAfterPlay(context, id, flashNewsPath);						        
					            }
				
						    });
						    player.start();		    
						
						} catch (Exception e) {
						    e.printStackTrace();
						}*/
				        ///////////////////////////////////////////////////////////////////////////////////// 
				         
				         
				          
				         
					 }		         
		         }else{
		        	 
		         }		         
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
		}		
	}
	
	public static void deleteFlashNewsAfterPlay(Context context, long id, String flashNewsPath){
		try{
			//delete from table after playing....
        	DatabaseUtil util1 = new DatabaseUtil(context);
        	util1.deleteFlashNewsAfterPlay(id);
	        util1.close();
	        
	        //delete from SD card after playing....
	        deleteDir(new File(flashNewsPath));
		}catch(Exception e){
			
		}
	}
	
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
}
