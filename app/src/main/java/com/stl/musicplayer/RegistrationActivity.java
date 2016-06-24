package com.stl.musicplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends Activity implements OnClickListener,AdapterView.OnItemSelectedListener {
	private static final String TAG = "RegistrationActivity : ";
	Button btn_save, btn_exit;
	ProgressDialog pd;
	TextView urlTv;
	Spinner sp_state;
	DatabaseUtil dbutil;
	EditText et_busname, et_bus_regno, et_sit_capacity, et_bus_st, et_bus_et, et_owner_nm, et_owner_cn, et_agent_nm, et_agent_cn;
	String imei_no, state, bus_name, bus_reg_no, sit_cap, bus_st, bus_et, own_nm, own_cn, agent_nm, agent_cn;

	String mTitle, mMsg;
	boolean bbusname, bbusregno, bsitcapacity, bst, bet, bownernm, bownercn, bagentnm, bagentcn;
	JSONObject jsonObjRegDetails;
	String status = "";
	String serverACK = "";
	long regId;
	Intent mIntent ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_regd);
		dbutil = new DatabaseUtil(RegistrationActivity.this);
		//check for the registration details in local database
		jsonObjRegDetails = dbutil.getRegdDetails();

		//if present then check for the status
		if (null != jsonObjRegDetails) {
			System.out.println(TAG+" Registration details available in local db...");
			//Get id and status
			try {
				regId = jsonObjRegDetails.getLong("id");
				status = jsonObjRegDetails.getString("status");
				System.out.println(TAG+" regId: "+regId+" status: "+status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// if status is 0 get the details of the registration and send to server for updation
			if ("1".equals(status)) {//user is registered
				mIntent = new Intent(RegistrationActivity.this, AndroidBuildingMusicPlayerActivity.class);
				startActivity(mIntent);
				finish();
			}else{//Registration ack not get and not saved in local
				if (Utils.checkiInternet(RegistrationActivity.this)) {
					getRegistrationAck(jsonObjRegDetails);
				} else {
					mTitle = "Message";
					mMsg = "Internet connectivity unavailable!!!";
					showErrorAlert(mTitle, mMsg);
				}
			}
		} else {//if totally absent in local db run below code
			System.out.println(TAG+"No registration details available in local db...");
			initUI();
			/****************************** Validate user id availability *******************************/
			et_busname.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String busname = et_busname.getText().toString().trim();
						if (!busname.matches("[A-Za-z][A-Za-z0-9_\\s]+")) {
							/*mTitle = "Invalid User Id Length";
							mMsg = "User Id should be in between 6 to 15 digits";
							showErrorAlert(mTitle, mMsg);
							et_state.setText("");*/
							et_busname.setError("Invalid Vehicle Name...");
							et_busname.setText("");
							//et_state.requestFocus();
							bbusname = false;
						} else {
							bbusname = true;
							bus_name = busname;
						}
					}
				}
			});
			et_bus_regno.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_bus_regno.getText().toString().trim();
						if (!str.matches("[A-Za-z]{2}[A-Za-z0-9\\s]+")) {
							et_bus_regno.setError("Invalid Vehicle No...");
							et_bus_regno.setText("");
							bbusregno = false;
						} else {
							bbusregno = true;
							bus_reg_no = str;
						}
					}
				}
			});
			et_sit_capacity.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_sit_capacity.getText().toString().trim();
						if (!str.matches("[1-9][0-9][0-9]?")) {
							et_sit_capacity.setError("Invalid Sitting Capacity...");
							et_sit_capacity.setText("");
							bsitcapacity = false;
						} else {
							bsitcapacity = true;
							sit_cap = str;
						}
					}
				}
			});
			et_bus_st.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_bus_st.getText().toString().trim();
						if (!str.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
							et_bus_st.setError("Invalid Time...");
							et_bus_st.setText("");
							bst = false;
						} else {
							bst = true;
							bus_st = str;
						}
					}
				}
			});
			et_bus_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_bus_et.getText().toString().trim();
						if (!str.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
							et_bus_et.setError("Invalid Time...");
							et_bus_et.setText("");
							bet = false;
						} else {
							bet = true;
							bus_et = str;
						}
					}
				}
			});
			et_owner_nm.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_owner_nm.getText().toString().trim();
						if (!str.matches("[A-Za-z][a-zA-Z\\s]*")) {
							et_owner_nm.setError("Invalid Name...");
							et_owner_nm.setText("");
							bownernm = false;
						} else {
							bownernm = true;
							own_nm = str;
						}
					}
				}
			});
			et_agent_nm.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_agent_nm.getText().toString().trim();
						if (!str.matches("[A-Za-z][a-zA-Z\\s]*")) {
							et_agent_nm.setError("Invalid Name...");
							et_agent_nm.setText("");
							bagentnm = false;
						} else {
							bagentnm = true;
							agent_nm = str;
						}
					}
				}
			});
			et_owner_cn.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_owner_cn.getText().toString().trim();
						if (!str.matches("(0|91)?[7-9][0-9]{9}")) {
							et_owner_cn.setError("Invalid Mobile no...");
							et_owner_cn.setText("");
							bownercn = false;
						} else {
							bownercn = true;
							own_cn = str;
						}
					}
				}
			});
			et_agent_cn.setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if (hasFocus) {
						// do nothing
					} else {
						String str = et_agent_cn.getText().toString().trim();
						if (!str.matches("(0|91)?[7-9][0-9]{9}")) {
							et_agent_cn.setError("Invalid Mobile no...");
							et_agent_cn.setText("");
							bagentcn = false;
						} else {
							bagentcn = true;
							agent_cn = str;
						}
					}
				}
			});
		}//end if
	}

	/**
	 * UI initialization method
	 */
	private void initUI() {
		System.out.println(TAG+" ENTRY----> initUI");
		//et_state,et_busname,et_bus_regno,et_sit_capacity,et_bus_st,et_bus_et,et_owner_nm,et_owner_cn,et_agent_nm,et_agent_cn
		//et_state = (EditText)findViewById(R.id.et_state);
		sp_state = (Spinner) findViewById(R.id.sp_state);
		sp_state.setOnItemSelectedListener(this);

		et_busname = (EditText) findViewById(R.id.et_busname);
		et_bus_regno = (EditText) findViewById(R.id.et_bus_regno);
		et_sit_capacity = (EditText) findViewById(R.id.et_sit_capacity);
		et_bus_st = (EditText) findViewById(R.id.et_bus_st);
		et_bus_et = (EditText) findViewById(R.id.et_bus_et);
		et_owner_nm = (EditText) findViewById(R.id.et_owner_nm);
		et_owner_cn = (EditText) findViewById(R.id.et_owner_cn);
		et_agent_nm = (EditText) findViewById(R.id.et_agent_nm);
		et_agent_cn = (EditText) findViewById(R.id.et_agent_cn);

		btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);

		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_exit.setOnClickListener(this);
		//bstate,bbusname,bbusregno,bsitcapacity,bbusst,bbuset,bownernm,bownercn,bagentnm,bagentcn
		bbusname = bbusregno = bsitcapacity = bst = bet = bownernm = bownercn = bagentnm = bagentcn = false;
		imei_no = Utils.getImeiNo(getApplicationContext());
		System.out.println(TAG+" EXIT----> initUI");
	}//end initUI


	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_save:
				et_agent_cn.clearFocus();
				if (validateFields()) {
					//Toast.makeText(getBaseContext(),"All fields valid...",Toast.LENGTH_SHORT).show();


					if (Utils.checkiInternet(getApplicationContext())) {
						//save in local db and send to server for getting status
						saveRegdDetails();

					} else {
						//give voice message of toast
						mTitle = "Message";
						mMsg = "Internet connectivity unavailable!!!";
						showErrorAlert(mTitle, mMsg);
					}
				} else {
					mTitle = "Fill all fields";
					mMsg = "Fields cannot be left blank!!!";
					showErrorAlert(mTitle, mMsg);
				}
				break;
			case R.id.btn_exit:
				finish();
				break;
			default:
				break;
		}
	}

	/**
	 * This method saves the registration details in the local database
	 * then sends the particulars to the server and gets the ack
	 */
	private void saveRegdDetails() {
		System.out.println(TAG + " ENTRY----> saveRegdDetails");
		jsonObjRegDetails = new JSONObject();
		try {
			//String imei_no,state,bus_name,bus_reg_no,sit_cap,bus_st,bus_et,own_nm,own_cn,agent_nm,agent_cn ;
			jsonObjRegDetails.put("imei_no", imei_no);
			jsonObjRegDetails.put("state", state.trim().toUpperCase());
			jsonObjRegDetails.put("bus_name", bus_name.trim().toUpperCase());
			jsonObjRegDetails.put("bus_reg_no", bus_reg_no.trim().toUpperCase());
			jsonObjRegDetails.put("sit_cap", sit_cap);
			jsonObjRegDetails.put("bus_st", bus_st);
			jsonObjRegDetails.put("bus_et", bus_et);
			jsonObjRegDetails.put("own_nm", own_nm.trim().toUpperCase());
			jsonObjRegDetails.put("own_cn", own_cn);
			jsonObjRegDetails.put("agent_nm", agent_nm.trim().toUpperCase());
			jsonObjRegDetails.put("agent_cn", agent_cn);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//save details in local db
		regId = dbutil.logRegdDetails(jsonObjRegDetails);
		//send to server for ack and put in serverACK
		getRegistrationAck(jsonObjRegDetails);
		System.out.println(TAG + "serverACK : " + serverACK);
		System.out.println(TAG + " EXIT----> saveRegdDetails");
	}//end of saveRegdDetails

	private void getRegistrationAck(final JSONObject jsonObjRegDetails) {
		System.out.println(TAG + " ENTRY----> getRegistrationAck");


		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(RegistrationActivity.this);
				pd.setTitle("Registration in progress");
				pd.setMessage("Please wait...");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {

				try {


					System.out.println(TAG+"Before sending to server------------------>>>" + jsonObjRegDetails.toString());

					serverACK = Utils.getRegistrationAck(RegistrationActivity.this, jsonObjRegDetails);

					System.out.println(TAG+"Received ACK=====> " + serverACK);


				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
				}
				//if ack is not null then save the ack to local db as status
				if (serverACK != null && !"".equals(serverACK)) {
					dbutil.updateRegdStatus(regId, serverACK);//update ack as status in local db
					mIntent = new Intent(RegistrationActivity.this, AndroidBuildingMusicPlayerActivity.class);
					startActivity(mIntent);
					finish();
				}else{//error fetching server ack so exit from app
					finish();
				}

			}
		};
		task.execute();
		System.out.println(TAG + " EXIT----> getRegistrationAck");
	}//end of getRegistrationAck
	/**
	 * Before saving data this mehtod validates all the ui fields
	 */
	private boolean validateFields() {
		System.out.println(TAG + " ENTRY----> validateFields");
		boolean flag = false ;
		System.out.println(TAG+"bbusname :"+bbusname);
		System.out.println(TAG+"bbusregno :"+bbusregno);
		System.out.println(TAG+"bsitcapacity :"+bsitcapacity);
		System.out.println(TAG+"bst :"+bst);
		System.out.println(TAG+"bet :"+bet);
		System.out.println(TAG+"bownernm :"+bownernm);
		System.out.println(TAG+"bownercn :"+bownercn);
		System.out.println(TAG+"bagentnm :"+bagentnm);
		System.out.println(TAG+"bagentcn :"+bagentcn);
		if(bbusname && bbusregno && bsitcapacity && bst && bet && bownernm && bownercn && bagentnm && bagentcn)
			flag = true ;
		System.out.println(TAG + " EXIT----> validateFields");
		return flag ;
	}//end of validateFields

	/**
	 * Error Dialog Alert
	 *
	 * @param title
	 * @param msg
	 */
	public void showErrorAlert(String title, String msg) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);

		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}
	/***************** Item Selected Listner ******************/
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
		state = arg0.getItemAtPosition(pos).toString();
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
}
