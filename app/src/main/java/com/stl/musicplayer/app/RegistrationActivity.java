package com.stl.musicplayer.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.stl.musicplayer.R;
import com.stl.musicplayer.models.DistributorModel;
import com.stl.musicplayer.models.VehicleModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class RegistrationActivity extends Activity implements OnClickListener,AdapterView.OnItemSelectedListener {
	private static final String TAG = "RegistrationActivity : ";
	Button btn_save, btn_exit;
	ProgressDialog pd;
	TextView urlTv;
	Spinner sp_state,sp_owner_nm,sp_bus_regno;
	DatabaseUtil dbutil;
	EditText et_owner_cn,et_busname, et_sit_capacity, et_bus_st, et_bus_et,et_agent_id, et_agent_nm, et_agent_cn;
	String imei_no, state,own_code,own_nm,own_cn,bus_name,bus_reg_no, sit_cap, bus_st, bus_et,agent_id,agent_nm, agent_cn;

	String mTitle, mMsg;
	boolean  bsitcapacity, bst, bet,bagentid, bagentnm, bagentcn;
	JSONObject jsonObjRegDetails;
	String status = "";
	String serverACK = "";

	String mStrDistributorDetails = "" ;
	String mStrVehicleDetails = "" ;

	ArrayList<DistributorModel> mDistributorModels = null ;
	ArrayList<VehicleModel> mVehicleModels = null ;
	long regId;
	Context mContext ;
	Intent mIntent ;
	//Tags used in the JSON String
	public static final String TAG_DISTRIBUTOR_NM = "name";
	public static final String TAG_DISTRIBUTOR_CN = "mobile_no";
	public static final String TAG_DISTRIBUTOR_ID = "distributor_code";
	public static final String TAG_VEHICLE_RN = "vehicle_no";
	public static final String TAG_VEHICLE_NM = "vehicle_name";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_regd);
		mContext = this ;
		dbutil = new DatabaseUtil(RegistrationActivity.this);
		//check for the registration details in local database
		jsonObjRegDetails = dbutil.getRegdDetails();

		//if present then check for the status
		if (null != jsonObjRegDetails) {
			System.out.println(TAG+" Registration details from local ::"+jsonObjRegDetails);
			//Get id and status
			try {
				regId = jsonObjRegDetails.getLong("id");
				status = jsonObjRegDetails.getString("status");
				System.out.println(TAG+" regId: "+regId+" status: "+status);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// if status is 0 get the details of the registration and send to server for updation
			if (Utils.TAG_SUCCESS_ACK.equals(status)) {//user is registered
				mIntent = new Intent(RegistrationActivity.this, AndroidBuildingMusicPlayerActivity.class);
				try {
					mIntent.putExtra("start_date",jsonObjRegDetails.getString("created_on"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				startActivity(mIntent);
				finish();
			}
		} //else {//if totally absent in local db run below code
		System.out.println(TAG + "No registration details available in local db...");
		initUI();
		/****************************** Validate user id availability *******************************/


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
					// Process to get Current Time
					final Calendar c = Calendar.getInstance();
					int mHour = c.get(Calendar.HOUR_OF_DAY);
					int mMinute = c.get(Calendar.MINUTE);
					new TimePickerDialog(RegistrationActivity.this,
							new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view, int hourOfDay,
													  int minute) {
									et_bus_st.setText((hourOfDay<=9 ? "0"+hourOfDay : hourOfDay) + ":" + (minute<=9 ? "0"+minute : minute));
								}
							}, mHour, mMinute, false).show();
				} else {
					String str = et_bus_st.getText().toString();
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
					// Process to get Current Time
					final Calendar c = Calendar.getInstance();
					int mHour = c.get(Calendar.HOUR_OF_DAY);
					int mMinute = c.get(Calendar.MINUTE);
					new TimePickerDialog(RegistrationActivity.this,
							new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view, int hourOfDay,
													  int minute) {
									et_bus_et.setText((hourOfDay<=9 ? "0"+hourOfDay : hourOfDay) + ":" + (minute<=9 ? "0"+minute : minute));
								}
							}, mHour, mMinute, false).show();
				} else {
					String str = et_bus_et.getText().toString();
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
		et_agent_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (hasFocus) {
					// do nothing
				} else {
					String str = et_agent_id.getText().toString().trim();
					if (!str.matches("[A-Za-z]{2}[a-zA-Z0-9\\s]*")) {
						et_agent_id.setError("Invalid Id...");
						et_agent_id.setText("");
						bagentid = false;
					} else {
						bagentid = true;
						agent_id = str;
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
		//}//end of else
	}//end oncreate

	/**
	 * UI initialization method
	 */
	private void initUI() {
		System.out.println(TAG+" ENTRY----> initUI");
		//et_state,et_busname,et_bus_regno,et_sit_capacity,et_bus_st,et_bus_et,et_owner_nm,et_owner_cn,et_agent_nm,et_agent_cn
		//et_state = (EditText)findViewById(R.id.et_state);
		sp_state = (Spinner) findViewById(R.id.sp_state);

		sp_owner_nm = (Spinner) findViewById(R.id.sp_owner_nm);
		et_owner_cn = (EditText) findViewById(R.id.et_owner_cn);
		sp_bus_regno = (Spinner) findViewById(R.id.sp_bus_regno);
		et_busname = (EditText) findViewById(R.id.et_busname);

		et_sit_capacity = (EditText) findViewById(R.id.et_sit_capacity);
		et_bus_st = (EditText) findViewById(R.id.et_bus_st);
		et_bus_et = (EditText) findViewById(R.id.et_bus_et);

		et_agent_id = (EditText) findViewById(R.id.et_agent_id);
		et_agent_nm = (EditText) findViewById(R.id.et_agent_nm);
		et_agent_cn = (EditText) findViewById(R.id.et_agent_cn);

		sp_state.setOnItemSelectedListener(this);
		sp_owner_nm.setOnItemSelectedListener(this);
		sp_bus_regno.setOnItemSelectedListener(this);

		btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);

		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_exit.setOnClickListener(this);
		//bstate,bbusname,bbusregno,bsitcapacity,bbusst,bbuset,bownernm,bownercn,bagentnm,bagentcn
		bsitcapacity = bst = bet = bagentid = bagentnm = bagentcn = false;
		imei_no = Utils.getImeiNo(getApplicationContext());//get imei no
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
					mTitle = "Alert";
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
			jsonObjRegDetails.put("own_code", own_code.trim().toUpperCase());
			jsonObjRegDetails.put("own_nm", own_nm.trim().toUpperCase());
			jsonObjRegDetails.put("own_cn", own_cn);
			jsonObjRegDetails.put("bus_reg_no", bus_reg_no.trim().toUpperCase());
			jsonObjRegDetails.put("bus_name", bus_name.trim().toUpperCase());

			jsonObjRegDetails.put("sit_cap", sit_cap);
			jsonObjRegDetails.put("bus_st", bus_st);
			jsonObjRegDetails.put("bus_et", bus_et);
			jsonObjRegDetails.put("agent_id", agent_id.trim().toUpperCase());
			jsonObjRegDetails.put("agent_nm", agent_nm.trim().toUpperCase());
			jsonObjRegDetails.put("agent_cn", agent_cn);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		//send to server for ack and put in serverACK
		getRegistrationAck(jsonObjRegDetails);
		System.out.println(TAG + "serverACK : " + serverACK);
		System.out.println(TAG + " EXIT----> saveRegdDetails");
	}//end of saveRegdDetails
	/**********************************************************
	 * This method gets the distributors state wise from server
	 * and then populates the spinner element of distributor
	 * @param strStateName
	 * @date 29062016
	 **********************************************************/
	private void getDistributorDetails(final String strStateName) {
		System.out.println(TAG + " ENTRY----> getDistributorDetails");


		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Void doInBackground(Void... arg0) {

				try {


					System.out.println(TAG+"Before sending to server------------------>>>" + strStateName);

					mStrDistributorDetails = Utils.getDistributorDetails(RegistrationActivity.this, strStateName);

					System.out.println(TAG+"Received Response=====> " + mStrDistributorDetails);


				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				//if ack is not null then save the ack to local db as status
				if ("FAILED".equals(mStrDistributorDetails) || "".equals(mStrDistributorDetails )) {
					Toast.makeText(RegistrationActivity.this,"Error fetching distributor list.",Toast.LENGTH_SHORT).show();
					//reinitialize dependent values
					sp_owner_nm.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					own_nm = "" ;
					et_owner_cn.setText("");
					own_cn = "" ;
					own_code = "" ;
					//do for vehicle also
					sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					bus_reg_no = "" ;
					et_busname.setText("");
					bus_name = "" ;
				}else if ("NODATA".equals(mStrDistributorDetails)) {
					mTitle = "Message";
					mMsg = "Currently no distributors available\nContact your admin";
					showErrorAlert(mTitle, mMsg);
					//reinitialize dependent values
					sp_owner_nm.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					own_nm = "" ;
					et_owner_cn.setText("");
					own_cn = "" ;
					own_code = "" ;
					//do for vehicle also
					sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					bus_reg_no = "" ;
					et_busname.setText("");
					bus_name = "" ;

				}else{
					//populate distributor spinner
					//convert response to jsonobject
					try {
						JSONObject jboj = new JSONObject(mStrDistributorDetails);
						//System.out.println(TAG+"jboj---------->"+jboj);
						//get the jsonarray from the key data
						JSONArray jaDistributorData  = jboj.getJSONArray("data");
						if(jaDistributorData.length() > 0){
							getDistributors(jaDistributorData);
							//populate the spinner with key value
							ArrayAdapter<DistributorModel> adapter =
									new ArrayAdapter<DistributorModel>(
											RegistrationActivity.this,
											android.R.layout.simple_spinner_item,
											mDistributorModels);
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							sp_owner_nm.setAdapter(adapter);

							System.out.println(TAG+"spinner length----->"+sp_owner_nm.getChildCount());
						}else{//no data available update the owner spinner
							//reinitialize dependent values
							sp_owner_nm.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
							own_nm = "" ;
							et_owner_cn.setText("");
							own_cn = "" ;
							own_code = "" ;
							//do for vehicle also
							sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
							bus_reg_no = "" ;
							et_busname.setText("");
							bus_name = "" ;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}




				}

			}
		};
		task.execute();
		System.out.println(TAG + " EXIT----> getDistributorDetails");
	}//end of getDistributorDetails

	/***************************************************************
	 * This method populates the distributor arraylist from server
	 * JSONArray response
	 * @param j
	 ****************************************************************/
	private void getDistributors(JSONArray j){
		String methodname = "getDistributors" ;
		System.out.println(TAG + " ENTRY----> "+methodname);
		//"data":[{"distributor_code":"D0000000042","name":"SAUDAGAR PATRA","mobile_no":"9853123052"}]
		DistributorModel model = null ;
		mDistributorModels = new ArrayList<DistributorModel>();
		JSONObject jobj = null ;

		//Traversing through all the items in the json array
		for(int i=0;i<j.length();i++){

			try {
				//Getting json object
				jobj = j.getJSONObject(i);
				model = new DistributorModel(
						null!=jobj.get(TAG_DISTRIBUTOR_ID)?jobj.get(TAG_DISTRIBUTOR_ID).toString():"",
						null!=jobj.get(TAG_DISTRIBUTOR_CN)?jobj.get(TAG_DISTRIBUTOR_CN).toString():"",
						null!=jobj.get(TAG_DISTRIBUTOR_NM)?jobj.get(TAG_DISTRIBUTOR_NM).toString():""

				);
				mDistributorModels.add(model);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}//end for loop
		System.out.println(TAG + " EXIT----> "+methodname);
	}//end of getDistributors
	/********************************************************
	 * This method gets the vehicle lists and update in ui
	 * using the distributor code
	 * @param distributor_code
	 *********************************************************/
	private void getVehicleDetails(final String distributor_code) {
		String methodname = "getVehicleDetails" ;
		System.out.println(TAG + " ENTRY----> "+methodname);


		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Void doInBackground(Void... arg0) {

				try {


					System.out.println(TAG+"Before sending to server------------------>>>" + distributor_code);

					mStrVehicleDetails = Utils.getVehicleDetails(RegistrationActivity.this, distributor_code);

					System.out.println(TAG+"Received Response=====> " + mStrVehicleDetails);


				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				//if ack is not null then save the ack to local db as status
				if ("FAILED".equals(mStrVehicleDetails) || "".equals(mStrVehicleDetails )) {


					Toast.makeText(RegistrationActivity.this,"Error fetching vehicle list.",Toast.LENGTH_SHORT).show();
					//reinitialize dependent values
					sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					bus_reg_no = "" ;
					et_busname.setText("");
					bus_name = "" ;
				}else if ("NODATA".equals(mStrVehicleDetails)) {
					mTitle = "Message";
					mMsg = "Currently no vehicles mapped\nContact your admin";
					showErrorAlert(mTitle, mMsg);
					//reinitialize dependent values
					sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					bus_reg_no = "" ;
					et_busname.setText("");
					bus_name = "" ;
				}else{
					//populate distributor spinner
					//convert response to jsonobject
					try {
						JSONObject jboj = new JSONObject(mStrVehicleDetails);
						//System.out.println(TAG+"jboj---------->"+jboj);
						//get the jsonarray from the key data
						JSONArray jaVehicleData  = jboj.getJSONArray("data");
						if(jaVehicleData.length() > 0){
							getVehicles(jaVehicleData);
							//Setting adapter to show the items in the spinner
							//sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, mVehicles));
							ArrayAdapter<VehicleModel> adapter =
									new ArrayAdapter<VehicleModel>(
											RegistrationActivity.this,
											android.R.layout.simple_spinner_item,
											mVehicleModels);
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							sp_bus_regno.setAdapter(adapter);
							System.out.println(TAG+"spinner length----->"+sp_bus_regno.getChildCount());
						}else{//no data available update the owner spinner
							sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
							bus_reg_no = "" ;
							et_busname.setText("");
							bus_name = "" ;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}




				}

			}
		};
		task.execute();
		System.out.println(TAG + " EXIT----> "+methodname);
	}//end of getVehicleDetails
	/***************************************************************
	 * This method populates the vehicle arraylist from server
	 * JSONArray response
	 * @param j
	 ****************************************************************/
	private void getVehicles(JSONArray j){
		String methodname = "getVehicles" ;
		System.out.println(TAG + " ENTRY----> "+methodname);
		//"data":[{"name":"SAIKRUPA","number":"OD02Q2155"}]
		VehicleModel model = null ;
		mVehicleModels = new ArrayList<VehicleModel>();
		JSONObject jobj = null ;

		//Traversing through all the items in the json array
		for(int i=0;i<j.length();i++){

			try {
				//Getting json object
				jobj = j.getJSONObject(i);
				model = new VehicleModel(
						null!=jobj.get(TAG_VEHICLE_NM)?jobj.get(TAG_VEHICLE_NM).toString():"",
						null!=jobj.get(TAG_VEHICLE_RN)?jobj.get(TAG_VEHICLE_RN).toString():""

				);
				mVehicleModels.add(model);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}//end for loop
		System.out.println(TAG + " EXIT----> "+methodname);
	}//end of getVehicles



	/**********************************************************
	 * This method used to send registration details to server
	 * and then retrieves the response/ack from server
	 * and then update the status in local database
	 * @param jsonObjRegDetails
	 **********************************************************/
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
				if (serverACK != null && !"".equals(serverACK) && Utils.TAG_SUCCESS_ACK.equals(serverACK)) {
					//dbutil.updateRegdStatus(regId, serverACK);//update ack as status in local db
					//save details in local db
					try {
						jsonObjRegDetails.put("status", Utils.TAG_SUCCESS_ACK);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					regId = dbutil.logRegdDetails(jsonObjRegDetails);
					mIntent = new Intent(RegistrationActivity.this, AndroidBuildingMusicPlayerActivity.class);
					startActivity(mIntent);
					finish();
				}else if(Utils.TAG_BLOCK_ACK.equals(serverACK)){//error fetching server ack so exit from app
					//finish();
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this);
					alertDialog.setTitle("Alert");
					alertDialog.setMessage("Operations Blocked\nContact your Admin");

					alertDialog.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//exit application
									finish();
									dialog.dismiss();

								}
							});

					alertDialog.show();
				}else{//error fetching server ack so exit from app
					//finish();
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this);
					alertDialog.setTitle("Message");
					alertDialog.setMessage("Registration Failed");

					alertDialog.setPositiveButton("RETRY",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//again registration request with same data
									getRegistrationAck(jsonObjRegDetails);
									dialog.dismiss();

								}
							});
					alertDialog.setNegativeButton("CANCEL",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					alertDialog.show();
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
		System.out.println(TAG+"bsitcapacity :"+bsitcapacity);
		System.out.println(TAG+"bst :"+bst);
		System.out.println(TAG+"bet :"+bet);
		System.out.println(TAG + "bagentid :" + bagentid);
		System.out.println(TAG + "bagentnm :" + bagentnm);
		System.out.println(TAG + "bagentcn :" + bagentcn);
		if(bsitcapacity && bst && bet && bagentid && bagentnm && bagentcn)
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
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		//Spinner spinner = (Spinner) parent;
		switch(parent.getId()){
			case R.id.sp_state:
				state = parent.getItemAtPosition(pos).toString();
				System.out.println(TAG + "state::" + state);
				//go for distributors/owners and populate owner names & cn
				getDistributorDetails(state);
				break;
			case R.id.sp_owner_nm:

				DistributorModel d = (DistributorModel)parent.getSelectedItem();

				own_nm =  d.getName() ;
				own_cn = d.getMobile_no() ;
				own_code = d.getDistributor_code() ;
				et_owner_cn.setText(d.getMobile_no());
				System.out.println(TAG + "own_code::" + d.getDistributor_code());
				System.out.println(TAG + "own_nm::" + own_nm);
				System.out.println(TAG+"own_cn::"+own_cn);
				//go for vehicle details and populate vehicle regnos
				getVehicleDetails(d.getDistributor_code());

				break;
			case R.id.sp_bus_regno:
				VehicleModel v = (VehicleModel)parent.getSelectedItem();

				bus_reg_no = v.getNumber();
				bus_name =  v.getName() ;
				et_busname.setText(v.getName());
				System.out.println(TAG + "bus_reg_no::" + bus_reg_no);
				System.out.println(TAG + "bus_name::" + bus_name);

				break;
			default :
				break;
		}


	}



	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
}
