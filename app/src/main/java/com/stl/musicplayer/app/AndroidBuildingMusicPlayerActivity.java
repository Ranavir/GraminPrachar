package com.stl.musicplayer.app;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.stl.musicplayer.R;
import com.stl.musicplayer.models.ReportModel;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint({ "NewApi", "Wakelock" })

public class AndroidBuildingMusicPlayerActivity extends Activity implements OnCompletionListener{//, SeekBar.OnSeekBarChangeListener
	private static final String TAG = "AndroidBuildingMusicPlayerActivity : ";
	final static private long ONE_SECOND = 1000;
	
	public static AndroidBuildingMusicPlayerActivity androidBuildingMusicPlayerActivityInstance;
	
	AlertDialog alertDialog = null;	
	ProgressDialog pd;
    Handler location 	= new Handler();    
    int currPosition = 0;
    int songNotPresent = 0;
    private double last_latitude;
    private double last_longitude;
	private ImageButton btnPlay;
	private ImageButton btnPlaylist;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private  MediaPlayer mp,voicemp ;
	private AssetFileDescriptor afd = null;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	//private SongsManager songManager;
	private Utilities utils;
	//private int seekForwardTime = 5000; // 5000 milliseconds
	//private int seekBackwardTime = 5000; // 5000 milliseconds
	public static int currentSongIndex = 0;
	
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	long songSeqId;long pauseid;
	String songName,plylistname;
	AudioManager audioManager;
	GPSTracker gps;
	HeadsetIntentReceiver receiver;
	
	//2014-10-19
	PowerConnectionReceiver chargeReceiver;
	
	boolean manualPause=false;
	
	private static final String LOG_TAG = "AppUpgrade";
	private double versionName = 0;
	String appURI = "";
	
	private MyWebReceiver swUpdateReceiver;
	private DownloadManager downloadManager;
	private long downloadReference;
	
	PowerManager pm = null;
	PowerManager.WakeLock wl = null;
	WifiLock wifiLock = null;
	
	//alarm manager for updating playlist at mid night
	PendingIntent pendingIntent;
	BroadcastReceiver broadCastReceiver;
	AlarmManager alarmManager;

	//dashboard
	LinearLayout report_layout;
	TextView tv_report_type ;
	TextView tv_date;
	Button btnRefresh ;
	String mStDate ;
	DatePickerDialog mDatePickerDialog ;
	private Calendar calendar ;
	private int year;
	private int month;
	private int day;
	static final int DATE_DIALOG_ID = 999;
	private boolean sp_flag = false ;
	ArrayList<ReportModel> mReportModels;
	DatabaseUtil mDatabaseUtil = null;
	TextView tv_td2,tv_td3,tv_td4,tv_td5,tv_td7,tv_td8,tv_td9,tv_td10,tv_td41,tv_td91;
	//TextView tv_cal_header ;
	Intent backendServiceIntent, mediaDownloadIntent, serviceBootCompleteIntent;
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
				
		androidBuildingMusicPlayerActivityInstance = this;
		
		Utils.SERVER_URL = Utils.getHostURL(getApplicationContext());
		
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			
			//get the app version Name for display
			versionName = Double.parseDouble(pInfo.versionName);
			//get the app version Code for checking
			int versionCode = pInfo.versionCode;
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
 
				
		//Broadcast receiver for our Web Request 
		IntentFilter filter = new IntentFilter(MyWebReceiver.PROCESS_RESPONSE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		swUpdateReceiver = new MyWebReceiver();
		registerReceiver(swUpdateReceiver, filter);
 
		//Broadcast receiver for the download manager
		filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(downloadReceiver, filter);	
		
		
				
		//Prevent an Android device from going to sleep
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		if ((wl != null) && (wl.isHeld() == false)) {
			wl.acquire();
		}
		//wl.release();
		
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "MyWifiLock");		
		
		
		if(Utils.checkiInternet(getApplicationContext())){
			System.out.println("Create Service=======");
			Intent msgIntent = new Intent(this, MyWebService.class);
			msgIntent.putExtra(MyWebService.REQUEST_STRING, Utils.SERVER_URL);
			startService(msgIntent);			
		}				
		
		
		Utils.androidBuildingMusicPlayerActivity=this;
		
		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		//btnForward = (ImageButton) findViewById(R.id.btnForward);
		//btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		//btnNext = (ImageButton) findViewById(R.id.btnNext);
		//btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		
		//btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		//btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		//songTitleLabel.setSelected(true);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		// Mediaplayer
		mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		
		//songManager = new SongsManager(); 
		utils = new Utilities();
		
		// Listeners
		
