package com.stl.musicplayer.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.List;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint({ "NewApi", "Wakelock" })
public class RegistrationActivity extends Activity implements OnClickListener,AdapterView.OnItemSelectedListener {
	private static final String TAG = "RegistrationActivity : ";
	Button btn_save,btn_exit,btn_change_URL;
	ProgressDialog pd;

	Spinner sp_state,sp_owner_nm,sp_bus_regno;
	DatabaseUtil dbutil;
	EditText et_owner_cn,et_busname, et_sit_capacity, et_bus_st, et_bus_et,et_agent_id ;
	String imei_no, state,own_code,own_nm,own_cn,bus_name,bus_reg_no, sit_cap, bus_st, bus_et,agent_id ;

	String mTitle, mMsg;
	JSONObject jsonObjRegDetails;
	String status = "";
	String serverACK = "";

	String mStrStates = "" ;
	String mStrDistributorDetails = "" ;
	String mStrVehicleDetails = "" ;

	List<String> mStateList = null ;
	List<DistributorModel> mDistributorModels = null ;
	List<VehicleModel> mVehicleModels = null ;
	public static boolean bIPPingStatus = false ;//Can access
	long regId;
	Context mContext ;
	Intent mIntent ;
	//Tags used in the JSON String
	public static final String TAG_DISTRIBUTOR_NM = "name";
	public static final String TAG_DISTRIBUTOR_CN = "mobile_no";
	public static final String TAG_DISTRIBUTOR_ID = "distributor_code";
	public static final String TAG_VEHICLE_RN = "vehicle_no";
	public static final String TAG_VEHICLE_NM = "vehicle_name";
	public static final String TAG_SEAT_CAP = "sitting_capacity";
	public static final String TAG_ST = "start_time";
	public static final String TAG_ET = "end_time";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println(TAG + "### onCreate()");
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		//actionBar.setDisplayShowHomeEnabled(false);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		/*this.requestWindowFeature(Window.FEATURE_ACTION_BAR);*/
		setContentView(R.layout.activity_regd);

