package com.stl.musicplayer.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stl.musicplayer.R;

public class ChangeUrlActivity extends Activity implements OnClickListener{
	Button save,close;
	EditText port,ip;
	//View customerLayout;
	ProgressDialog pd;
	TextView urlTv;
	
	String ip_address;
	String port_no;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_url);
		
		save=(Button)findViewById(R.id.cuSaveBtn);
		save.setOnClickListener(this);
		
		close=(Button)findViewById(R.id.cuCloseBtn);
		close.setOnClickListener(this);
		
		ip = (EditText)findViewById(R.id.ipAddr);
		port = (EditText)findViewById(R.id.portNum);
				
		urlTv=(TextView)findViewById(R.id.urlTv);
		
		if(Utils.SERVER_URL!=null){
			urlTv.setText("Current URL is : " + Utils.SERVER_URL);
		}else{
			urlTv.setText("Current URL Not Set");
		}		
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
		case R.id.cuSaveBtn:
			String ipNo = ip.getText().toString();
			String portNo = port.getText().toString();
			if(ipNo.length()>0){
				DatabaseUtil util = new DatabaseUtil(getApplicationContext());	
				String  msg = util.updateHostDetails(ipNo, portNo);	
				util.close();
				final AlertDialog alertDialog = new AlertDialog.Builder(ChangeUrlActivity.this).create();
    			if(msg.equals("Success")){
    				/*if(portNo.length()>0){
    					Utils.SERVER_URL="http://"+ipNo+":"+portNo+"/MeasuReach/sc";
    				}else{
    					Utils.SERVER_URL="http://"+ipNo+"/MeasuReach/sc";
    				}*/
    				
    				Utils.SERVER_URL = Utils.getHostURL(getApplicationContext());
    				
    				alertDialog.setTitle("Alert");
        		    alertDialog.setMessage("Current URL is : " + Utils.SERVER_URL);
    			}else{
    				alertDialog.setTitle("Error");
        		    alertDialog.setMessage("Current URL is : " + Utils.SERVER_URL);
    			}
    		    
    		    
    		    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", 
    		    new DialogInterface.OnClickListener(){
    	  		   public void onClick(DialogInterface dialog, int which) {
    	  			 alertDialog.dismiss();
    	  			 finish();
    	  		   }
    		    });
    		    alertDialog.show();
				System.out.println("SERVER_URL : "+Utils.SERVER_URL);
			}else{
				Toast.makeText(getApplicationContext(), "Please insert a valid IP  address", Toast.LENGTH_LONG).show();
			}
			
			break;
		case R.id.cuCloseBtn:
			finish();
			break;
		}
	}



}