		//progress seekbar diabled
		//songProgressBar.setOnSeekBarChangeListener(this); // Important
		mp.setOnCompletionListener(this); // Important
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);		
		
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		for(int volume=currVolume+1;volume<=maxVolume;volume++){
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_VIBRATE);
			System.out.println(TAG + " volume: " + volume + "         curVolume: " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
		}
		//audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_VIBRATE);

		gps = new GPSTracker(AndroidBuildingMusicPlayerActivity.this);
		
		//handleHeadphonesState(getApplicationContext());
		
		//*** check head phone state ***\\
		IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		receiver = new HeadsetIntentReceiver();
		registerReceiver( receiver, receiverFilter );
		
		//*** check charger state ***\\
		IntentFilter chargeFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		//2014-10-19
		chargeReceiver = new PowerConnectionReceiver();
		//2014-10-19
		registerReceiver( chargeReceiver, chargeFilter );
		
		String MEDIA_PATH = Environment.getExternalStorageDirectory().toString()+"/Music"; 
		File file = new File(MEDIA_PATH);
		if(!file.exists()){
			file.mkdir();
		}
		
		/*Added by Ranvir Dt. 22-06-2016*/
		/*************************************/
		//turnGPSOn();
		swtichDataConnection(true, AndroidBuildingMusicPlayerActivity.this);
		//System.out.println(TAG+"Ring volume before::"+audioManager.getStreamVolume(AudioManager.STREAM_RING));
		if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			//audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_ALLOW_RINGER_MODES|AudioManager.FLAG_PLAY_SOUND);
			//Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "Already in Silent Mode", Toast.LENGTH_LONG).show();
		} else {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			//audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_ALLOW_RINGER_MODES|AudioManager.FLAG_PLAY_SOUND);
			//Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "Changed to Silent Mode", Toast.LENGTH_LONG).show();
		}
		//System.out.println(TAG+"Ring volume after::"+audioManager.getStreamVolume(AudioManager.STREAM_RING));
		//System.out.println(TAG+"Music volume after::"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

		if(null != getIntent()){
			mStDate = getIntent().getStringExtra("start_date");
			if(null != mStDate){
				try {
					Long Ltime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(mStDate).getTime();
					System.out.println(TAG+"mStDate::"+mStDate);
					System.out.println(TAG+"Ltime::"+Ltime);
					//mCal.setMinDate(new Date(Ltime).getTime());//set the registration date as min date

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		report_layout = (LinearLayout)findViewById(R.id.report_layout);
		tv_report_type = (TextView)findViewById(R.id.tv_report_type);
		tv_date = (TextView)findViewById(R.id.tv_date);
		btnRefresh = (Button)findViewById(R.id.btnRefresh);
		tv_td2 = (TextView)findViewById(R.id.tv_td2);
		tv_td3 = (TextView)findViewById(R.id.tv_td3);
		tv_td4 = (TextView)findViewById(R.id.tv_td4);
		tv_td5 = (TextView)findViewById(R.id.tv_td5);
		tv_td7 = (TextView)findViewById(R.id.tv_td7);
		tv_td8 = (TextView)findViewById(R.id.tv_td8);
		tv_td9 = (TextView)findViewById(R.id.tv_td9);
		tv_td10 = (TextView)findViewById(R.id.tv_td10);
		tv_td41 = (TextView)findViewById(R.id.tv_td41);
		tv_td91 = (TextView)findViewById(R.id.tv_td91);

		calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1 ;
		day = calendar.get(Calendar.DAY_OF_MONTH);
		tv_date.setText(new StringBuilder().append((day) <= 9 ? "0" + (day) : (day))
						.append("-").append((month) <= 9 ? "0" + (month) : (month)).append("-").append(year)
		);//dd-mm-yyyy
		showDailyReport(tv_date.getText().toString());



		btnRefresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				//showDialog(DATE_DIALOG_ID);
				showDailyReport(tv_date.getText().toString());
			}

		});



		/*************************************/
		checkForUpdates();
		
		
		if(isMyServiceRunning("com.stl.musicplayer.app.ServiceBootComplete")){
			System.out.println("ServiceBootComplete Already Started");
			Toast.makeText(getApplicationContext(), "ServiceBootComplete Already Started", Toast.LENGTH_SHORT).show();
		}else{
			System.out.println("ServiceBootComplete Alredy Stoped");
			Toast.makeText(getApplicationContext(), "ServiceBootComplete Already Stoped", Toast.LENGTH_SHORT).show();
			
			serviceBootCompleteIntent = new Intent(this, ServiceBootComplete.class);
			startService(serviceBootCompleteIntent);
		}

		
		//setup the broadcast receiver and alarm manager
		setup();
		
		/**
		 * Button Click event for Play list click event
		 * Launches list activity which displays list of songs
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
				startActivityForResult(i, 100);
			}
		});



	}//end of onCreate

	private void showDailyReport(String date) {
		System.out.println(TAG+"Begin showDailyReport");
		System.out.println(TAG+"date = "+date);
		JSONObject obj = null ;
		mDatabaseUtil = new DatabaseUtil(getApplicationContext());
		try {

			//call to reporting accordingly
			//Toast.makeText(getApplicationContext(),"Show day wise report....",Toast.LENGTH_SHORT).show();
			/****************************************************/
			obj = mDatabaseUtil.getDailyReports(date);
			//Toast.makeText(getApplicationContext(),"obj::"+obj,Toast.LENGTH_SHORT).show();

			/****************************************************/

			tv_td2.setText(obj.get("song_total_time").toString());
			tv_td3.setText(obj.get("song_start_time").toString());
			tv_td4.setText(obj.get("song_end_time").toString());
			tv_td5.setText(obj.get("song_pause_count").toString());
			tv_td41.setText(obj.get("song_count").toString());

			tv_td7.setText(obj.get("add_total_time").toString());
			tv_td8.setText(obj.get("add_start_time").toString());
			tv_td9.setText(obj.get("add_end_time").toString());
			tv_td10.setText(obj.get("add_pause_count").toString());
			tv_td91.setText(obj.get("add_count").toString());
		}catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(null != mDatabaseUtil){
				mDatabaseUtil.close();
			}
		}

		System.out.println(TAG+"End showDailyReport");
	}



	Handler playlistHandler = new Handler();
	Runnable playlistRunnable = new Runnable() {		 
        @Override
        public void run() {
        	getLatestPlaylistAndUpdate();
        }
    };    
    public void getLatestPlaylistAndUpdate(){
    	String tag = "getLatestPlaylistAndUpdate---->";
		System.out.println(TAG+tag+songsList.toString());
		DatabaseUtil util = new DatabaseUtil(getApplicationContext());
		ArrayList<HashMap<String, String>> latestSongList = util.checkSong();
		util.close();		
    	
    	if(songsList.size()==0 && latestSongList.size()==0){    		
    		Log.i(tag, "songList=0 and latestSongList=0");
			System.out.println(TAG + tag + "songList=0 and latestSongList=0");
    		//Toast.makeText(getApplicationContext(), "99999999: "+ currentSongIndex, Toast.LENGTH_SHORT).show();
        	
    	}else if(songsList.size()==0 && latestSongList.size()>0){    		
    		Log.i(tag, "songList=0 and latestSongList>0");
			System.out.println(TAG + tag + "songList=0 and latestSongList>0");
    		songsList = latestSongList;
    		
    		currentSongIndex = getLatestPlaySongIndex();
    		playSong(currentSongIndex);
    		
    		//Toast.makeText(getApplicationContext(), "88888888: "+ currentSongIndex, Toast.LENGTH_SHORT).show();
    		
    	}else if(songsList.size()>0 && latestSongList.size()==0){
    		Log.i(tag, "songList>0 and latestSongList=0");
			System.out.println(TAG + tag + "songList>0 and latestSongList=0");
    		//Toast.makeText(getApplicationContext(), "77777777: "+ currentSongIndex, Toast.LENGTH_SHORT).show();
    		
    	}else if(songsList.size()>0 && latestSongList.size()>0){
    		Log.i(tag, "songList>0 and latestSongList>0");
			System.out.println(TAG + tag + "songList>0 and latestSongList>0");
    		songsList = latestSongList;
    		//String songPath = songsList.get(i).get("songPath");  
     		
    		//Toast.makeText(getApplicationContext(), "66666666: "+ currentSongIndex, Toast.LENGTH_SHORT).show();
    	}    	
    	playlistHandler.postDelayed(playlistRunnable, 30 * 1000);
    }
	
	
	
	
	public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }
	
	private void setup() {
		broadCastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {				
				String currTime = Utils.getDate("HH:mm:ss");	
				Toast.makeText(c, "UpdatePlaylist 00:00:00 / "+currTime, Toast.LENGTH_SHORT).show();				
				checkPlayDate();
			}
		};		
		
		registerReceiver(broadCastReceiver, new IntentFilter("com.stl.musicplayer"));
		pendingIntent = PendingIntent.getBroadcast( this, 0, new Intent("com.stl.musicplayer"), 0 );
		alarmManager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long midNightToday = calendar.getTimeInMillis();		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midNightToday, AlarmManager.INTERVAL_DAY, pendingIntent);
	}
	
	/**
	 * Receiving song index from playlist view
	 * and play the song
	 * */
	public void onResume(){
		super.onResume();

		System.out.println(TAG+"resumed...........");
	}
	public void onPause(){
		super.onPause();
		System.out.println(TAG+"paused--------------");
	}
	public void onStop(){
		super.onStop();
		System.out.println(TAG+"stop...........");
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		System.out.println(TAG+"destroy...........");

		unregisterReceiver(receiver);
		unregisterReceiver(swUpdateReceiver);
		
		//2014-10-19
		unregisterReceiver(chargeReceiver);
		this.unregisterReceiver(downloadReceiver);
	   // mp.release();

		/*************************************************/
		//Added Dt. 22062016 by Ranvir
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
		//turnGPSOff();
		gps.stopUsingGPS();
		swtichDataConnection(false, getApplicationContext());
		/*************************************************/
	    
	    double latitude = 0;
        double longitude = 0;
        // check if GPS enabled    
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }else{
        	latitude=last_latitude;
        	longitude=last_longitude;
        }
		DatabaseUtil util = new DatabaseUtil(getApplicationContext());
		util.updatePlayDetEndTime(songSeqId,"end_time",latitude,longitude);
		util.close();
		

		mp.stop();
		mp.release();
		if(null != voicemp) {
			voicemp.release();
		}
		//h2.removeCallbacks(timerCheck);
		//uploadHandler.removeCallbacks(uploadrun);
		
		location.removeCallbacks(locationRun);
		mHandler.removeCallbacks(mUpdateTimeTask);
		//finish();		
		
		playlistHandler.removeCallbacks(playlistRunnable);
		
		if (wl != null) {
			wl.release();
			wl = null;
		}		
		if (wifiLock.isHeld()==true) 
	        wifiLock.release();
		 
		
		
		//unregister alarm manger and broadcast receiver
		alarmManager.cancel(pendingIntent);
	    unregisterReceiver(broadCastReceiver);


	    return;	    
	}
	
	@Override
	public void onBackPressed() {
		if(songName!=null && plylistname!=null && !songName.equals("") && !plylistname.equals("") && songSeqId!=0){
			DatabaseUtil util = new DatabaseUtil(getApplicationContext());
			pauseid=util.logPlaySongPauseResumeDetails(songName,songSeqId,"back key pressed",plylistname);
			powerPauseId=pauseid;		
		}
		
		if(androidBuildingMusicPlayerActivityInstance != null) {
			androidBuildingMusicPlayerActivityInstance.finish();
		}
	};
	
	
	boolean isStarted = false;
	/**
	 * Function to play a song
	 * @param songIndex - index of song
	 * */
	public void  playSong(int songIndex){
		System.out.println(TAG+" ENTRY---->playSong songIndex:"+songIndex);
		//System.out.println("current song index on playsong: "+songIndex);
		//Toast.makeText(getApplicationContext(), "current song index on playsong : "+songIndex, Toast.LENGTH_LONG).show();
		// Play song
		//System.out.println("Current Song List :: "+songsList.toString());						
		try {
			
			if(songsList.size()>0){
				if(handleHeadphonesState(getApplicationContext())){
					//if(isConnected(getApplicationContext())){
						if(getCurrentVolume()>=8){
							Utils.playlistUpdate=false;
							mp.reset();
							
							//when ever playlist begin after complete cycle songNotPresent should initialized
							if(currentSongIndex==0)
								songNotPresent = 0;
														
							String songPath = songsList.get(songIndex).get("songPath");
							File file = new File(songPath);							
							if(file.exists()){
								mp.setDataSource(songPath);
								mp.prepare();
								
								mp.start();
								isStarted=true;
								// Displaying Song title
								String songTitle = songsList.get(songIndex).get("songTitle");
					        	songTitleLabel.setText(songTitle+"                                   ");
					        	songName=songTitle+".mp3";
					        	plylistname=songsList.get(songIndex).get("plylistname");
					        	// Changing Button Image to pause image
								btnPlay.setImageResource(R.drawable.btn_pause);
								
								// set Progress bar values
								songProgressBar.setProgress(0);
								songProgressBar.setMax(100);
								//record start time
								
								double latitude = 0;
					            double longitude = 0;
						        // check if GPS enabled    
						        if(gps.canGetLocation()){
						            latitude = gps.getLatitude();
						            longitude = gps.getLongitude();
						        }else{
						        	latitude=last_latitude;
						        	longitude=last_longitude;
						        }
								DatabaseUtil util = new DatabaseUtil(getApplicationContext());
								songSeqId = util.logPlaySongDetails(songName,latitude,longitude,plylistname);
								util.close();
								// Updating progress bar
								updateProgressBar();
							}else{
								songTitleLabel.setText("Song not present in device");
								System.out.println(TAG+"Song not present in device......................:" + currentSongIndex);

								songNotPresent++;					
								

								//System.out.println("########### currentSongIndex: "+ currentSongIndex + "       songNotPresent: "+ songNotPresent);
								
								
								if(currentSongIndex < (songsList.size() - 1)){
									System.out.println(TAG+"nnnnnnnnnnnnnn   1");
									currentSongIndex = currentSongIndex + 1;									
								}else{
									System.out.println(TAG+"nnnnnnnnnnnnnn   2");
									currentSongIndex = 0; 
								}	
								
								if(songNotPresent == songsList.size()){
									songTitleLabel.setText("All assigned songs not found in device");
								
									//2014-10-27
									//if all assigned songs not present in device then stop the player, 
									//initialize the songsList and wait for 30 second for latest playlist
									if(mp.isPlaying())
										mp.stop();
									
				    				mHandler.removeCallbacks(mUpdateTimeTask);
				    				initProgressBar();  
				    				songsList = new ArrayList<HashMap<String, String>>();
								}else{
									playSong(currentSongIndex);							
								}								
							}
								
						}else{    
							songTitleLabel.setText("Volume Should be more than 8");
							//showAlertDialog("Volume Should be more than 8");
						}
					/*}else{
						songTitleLabel.setText("Connect charger");
						showAlertDialog("Connect charger");
					}*/
					
				}else{
					songTitleLabel.setText("Insert Speaker Pin");
					showAlertDialog("Insert Speaker Pin In mobile jack");
				}			
				
			}else{
				//System.out.println("00000000000000000000000000000000000000 playsong");
				//showAlertDialog("No Song in the playlist");
			}
				
        			
		} catch (IOException e) {
			// record file not found exception..
			//System.out.println("currentSongIndex :: "+currentSongIndex);
			//System.out.println("songsList size :: "+songsList.size());
			e.printStackTrace();
			if(currentSongIndex < (songsList.size() - 1)){
				System.out.println(TAG+"nnnnnnnnnnnnnn   3");
				currentSongIndex = currentSongIndex + 1;			
			}else{
				System.out.println(TAG+"nnnnnnnnnnnnnn   4");
				currentSongIndex = 0;
			}
			playSong(currentSongIndex);
		}
		System.out.println(TAG+" EXIT---->playSong");
	}//end of playsong
	@Override
	public void onCompletion(MediaPlayer arg0) {
		if(isStarted==true){
			//mp.reset();
			isStarted=false;
			System.out.println(TAG+"Song Playing Complete::::");
			Toast.makeText(getApplicationContext(), "Song Playing Complete", Toast.LENGTH_LONG).show();
			//log end time..
			double latitude = 0;
	        double longitude = 0;
	        // check if GPS enabled    
	        if(gps.canGetLocation()){
	            latitude = gps.getLatitude();
	            longitude = gps.getLongitude();
	        }else{
	        	latitude=last_latitude;
	        	longitude=last_longitude;
	        }
	        //System.out.println("333333333333333333333333333333333333333333");
			DatabaseUtil util = new DatabaseUtil(getApplicationContext());
			util.updatePlayDetEndTime(songSeqId,"end_time",latitude,longitude);			
			util.close();
			
			// check for repeat is ON or OFF
			if(isRepeat){
				System.out.println(TAG+"aaa  isRepeate");
				// repeat is on play same song again
				//playSong(currentSongIndex);
			} else if(isShuffle){
				System.out.println(TAG+"aaa  isSuffle");
				// shuffle is on - play a random song
				//Random rand = new Random();
				//currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
				//playSong(currentSongIndex);
			} else {
				
				System.out.println(TAG+"aaa  not isRepeate and isSuffle");
				
				//Toast.makeText(getApplicationContext(), "currentSongIndex on comp : "+currentSongIndex, Toast.LENGTH_SHORT).show();
				System.out.println(TAG+"currentSongIndex on comp : " + currentSongIndex);
				// no repeat or shuffle ON - play next song
				if(currentSongIndex < (songsList.size() - 1)){
					System.out.println(TAG+"nnnnnnnnnnnnnn   5");
					currentSongIndex = currentSongIndex + 1;					
				}else{
					System.out.println(TAG+"nnnnnnnnnnnnnn   6");
					currentSongIndex = 0;					
				}
				
				DatabaseUtil dbUtil = new DatabaseUtil(getApplicationContext());
				String playDt = Utils.playDate.trim();
				dbUtil.updatePlaySongIndex(playDt, currentSongIndex);					
				dbUtil.close();
				
				playSong(currentSongIndex);


			}

		}else{
			
		}
		
	}
	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 1000);        
    }	
	
	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			  // System.out.println("mp :: "+mp);
			   long totalDuration = mp.getDuration();
			   long currentDuration = mp.getCurrentPosition();
			  
			   // Displaying Total Duration time
			   songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			   // Displaying time completed playing
			   songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
			   
			   // Updating progress bar
			   int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
			   //Log.d("Progress", ""+progress);
			   songProgressBar.setProgress(progress);
			   
			   // Running this thread after 100 milliseconds
		       mHandler.postDelayed(this, 1000);
		   }
	};
		
	/**
	 * On Song Playing completed
	 * if repeat is ON play same song again
	 * if shuffle is ON play random song
	 * */
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	       // Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.smart_campaigner, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){         
        switch (item.getItemId()){
        	case R.id.action_settings:
        		startActivity(new Intent(getApplicationContext(), SettingOptions.class));
        		return false;
			case R.id.action_dashboard:
				startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
				return false;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
	
	//note volume data...
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	     int action = event.getAction();
	     int keyCode = event.getKeyCode();
	     AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);

	     System.out.println(TAG+"max volume: " + am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)+"   curr volume: " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
	    
	     switch (keyCode) {
	         case KeyEvent.KEYCODE_VOLUME_UP:
	             if (action == KeyEvent.ACTION_UP) {             
		              am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE);
		              int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		              logCurrentVolume(volume_level);
	             }
	             return true;
	         case KeyEvent.KEYCODE_VOLUME_DOWN:
	             if (action == KeyEvent.ACTION_DOWN) {
		              am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE);
		              int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		              
		              //volume should be always set 10 when volume is less then 8 as said by G.K
		              if(volume_level<8){
		            	  am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE);
		            	  volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		              }
		              logCurrentVolume(volume_level);
	             }
	             return true;
	         default:
	             return super.dispatchKeyEvent(event);
	     }
	 }
		
	public void logCurrentVolume(int volume_level){
		System.out.println(TAG+"currentVolume  "+volume_level);
		if(songsList.size()>0){			
			if(volume_level<8){				
				if(songsList.size()>0){
					if(mp.isPlaying()){
						if(mp!=null){						
							currPosition = mp.getCurrentPosition();
							mp.pause();
							// Changing button image to play button
							btnPlay.setImageResource(R.drawable.btn_play);
							//record pause time
							DatabaseUtil util = new DatabaseUtil(getApplicationContext());
							pauseid=util.logPlaySongPauseResumeDetails(songName,songSeqId,"low volume",plylistname);
							util.close();				
						}
					}
				}
			}else{
				if(mp!=null){
					if(getCurrentVolume()>=8){
						if(handleHeadphonesState(getApplicationContext())){
							if(isConnected(getApplicationContext())){
								mp.start();
								// Changing button image to pause button
								btnPlay.setImageResource(R.drawable.btn_pause);
								//record resume time
								DatabaseUtil util = new DatabaseUtil(getApplicationContext());
								util.updatePlayDetResumeTime(pauseid);
								util.close();
								powerPauseId=pauseid;
							}else{
								
							}
						}else{
							showAlertDialog("Insert Speaker Pin In mobile jack");
						}
						
					}else{
						showAlertDialog("Volume Should be more than 8");
					}					
				}
			}
			DatabaseUtil util = new DatabaseUtil(getApplicationContext());
	        util.logPlaySongVolumeDetails(songName,songSeqId,volume_level,plylistname);
	        util.close();
		}		
	}
	
	public int getCurrentVolume(){
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		return currentVolume;
	}
	
	public void logJackStatus(String status){
		if(songsList.size()>0){
			DatabaseUtil util = new DatabaseUtil(getApplicationContext());
	        util.logPlaySongJackRemoveDetails(songName, songSeqId,status,plylistname);
	        util.close();
		}
	}

	public boolean handleHeadphonesState(Context context){
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if(am.isWiredHeadsetOn()) {
			// handle headphones plugged in
			//System.out.println("yesssssssssssssssssssssssssssssssssss");
			return true;
		} else{
			// handle headphones unplugged
			//System.out.println("noooooooooooooooooooooooooooooooooooo");
			return false;
		}
	}
	
	public void showAlertDialog(String msg){
		//final AlertDialog alertDialog = new AlertDialog.Builder(AndroidBuildingMusicPlayerActivity.this).create();
		alertDialog = new AlertDialog.Builder(AndroidBuildingMusicPlayerActivity.this).create();
		alertDialog.setTitle("Alert");
	    alertDialog.setMessage(msg);
	    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", 
	    new DialogInterface.OnClickListener(){
  		   public void onClick(DialogInterface dialog, int which) {
  			 alertDialog.dismiss();
  		   }
	    });
	    alertDialog.show();
	}

 
    Runnable locationRun = new Runnable() {		 
        @Override
        public void run() {
        	getLastLocation loc = new getLastLocation();
	    	loc.execute("5000");  
        }
    }; 
    
    
    public int getLatestPlaySongIndex(){
    	DatabaseUtil dbUtil = new DatabaseUtil(getApplicationContext());
		try{			
			JSONObject obj = dbUtil.getPlaySongIndex();
			String curDate = Utils.getDate("yyyy-MM-dd");
			if(obj!=null){
				//id,play_date,song_index
				String pDate = obj.getString("play_date");
				if(pDate.equals(curDate)){
					System.out.println(TAG+"pppppppppppppppppppppppppppppppppppppp-1");
					currentSongIndex = obj.getInt("song_index");
				}else{
					System.out.println(TAG+"pppppppppppppppppppppppppppppppppppppp-2");
					currentSongIndex = 0;								
					dbUtil.updatePlaySongIndex(curDate, currentSongIndex);							
				}
			}else{
				System.out.println(TAG+"pppppppppppppppppppppppppppppppppppppp-3");
				currentSongIndex = 0;
			}

		} catch (Exception e) {
			currentSongIndex = 0;
			e.printStackTrace();
		} finally {
			if (dbUtil!=null)
				dbUtil.close();
		}
		
		System.out.println(TAG+"cuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuurrrrrrrrrrrrrrrr song index: " + currentSongIndex);
		return currentSongIndex;
    }
    
	public void checkForUpdates() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			String msg;
			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(AndroidBuildingMusicPlayerActivity.this);
				pd.setTitle("Checking For Updates");
				pd.setMessage("Please wait...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
				
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					DatabaseUtil util = new DatabaseUtil(getApplicationContext());
					ArrayList<HashMap<String, String>> tempSongsList = util.checkSong(); 
					util.close();
					if(tempSongsList.size()>0){
						System.out.println(TAG+"mmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
						currentSongIndex = getLatestPlaySongIndex();
						System.out.println(TAG+"2222222222222222222222222222 currentSongIndex: manoj:" + currentSongIndex);
						//playSong(0);		
					}else{
						if(Utils.checkiInternet(getApplicationContext())){
							TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
							mngr.getDeviceId();
							String UID = mngr.getDeviceId();
							msg = Utils.getInputDataFromServer(UID, getApplicationContext());
						}else{
							System.out.println(TAG+"1. Internet Connection Not Present");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
			    		
			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) {
					pd.dismiss();
				}
				//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				msg = "All data uploaded successfully";
				DatabaseUtil util = new DatabaseUtil(getApplicationContext());
				ArrayList<HashMap<String, String>> tempSongsList = util.checkSong();
				util.close();
				//System.out.println("22222222222222222222222222222222 "+songsList.size());
				
				
				
				//added by manoj on date 2014-10-16
				//because when player start check for today's last play song index and play if not completed.
				currentSongIndex = getLatestPlaySongIndex();
				System.out.println(TAG+"2222222222222222222222222222 currentSongIndex: " + currentSongIndex);
								
				// By default play first song 
				//but required, last song index which is not complete, play again
				if(tempSongsList.size()>0){
					System.out.println(TAG+"ddddddddddddddddd   7");
					//commented my manoj on date 2014-10-16
					//because when player start check for today's last play song index and play if not completed.
					//playSong(0);
										
					
					//added by manoj on date 2014-10-16
					//because when player start check for today's last play song index and play if not completed.					
					//playSong(currentSongIndex);				

				}else{
					//System.out.println("9999999999999999999999999999999999999");
					showAlertDialog("No Song in the playlist");
					//give voice message that no song in local db
					playVoiceMsg("playlist_not_found.3gpp");
				}
				
				//added by manoj on date 2014-10-25
				//because when player start check for today's last play song index and play if not completed.					
				//get latest playlist and play
				getLatestPlaylistAndUpdate();
				
				Handler temp = new Handler();
				temp.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							startAllBackgroundTask();
						}catch (Exception e) {
							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
					}
				}, 50 * 1000);			
			}
		};
		task.execute();
	}
	
	public void startAllBackgroundTask(){        
    	getLastLocation loc = new getLastLocation();
    	loc.execute("5000"); 
	}



	private class getLastLocation extends AsyncTask<String, String, String> {		
		  private String resp;		   
		  @Override
		  protected String doInBackground(String... params) {
			  if(gps.canGetLocation()){
		        	last_latitude = gps.getLatitude();
		        	last_longitude = gps.getLongitude();
			  }
			 // System.out.println("updating location :: latitude is :: "+last_latitude +" & Longitude is :: "+last_longitude);
			return resp;
		  }
		 
		  @Override
		  protected void onPostExecute(String result) {
			  location.postDelayed(locationRun, 10*1000);
		  }
		  @Override
		  protected void onPreExecute() {
		  
		  }
		  @Override
		  protected void onProgressUpdate(String... text) {
		   
		  }
	}
		
	//************************ class to check headset is pluged or not *************************||
	public class HeadsetIntentReceiver extends BroadcastReceiver {
		private String TAG = "HeadSet";
		
		public HeadsetIntentReceiver() {
			Log.d(TAG, "Created");
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				System.out.println("state of head phone:  "+state);
				switch(state) {
					case(0):
						Log.d(TAG, "Headset unplugged");
		                logJackStatus("unplugged");
		                if(songsList.size()>0){
		                	if(mp!=null){
		                		if(mp.isPlaying()){
		                			currPosition = mp.getCurrentPosition();		    							
		    						mp.pause();
		    						// Changing button image to play button
		    						btnPlay.setImageResource(R.drawable.btn_play);
		    						//record pause time
		    						DatabaseUtil util = new DatabaseUtil(getApplicationContext());
		    						pauseid = util.logPlaySongPauseResumeDetails(songName,songSeqId,"pin removed",plylistname);
		    						util.close();
		    					}
		                	}
		                }
		                Toast.makeText(getApplicationContext(), "=== Headset unplugged ===", Toast.LENGTH_LONG).show();
		                break;
					case(1):
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_VIBRATE);
		                logJackStatus("plugged");
		                if(mp!=null){
		                	if(getCurrentVolume()>=8){
		                		if(isConnected(getApplicationContext())){
		                			
		                			//Toast.makeText(getApplicationContext(), "csi:"+currentSongIndex + "   pos:"+ currPosition, Toast.LENGTH_SHORT).show();
		                			
		                			if(Utils.playlistUpdate){
		                				//Toast.makeText(getApplicationContext(), "111", Toast.LENGTH_SHORT).show();
		                				System.out.println(TAG+"ddddddddddddddddd   8");

		        						//commented on date  2014-10-20
		                				//currentSongIndex = 0;
		                				currentSongIndex = getLatestPlaySongIndex();
										playSong(currentSongIndex);	
									
									//commented on date 2014-10-27
		                			//}else if(currentSongIndex == 0 && currPosition==0){
		                			}else if(currPosition==0){
		                				//Toast.makeText(getApplicationContext(), "222", Toast.LENGTH_SHORT).show();
		                				if(!mp.isPlaying()){
		                					//Toast.makeText(getApplicationContext(), "333", Toast.LENGTH_SHORT).show();
		                					System.out.println(TAG+"ddddddddddddddddd   9.1");
											currentSongIndex = getLatestPlaySongIndex();	
											
											//commented by manoj on date 2014-10-17 3:15PM
											playSong(currentSongIndex);
				        				}else{
				        					//Toast.makeText(getApplicationContext(), "444", Toast.LENGTH_SHORT).show();
				        					System.out.println(TAG+"ddddddddddddddddd   9.2");
				        				}											
		                			}else{
		                				//Toast.makeText(getApplicationContext(), "555", Toast.LENGTH_SHORT).show();
		                				mp.start();
										// Changing button image to pause button
										btnPlay.setImageResource(R.drawable.btn_pause);
										//record resume time
										DatabaseUtil util = new DatabaseUtil(getApplicationContext());
										util.updatePlayDetResumeTime(pauseid);
										util.close();
										powerPauseId=pauseid;										
		                			}
										
		                		}else{
									//showAlertDialog("Connect Charger");
		                		}
		                	}else{
									
		                	}									
		                }
		                Log.d(TAG, "Headset plugged");
		                Toast.makeText(getApplicationContext(), "*** Headset plugged ***", Toast.LENGTH_LONG).show();
		                break;
					default:
						Log.d(TAG, "Error");
		                Toast.makeText(getApplicationContext(), "!!! Error !!!", Toast.LENGTH_LONG).show();
				}
			}else{
				// System.out.println("11111111111111111111111111111111111111111111111");
			}
		}
	}
	
	// **************************** class to check charger state *****************************\\
	boolean isResumed;
	long powerPauseId=0;
	public class PowerConnectionReceiver extends BroadcastReceiver {
		@Override
	    public void onReceive(Context context, Intent intent) {
			System.out.println("PowerConnectionReceiver: ENTRY---> onReceive");
			int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
	    	boolean isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
	    		    	
	    	//Toast.makeText(context, "isCharging: " + isCharging, Toast.LENGTH_SHORT).show();
	    	
	    	//int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	        //boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
	        //boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
	         	         
	        //System.out.println("isCharging: " + isCharging + "  chargePlug: "+ chargePlug +"  usbCharge: "+usbCharge + "   acCharge: "+acCharge);
	        //Toast.makeText(context, "isCharging: " + isCharging + "  chargePlug: "+ chargePlug +"  usbCharge: "+usbCharge + "   acCharge: "+acCharge, Toast.LENGTH_LONG).show();
	     	    	
	        if(isCharging == true){
	        	if(getCurrentVolume()>=8){
	        		//record resume time 
					//System.out.println("powerPauseId : "+powerPauseId);
					if(handleHeadphonesState(getApplicationContext())){
						//System.out.println("manualPause status :: "+manualPause);
		        		if(manualPause==true){
		        				
		        		}else{
		        			if(Utils.playlistUpdate){
		        				System.out.println(TAG+"ddddddddddddddddd   10");
		        				//currentSongIndex=0;
		        				currentSongIndex = getLatestPlaySongIndex();
								playSong(currentSongIndex);
		        			}else if(currentSongIndex == 0 && currPosition==0){
		        				if(!mp.isPlaying()){
		        					System.out.println(TAG+"ddddddddddddddddd   11.0");
									//currentSongIndex=0;
		        					currentSongIndex = getLatestPlaySongIndex();
									playSong(currentSongIndex);
		        				}else{
		        					System.out.println(TAG+"ddddddddddddddddd   11.1");
		        				}			        					
							}else{
								mp.start();
								// Changing button image to pause button
								btnPlay.setImageResource(R.drawable.btn_pause);
								if(isResumed==false){
									DatabaseUtil util = new DatabaseUtil(getApplicationContext());
									util.updatePlayDetResumeTime(pauseid);
									util.close();
									isResumed=true;
								}
		        			}
		        		}	
		        	}
				}else{
					showAlertDialog("Volume Should be more than 8");
				}
	        }else{
	        	if(songsList.size()>0){
	        		if(mp.isPlaying()){
	        			if(mp!=null){
	        				currPosition = mp.getCurrentPosition();
							mp.pause();
							// Changing button image to play button
							btnPlay.setImageResource(R.drawable.btn_play);
							//record pause time
							DatabaseUtil util = new DatabaseUtil(getApplicationContext());
							pauseid=util.logPlaySongPauseResumeDetails(songName,songSeqId,"power disconnected",plylistname);
							powerPauseId=pauseid;
							isResumed=false;
							util.close();
						}
					}
				}
	        }
			System.out.println("PowerConnectionReceiver: EXIT---> onReceive");
	    }
	}

	void initProgressBar(){
		songTotalDurationLabel.setText("");
	   	// Displaying time completed playing
	   	songCurrentDurationLabel.setText("");	   
	   	// Updating progress bar
	   	songProgressBar.setProgress(0);
	   	songProgressBar.setMax(100);
	}
	
	public void checkPlayDate(){
    	try {
    		if(Utils.playDate==null || Utils.playDate.trim().equals("")){
    			songTitleLabel.setText("No playlist assigned to you");
    			
    			//2014-10-01
    			mHandler.removeCallbacks(mUpdateTimeTask);
    			initProgressBar();
    			
    			System.out.println(TAG+"bbbbb    1");
    			updatePlaylist();
    		}else{
    			System.out.println(TAG+"bbbbb    2");
    			String playDt = Utils.playDate.trim() + " 23:59:59";
    			String toDt = Utils.getDate("yyyy-MM-dd HH:mm:ss");

    			System.out.println(TAG+"checking playDt: " + playDt + "            toDt: " + toDt);
    			
    			if(playDt.compareTo(toDt)>0){
    				System.out.println(TAG+"bbbbb    3");
    			}else{
    				System.out.println(TAG+"bbbbb    4");
    				songTitleLabel.setText("No playlist assigned to you");
    				
    				//2014-10-01
    				mHandler.removeCallbacks(mUpdateTimeTask);
    				initProgressBar();
    				
    				Utils.playDate = Utils.getDate("yyyy-MM-dd");
    				
    				currentSongIndex = 0;
    				DatabaseUtil dbUtil = new DatabaseUtil(getApplicationContext());
    				dbUtil.updatePlaySongIndex(Utils.playDate, currentSongIndex);					
    				dbUtil.close();    				
    				
    				//Toast.makeText(getApplicationContext(), Utils.playDate+"==="+currentSongIndex, Toast.LENGTH_SHORT).show();
    				    				
    				updatePlaylist();
    			}
    		}			
		} catch (Exception e) { 
			e.printStackTrace();
		} 
	}
		
	public void updatePlaylist() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		System.out.println(TAG+" ENTRY--->updatePlaylist");
		DatabaseUtil util = new DatabaseUtil(getApplicationContext());
		songsList = util.checkSong();
		util.close();
		/*if(mp.isPlaying()){
			System.out.println("bbbbb    5");
			mp.reset();
		}*/
			
		
		if(songsList.size()>0){			
			System.out.println("bbbbb    6"); 
				
			Utils.playlistUpdate=true;
			double latitude = 0;
	        double longitude = 0;
	        // check if GPS enabled    
	        if(gps.canGetLocation()){
	            latitude = gps.getLatitude();
	            longitude = gps.getLongitude();
	        }else{
	        	latitude=last_latitude;
	        	longitude=last_longitude;
	        }
			DatabaseUtil dutil = new DatabaseUtil(getApplicationContext());
			dutil.updatePlayDetEndTime(songSeqId,"end_time",latitude,longitude);
			dutil.close();
			
			System.out.println(TAG+"ddddddddddddddddd   12");

			//commented on date  2014-10-20
			//currentSongIndex = 0;
			currentSongIndex = getLatestPlaySongIndex();
			playSong(currentSongIndex);
		}else{
			System.out.println("bbbbb    7");	
			/*if(mp.isPlaying()){
				mp.reset(); 
			}*/ 
			mp.reset();
			mp.stop();	
						
			if(Utils.checkiInternet(getApplicationContext())){
				TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
				mngr.getDeviceId();
				String UID = mngr.getDeviceId();
				String status = Utils.getInputDataFromServer(UID, getApplicationContext());
				if("SUCCESS".equals(status)){
					DatabaseUtil util1 = new DatabaseUtil(getApplicationContext());
					songsList = util1.checkSong();
					util1.close();
					
					if(songsList.size()>0){			
						System.out.println(TAG+"bbbbbbbbbbb    8");
						Utils.playlistUpdate = true;	
						//commented on date  2014-10-20
						//currentSongIndex = 0;
						currentSongIndex = getLatestPlaySongIndex();
						playSong(currentSongIndex);
					}				
				}
			}
		}
		System.out.println(TAG+" EXIT--->updatePlaylist");
	}
	// -------------------------BroadcastReceiver---------------------
	public class MyWebReceiver extends BroadcastReceiver{
		 
		public static final String PROCESS_RESPONSE = "com.stl.musicplayer.intent.action.PROCESS_RESPONSE";

		@Override
		public void onReceive(Context context, Intent intent) {
 
			String reponseMessage = intent.getStringExtra(MyWebService.RESPONSE_MESSAGE);
			Log.v(LOG_TAG, reponseMessage);
			System.out.println(TAG+"22222222222222222222222222222222222222222222222222");
			//parse the JSON response
			JSONObject responseObj;
			try { 
				if((reponseMessage!=null) && (!reponseMessage.trim().equals(""))){
					responseObj = new JSONObject(reponseMessage);
					boolean success = responseObj.getBoolean("success");
					//if the reponse was successful check further
					if(success){
						//get the latest version from the JSON string
						double latestVersion = responseObj.getDouble("latestVersion");
						//get the lastest application URI from the JSON string
						System.out.println(TAG+"latest version code :: "+latestVersion);
						System.out.println(TAG+"prev version code :: "+versionName);
						appURI = responseObj.getString("appURI");
						//check if we need to upgrade?
						if(latestVersion > versionName){  
							//oh yeah we do need an upgrade, let the user know send an alert message
							AlertDialog.Builder builder = new AlertDialog.Builder(AndroidBuildingMusicPlayerActivity.this);
							String msg = String.format(TAG+"There is newer version %.1f of this application available, click OK to upgrade now?", latestVersion);
							builder.setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener() {
								//if the user agrees to upgrade							
							
								public void onClick(DialogInterface dialog, int id) {
									String appFileName = appURI.substring(appURI.lastIndexOf("/")+1, appURI.length());
									
									//start downloading the file using the download manager
									downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
									Uri Download_Uri = Uri.parse(appURI);
									DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
									
									//request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
									//request.setAllowedOverRoaming(false);
									
									//2014-10-29
									//request.setTitle("My Andorid App Download");									
									request.setTitle(appFileName);
									request.setDestinationInExternalFilesDir(AndroidBuildingMusicPlayerActivity.this, Environment.DIRECTORY_DOWNLOADS, appFileName);								
									
									downloadReference = downloadManager.enqueue(request);								
								}
							}).setNegativeButton("Remind Later", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
								}
							});
							//show the alert message
							builder.create().show();
						} 
					}
				}				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	//broadcast receiver to get notification about ongoing downloads
	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {		
		public void onReceive(Context context, Intent intent) { 
			//check if the broadcast message is for our Enqueued download
			long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			System.out.println(TAG+"==============referenceId: "+ referenceId);
			System.out.println(TAG+"==============downloadReference: "+ downloadReference);
			
			if(downloadReference == referenceId){ 
				Log.v(LOG_TAG, "Downloading of the new app version complete");
				  
				System.out.println(TAG+"========getUriForDownloadedFile: "+ downloadManager.getUriForDownloadedFile(downloadReference));
				if(downloadManager.getUriForDownloadedFile(downloadReference)!=null){
					//start the installation of the latest version
					Intent installIntent = new Intent(Intent.ACTION_VIEW);
					installIntent.setDataAndType(downloadManager.getUriForDownloadedFile(downloadReference), "application/vnd.android.package-archive");
					installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(installIntent); 
				}else{
					Toast.makeText(getApplicationContext(), "App file name mismatch in server", Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	
	
	private boolean isMyServiceRunning(String serviceClassName) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		    //if ("com.example.MyService".equals(service.service.getClassName())) {
			if (serviceClassName.equals(service.service.getClassName())) {
		        return true;
		    }
		}
		return false;
	}
	/*********************************************************************************************/
	/**
	 * This method plays the voice msgs when required
	 * @param filename
	 */
	private void playVoiceMsg(String filename) {
		try {
			voicemp = new MediaPlayer();
			afd = getAssets().openFd("songs/"+filename);
			voicemp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			afd.close();
			voicemp.prepare();
			voicemp.start();
			System.out.println(TAG+"must play ...");
		} catch (IOException e) {
			System.out.println(TAG+"exception...");
			e.printStackTrace();
		}
	}//end of playVoiceMsg
	/**
	 * @author Ranvir
	 * @date 26262016
	 */
	public void turnGPSOn()
	{
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		sendBroadcast(intent);

	}
	/**
	 * @author Ranvir
	 * @date 26262016
	 */
	// automatic turn off the gps
	public void turnGPSOff()
	{
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", false);
		sendBroadcast(intent);


	}
	/**
	 * @author Ranvir
	 * @date 26262016
	 */
	boolean swtichDataConnection(boolean ON,Context context)
	{
		int bv = Build.VERSION.SDK_INT;
		try{
			if(bv == Build.VERSION_CODES.FROYO)

			{
				Method dataConnSwitchmethod;
				Class<?> telephonyManagerClass;
				Object ITelephonyStub;
				Class<?> ITelephonyClass;

				TelephonyManager telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);

				telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
				Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
				getITelephonyMethod.setAccessible(true);
				ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
				ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

				if (ON) {
					dataConnSwitchmethod = ITelephonyClass
							.getDeclaredMethod("enableDataConnectivity");
				} else {
					dataConnSwitchmethod = ITelephonyClass
							.getDeclaredMethod("disableDataConnectivity");
				}
				dataConnSwitchmethod.setAccessible(true);
				dataConnSwitchmethod.invoke(ITelephonyStub);

			}
			else
			{
				//log.i("App running on Ginger bread+");
				Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "App running on Ginger bread+", Toast.LENGTH_LONG).show();

				final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				final Class<?> conmanClass = Class.forName(conman.getClass().getName());
				final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
				iConnectivityManagerField.setAccessible(true);
				final Object iConnectivityManager = iConnectivityManagerField.get(conman);
				final Class<?> iConnectivityManagerClass =  Class.forName(iConnectivityManager.getClass().getName());
				final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
				setMobileDataEnabledMethod.setAccessible(true);
				setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
				if(ON){
					Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "Data Connection ON", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "Data Connection OFF", Toast.LENGTH_LONG).show();
				}
			}


			return true;
		}catch(Exception e){
			//Log.e(TAG, "error turning on/off data");
			Toast.makeText(AndroidBuildingMusicPlayerActivity.this, "error turning on/off data", Toast.LENGTH_LONG).show();
			return false;
		}

	}//end of swtichDataConnection
	/*********************************************************************************************/
	public static boolean closeActivity(){
		boolean retValue = false;		
		if(androidBuildingMusicPlayerActivityInstance != null) {
			androidBuildingMusicPlayerActivityInstance.finish();
			retValue = true;
		}
		if(SplashScreen.splashScreenActivityInstance != null){
			SplashScreen.splashScreenActivityInstance.finish();
			retValue = true;
		}	
		if(PlayListActivity.playListActivityInstance !=null){
			PlayListActivity.playListActivityInstance.finish();
			retValue = true;
		}
				
		return retValue;
	}
}