		mContext = this ;
		initUI();
		dbutil = new DatabaseUtil(RegistrationActivity.this);
		System.out.println(TAG+"bIPPingStatus::"+bIPPingStatus);
	}//end oncreate

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println(TAG + "### onResume()");


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
		}//end if
		System.out.println(TAG + "No registration details available in local db...");

		/****************************** Validate user id availability *******************************/
		//Get states list and reinitialize the page after IPChange or in case of any error in fetching state list
		if(!bIPPingStatus) {//Error fetching server data previously so try to start from begining
			getStates();
		}
	}//end onResume

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println(TAG + "### onDestroy()");
		bIPPingStatus = false ;
	}

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


		sp_state.setOnItemSelectedListener(this);
		sp_owner_nm.setOnItemSelectedListener(this);
		sp_bus_regno.setOnItemSelectedListener(this);

		btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);

		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_exit.setOnClickListener(this);

		btn_change_URL = (Button) findViewById(R.id.btn_change_URL);
		btn_change_URL.setOnClickListener(this);
		//bstate,bbusname,bbusregno,bsitcapacity,bbusst,bbuset,bownernm,bownercn,bagentnm,bagentcn
		imei_no = Utils.getImeiNo(mContext);//get imei no

		et_agent_id.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
										  int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				String s = arg0.toString();
				if (!s.equals(s.toUpperCase())) {
					s = s.toUpperCase();
					et_agent_id.setText(s);
					et_agent_id.setSelection(s.length());
				}
			}
		});


		System.out.println(TAG+" EXIT----> initUI");
	}//end initUI


	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_save:
				et_agent_id.clearFocus();
				//Toast.makeText(getBaseContext(),"All fields valid...",Toast.LENGTH_SHORT).show();
				if (Utils.checkiInternet(mContext)) {
					//save in local db and send to server for getting status
					if (validateFields()) {
						saveRegdDetails();
					}

				} else {
					//give voice message of toast
					mTitle = "Message";
					mMsg = "Internet connectivity unavailable!!!";
					showErrorAlert(mTitle, mMsg);
				}
				break;
			case R.id.btn_exit:
				finish();
				break;
			case R.id.btn_change_URL:
				startActivity(new Intent(mContext, ChangeUrlActivity.class));
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
		jsonObjRegDetails = new JSONObject();//initialize with data
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

		} catch (JSONException e) {
			e.printStackTrace();
		}

		//send to server for ack and put in serverACK
		getRegistrationAck();
		System.out.println(TAG + "serverACK : " + serverACK);

		System.out.println(TAG + " EXIT----> saveRegdDetails");
	}//end of saveRegdDetails
	/**********************************************************
	 * This method gets the states from server
	 * and then populates the spinner element of state
	 * @date 29082016
	 **********************************************************/
	private void getStates() {
		System.out.println(TAG + " ENTRY----> getStates");


		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Void doInBackground(Void... arg0) {

				try {
					mStrStates = Utils.getStates(RegistrationActivity.this);

					System.out.println(TAG+"Received Response=====> " + mStrStates);


				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				//if ack is not null then save the ack to local db as status
				if ("FAILED".equals(mStrStates) || "".equals(mStrStates )) {
					Toast.makeText(RegistrationActivity.this,"Error fetching State list.",Toast.LENGTH_SHORT).show();
					//reinitialize dependent values
					sp_state.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
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
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");

				}else if ("NODATA".equals(mStrStates)) {
					mTitle = "Message";
					mMsg = "Currently no States Registered\nContact your admin";
					showErrorAlert(mTitle, mMsg);
					//reinitialize dependent values
					sp_state.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
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
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");

				}else{
					bIPPingStatus = true ;//Successfully get Server response
					//populate distributor spinner
					//convert response to jsonobject
					try {
						JSONObject jboj = new JSONObject(mStrStates);
						//System.out.println(TAG+"jboj---------->"+jboj);
						//get the jsonarray from the key data
						JSONArray jaStates  = jboj.getJSONArray("data");
						if (jaStates.length() > 0){
							getStateList(jaStates);
							//populate the spinner with key value
							ArrayAdapter<String> adapter =
									new ArrayAdapter<String>(
											RegistrationActivity.this,
											android.R.layout.simple_spinner_item,
											mStateList);
							adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							sp_state.setAdapter(adapter);

							System.out.println(TAG+"spinner length----->"+sp_state.getChildCount());
						}else{//no data available update the owner spinner
							//reinitialize dependent values
							sp_state.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
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
							sit_cap = "" ;
							et_sit_capacity.setText("");
							bus_st = "" ;
							et_bus_st.setText("HH:MM");
							bus_et = "" ;
							et_bus_et.setText("HH:MM");


						}
					} catch (JSONException e) {
						e.printStackTrace();
					}




				}

			}
		};
		task.execute();
		System.out.println(TAG + " EXIT----> getDistributorDetails");
	}//end of getStates
	/***************************************************************
	 * This method populates the State arraylist from server
	 * JSONArray response
	 * @param j
	 ****************************************************************/
	private void getStateList(JSONArray j){
		String methodname = "getStateList" ;
		System.out.println(TAG + " ENTRY----> "+methodname);
		JSONObject jobj = null ;
		String state = "" ;
		mStateList = new ArrayList<String>() ;
		//Please select option
		mStateList.add("--Select--");
		//Traversing through all the items in the json array
		for(int i=0;i<j.length();i++){
			try {
				//Getting json object
				state = j.get(i).toString();

			} catch (JSONException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			mStateList.add(state);
		}//end for loop
		System.out.println(TAG + " EXIT----> "+methodname);
	}//end of getStateList
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
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");
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
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");
				}else{
					//populate distributor spinner
					//convert response to jsonobject
					try {
						JSONObject jboj = new JSONObject(mStrDistributorDetails);
						//System.out.println(TAG+"jboj---------->"+jboj);
						//get the jsonarray from the key data
						JSONArray jaDistributorData  = jboj.getJSONArray("data");
						if (jaDistributorData.length() > 0){
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
							sit_cap = "" ;
							et_sit_capacity.setText("");
							bus_st = "" ;
							et_bus_st.setText("HH:MM");
							bus_et = "" ;
							et_bus_et.setText("HH:MM");
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
		JSONObject jobj = null ;
		mDistributorModels = new ArrayList<DistributorModel>() ;
		//Please select option
		model = new DistributorModel("","","--Select--");
		mDistributorModels.add(model);
		//Traversing through all the items in the json array
		for(int i=0;i<j.length();i++){
			model = new DistributorModel("","","");
			try {
				//Getting json object
				jobj = j.getJSONObject(i);
				model.setDistributor_code(null!=jobj.get(TAG_DISTRIBUTOR_ID)?jobj.get(TAG_DISTRIBUTOR_ID).toString():"");
				model.setMobile_no(null != jobj.get(TAG_DISTRIBUTOR_CN) ? jobj.get(TAG_DISTRIBUTOR_CN).toString() : "");
				model.setName(null!=jobj.get(TAG_DISTRIBUTOR_NM)?jobj.get(TAG_DISTRIBUTOR_NM).toString():"");
				/*model = new DistributorModel(
						null!=jobj.get(TAG_DISTRIBUTOR_ID)?jobj.get(TAG_DISTRIBUTOR_ID).toString():"",
						null!=jobj.get(TAG_DISTRIBUTOR_CN)?jobj.get(TAG_DISTRIBUTOR_CN).toString():"",
						null!=jobj.get(TAG_DISTRIBUTOR_NM)?jobj.get(TAG_DISTRIBUTOR_NM).toString():""

				);*/

			} catch (JSONException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			mDistributorModels.add(model);
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
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");
				}else if ("NODATA".equals(mStrVehicleDetails)) {
					mTitle = "Message";
					mMsg = "Currently no vehicles mapped\nContact your admin";
					showErrorAlert(mTitle, mMsg);
					//reinitialize dependent values
					sp_bus_regno.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this, android.R.layout.simple_spinner_item, new String[]{}));
					bus_reg_no = "" ;
					et_busname.setText("");
					bus_name = "" ;
					sit_cap = "" ;
					et_sit_capacity.setText("");
					bus_st = "" ;
					et_bus_st.setText("HH:MM");
					bus_et = "" ;
					et_bus_et.setText("HH:MM");
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
							sit_cap = "" ;
							et_sit_capacity.setText("");
							bus_st = "" ;
							et_bus_st.setText("HH:MM");
							bus_et = "" ;
							et_bus_et.setText("HH:MM");
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
		JSONObject jobj = null ;
		mVehicleModels = new ArrayList<VehicleModel>() ;

		//please select option
		model = new VehicleModel("","--Select--","","",0);
		mVehicleModels.add(model);
		//Traversing through all the items in the json array
		for(int i=0;i<j.length();i++){
			model = new VehicleModel("","","00:00","00:00",0);
			try {
				//Getting json object
				jobj = j.getJSONObject(i);
				model.setVehicle_no(null!=jobj.get(TAG_VEHICLE_RN)?jobj.get(TAG_VEHICLE_RN).toString():"");
				model.setVehicle_name(null!=jobj.get(TAG_VEHICLE_NM)?jobj.get(TAG_VEHICLE_NM).toString():"");
				model.setStart_time(null!=jobj.get(TAG_ST)?jobj.get(TAG_ST).toString():"");
				model.setEnd_time(null!=jobj.get(TAG_ET)?jobj.get(TAG_ET).toString():"");
				model.setSitting_capacity(null!=jobj.get(TAG_SEAT_CAP)?Integer.parseInt(jobj.get(TAG_SEAT_CAP).toString()):0);

				//call the other constructor


			} catch (JSONException e) {
				e.printStackTrace();
			}catch(NumberFormatException nfe){
				nfe.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			mVehicleModels.add(model);
		}//end for loop
		System.out.println(TAG + " EXIT----> " + methodname);
	}//end of getVehicles



	/**********************************************************
	 * This method used to send registration details to server
	 * and then retrieves the response/ack from server
	 * and then update the status in local database
	 **********************************************************/
	private void getRegistrationAck() {
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
									getRegistrationAck();
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
		//EditText et_owner_cn,et_busname, et_sit_capacity, et_bus_st, et_bus_et,et_agent_id ;
		//String imei_no, state,own_code,own_nm,own_cn,bus_name,bus_reg_no, sit_cap, bus_st, bus_et,agent_id ;
		if(null == state || "".equals(state) || "--Select--".equals(state)){
			showErrorAlert("Error","Choose State");
			return false ;
		}

		if(null == own_nm || "".equals(own_nm) || "--Select--".equals(own_nm)){
			showErrorAlert("Error","Choose Owner Name");
			return false ;
		}
		if(null == bus_reg_no || "".equals(bus_reg_no) || "--Select--".equals(bus_reg_no)){
			showErrorAlert("Error","Choose GraminPRACHAR Point");
			return false ;
		}

		//validating agent id
		agent_id = et_agent_id.getText().toString().trim() ;
		if(null == agent_id || "".equals(agent_id)){
			showErrorAlert("Error","Please Enter Your Agent Id");
			return false ;
		}
		if (!agent_id.matches("[A-Za-z]{2}[a-zA-Z0-9\\s]*")) {
			//et_agent_id.setError("Invalid Id...");
			showErrorAlert("Error","Invalid Agent ID");
			et_agent_id.setText("");
			return false ;
		}

		System.out.println(TAG + " EXIT----> validateFields");
		return true ;
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

				bus_reg_no = v.getVehicle_no();
				bus_name =  v.getVehicle_name() ;
				sit_cap = v.getSitting_capacity()+"" ;
				bus_st = v.getStart_time() ;
				bus_et = v.getEnd_time() ;
				et_busname.setText(bus_name);
				et_sit_capacity.setText(sit_cap);
				et_bus_st.setText(bus_st);
				et_bus_et.setText(bus_et);
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
