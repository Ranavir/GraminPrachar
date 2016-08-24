package com.stl.musicplayer.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.stl.musicplayer.R;


public class SettingOptions extends Activity implements OnClickListener{
	private static final String TAG = SettingOptions.class.getSimpleName();
	Button send,downPlaylist,downMedia,changeUrl;
	//EditText mrIdTxt;
	//View customerLayout;
	ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		send=(Button)findViewById(R.id.sendBulkDataBtn);
		send.setOnClickListener(this);
		
		downPlaylist=(Button)findViewById(R.id.downloadPlaylistBtn);
		downPlaylist.setOnClickListener(this);
		
		downMedia=(Button)findViewById(R.id.downloadMediaBtn);
		downMedia.setOnClickListener(this);
		
		changeUrl=(Button)findViewById(R.id.changeUrlBtn);
		changeUrl.setOnClickListener(this);	
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return true;
    }
	public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.action_close:
        	finish();
        	break;
        	
        default:
            return super.onOptionsItemSelected(item);
        }
		return false;
    }
	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sendBulkDataBtn:
			uploadPlayHistory();
			break;
		case R.id.downloadPlaylistBtn:
			downloadPlaylistData();
			break;
		case R.id.downloadMediaBtn:
			downloadMediaClip();
			break;
		case R.id.changeUrlBtn:
			startActivity(new Intent(getApplicationContext(),ChangeUrlActivity.class));
			break;
		}
	}


	public void uploadPlayHistory() {
		System.out.println(TAG+"inside upload playing history.............");
    	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {    		
			String msg;
    		@Override
    		protected void onPreExecute() {
    			pd = new ProgressDialog(SettingOptions.this);
    			pd.setTitle("Uploading");
    			pd.setMessage("Please wait...");
    			pd.setCancelable(false);
    			pd.setIndeterminate(true);
    			pd.show();
    		}
    			
    		@Override
    		protected Void doInBackground(Void... arg0) {
    			try {    				
    				if(Utils.checkiInternet(getApplicationContext())){
    					DatabaseUtil util = new DatabaseUtil(getApplicationContext());
    					util.getPlaySongDetDataToUpload(getApplicationContext());
    					util.getPauseResumeDetailDataToUpload(getApplicationContext());
    					util.getJackRemoveDetailDataToUpload(getApplicationContext());
    					util.getVolumeDataToUpload(getApplicationContext());        				
        				util.close();
        				msg = "All data uploaded successfully";
    				}else{
    					msg = "Internet Connection Not Present";
    				}
    			} catch (Exception e) {
    				System.out.println(TAG+"1. Exception....................");
    				msg = e.getMessage();
    				e.printStackTrace();
    			}
    			return null;
    		}   		    		
    		@Override
    		protected void onPostExecute(Void result) {
    			showAlertDialog(msg);
    			if (pd!=null) {
    				pd.dismiss();
    			}
    		}    			
    	};
    	task.execute();
    }

	public void downloadPlaylistData(){
		System.out.println(TAG+"inside download playlist.............");
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			String msg;
    		@Override
    		protected void onPreExecute() {
    			pd = new ProgressDialog(SettingOptions.this);
    			pd.setTitle("Downloading");
    			pd.setMessage("Please wait...");
    			pd.setCancelable(false);
    			pd.setIndeterminate(true);
    			pd.show();
    		}
    			
    		@Override
    		protected Void doInBackground(Void... arg0) {
    			try {
    				if(Utils.checkiInternet(getApplicationContext())){
	    				TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
	    				mngr.getDeviceId();
	    				String UID = mngr.getDeviceId();
	    				msg = Utils.getInputDataFromServer(UID, getApplicationContext());    				
    				}else{
    					msg = "Internet Connection Not Present";
    				}
    			} catch (Exception e) {
    				System.out.println(TAG+"2. Exception....................");
    				msg = e.getMessage();
    				e.printStackTrace();
    			}
    			return null;
    		}    		    		
    		@Override
    		protected void onPostExecute(Void result) {
    			System.out.println(TAG+"total download.. "+msg);
    			showAlertDialog(msg);    		
    			if (pd!=null) {
    				pd.dismiss();
    			}
    		}
    			
    	};
    	task.execute();    	
	}

	
	public void downloadMediaClip(){
		System.out.println(TAG+"inside download media clip.............");
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			String msg;
    		@Override
    		protected void onPreExecute() {
    			pd = new ProgressDialog(SettingOptions.this);
    			pd.setTitle("Downloading.");
    			pd.setMessage("This may take few minutes, Please wait.");
    			pd.setCancelable(false);
    			pd.setIndeterminate(true);
    			pd.show();
    		}
    			
    		@Override
    		protected Void doInBackground(Void... arg0) {
    			try{    				
    				if(Utils.checkiInternet(getApplicationContext())){
    					msg = Utils.downLoadMediaFile(getApplicationContext(), "9",Utils.getImeiNo(getApplicationContext()));    				
    				}else{
    					msg = "Internet Connection Not Present";
    				}    				
    			}catch(Exception e){
    				System.out.println(TAG+"3. Exception....................");
    				msg = e.getMessage();
    				e.printStackTrace();
    			}    			
    			return null;
    		}
    		
    		    		
    		@Override
    		protected void onPostExecute(Void result) {
    			System.out.println(TAG+"total download.. "+msg);
    			showAlertDialog(msg);    		
    			if (pd!=null) {
    				pd.dismiss();
    			}
    		}
    			
    	};
    	task.execute();    	
	}
	
	public void showAlertDialog(String msg){
		final AlertDialog alertDialog = new AlertDialog.Builder(SettingOptions.this).create();
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
	
}
