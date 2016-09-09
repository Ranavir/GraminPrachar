package com.stl.musicplayer.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.stl.musicplayer.R;
import com.stl.musicplayer.models.ReportModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint({ "NewApi", "Wakelock" })
public class DashboardActivity extends Activity {
    private static final String TAG = DashboardActivity.class.getSimpleName()+" : ";

    Context mContext ;
    Intent mIntent ;
    JSONObject mJSONObject;

    TextView tv_report_type ;
    TextView tv_date;
    Button btnChangeDate ;
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
    SimpleDateFormat sdf ;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mContext = this ;
        mDatabaseUtil = new DatabaseUtil(DashboardActivity.this);
        //check for the registration details in local database
        mJSONObject = mDatabaseUtil.getRegdDetails();
        try {
            mStDate = mJSONObject.getString("created_on");

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
            Long time ;
            if(null != mStDate){
                time = sdf.parse(mStDate).getTime();
            }else{
                time = sdf.parse(sdf.format(new Date())).getTime();//current time
            }
            System.out.println(TAG+"mStDate::"+mStDate);
            System.out.println(TAG+"time::"+time);
            //mCal.setMinDate(new Date(time).getTime());//set the registration date as min date
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }

        tv_report_type = (TextView)findViewById(R.id.tv_report_type);
        tv_date = (TextView)findViewById(R.id.tv_date);
        btnChangeDate = (Button)findViewById(R.id.btnChangeDate);
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
        //showDailyReport(tv_date.getText().toString());
        showDailyReport();


        btnChangeDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialog(DATE_DIALOG_ID);

            }

        });
    }//end onCreate

    /*private void showDailyReport(String date) {
        System.out.println(TAG+"Begin showDailyReport");
        System.out.println(TAG+"date = "+date);
        JSONObject obj = null ;
        mDatabaseUtil = new DatabaseUtil(getApplicationContext());
        try {

            //call to reporting accordingly
            //Toast.makeText(getApplicationContext(),"Show day wise report....",Toast.LENGTH_SHORT).show();
            *//****************************************************//*
            obj = mDatabaseUtil.getDailyReports(date);
            //Toast.makeText(getApplicationContext(),"obj::"+obj,Toast.LENGTH_SHORT).show();

            *//****************************************************//*

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
    }*/

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                mDatePickerDialog = new DatePickerDialog(this, datePickerListener,
                        year, month-1,day);

                try {
                    mDatePickerDialog.getDatePicker().setMinDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(mStDate).getTime());
                    mDatePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return mDatePickerDialog;
        }
        return null;
    }
    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth + 1 ;
            day = selectedDay;

            /********************************************************************
             1 - set date field value(date OR month) according to type
             2 - Call to reporting with proper reporting type and (date OR month)
             *********************************************************************/
            //get the type of from the spinner
            String choosenDate = new StringBuilder().append((day) <= 9 ? "0" + (day) : (day))
                    .append("-").append((month) <= 9 ? "0" + (month) : (month)).append("-").append(year).toString();
            // set selected date into textview
            tv_date.setText(choosenDate);//dd-mm-yyyy

            //showDailyReport(choosenDate);
            showDailyReport();
        }
    };

    /**
     * This method used to show daily report of songs and adds
     * from server side
     */
    public void showDailyReport() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            String date = tv_date.getText().toString() ;
            String NO_VAL = "00:00:00" ;
            JSONObject obj = null ;
            String msg;
            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(DashboardActivity.this);
                pd.setTitle("Fetching Daily Report");
                pd.setMessage("Please wait...");
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                pd.show();
                tv_td2.setText(NO_VAL);
                tv_td3.setText(NO_VAL);
                tv_td4.setText(NO_VAL);
                tv_td5.setText("0");
                tv_td41.setText("0");

                tv_td7.setText(NO_VAL);
                tv_td8.setText(NO_VAL);
                tv_td9.setText(NO_VAL);
                tv_td10.setText("0");
                tv_td91.setText("0");
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    if(Utils.checkiInternet(getApplicationContext())){
                        TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                        mngr.getDeviceId();
                        String UID = mngr.getDeviceId();
                        obj = Utils.getDailyReport(getApplicationContext(),date);
                    }else{
                        System.out.println(TAG+"1. Internet Connection Not Present");
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

                //msg = "All data uploaded successfully";
                try {
                    if(null != obj){
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }

            }
        };
        task.execute();
    }//end of showDailyReport
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
}
