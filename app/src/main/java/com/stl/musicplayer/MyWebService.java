package com.stl.musicplayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
  
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.stl.musicplayer.AndroidBuildingMusicPlayerActivity.MyWebReceiver;
  
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
  
public class MyWebService  extends IntentService{
  
	private static final String LOG_TAG = "MyWebService";
    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";
  
    private String URL = null;
    private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;
  
    public MyWebService() {
        super("MyWebService");
    }
  
    @Override
    protected void onHandleIntent(Intent intent) {
  
        String requestString = intent.getStringExtra(REQUEST_STRING);
        Log.v(LOG_TAG, requestString);
        String responseMessage = "";
         
        try {
        	System.out.println("=========requestString: "+ requestString);
        	
        	//URL = "http://172.16.1.98:8085/AndroidAppUpdate/update";
            
            ArrayList<NameValuePair> namevaluepair = new ArrayList<NameValuePair>();
    		namevaluepair.add(new BasicNameValuePair("reqId", "16"));
    		
            URL = requestString;
            HttpClient httpclient = new DefaultHttpClient();
            HttpParams params = httpclient.getParams();
  
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, WAIT_TIMEOUT);
            ConnManagerParams.setTimeout(params, WAIT_TIMEOUT);
  
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setEntity(new UrlEncodedFormEntity(namevaluepair));
            HttpResponse response = httpclient.execute(httpPost);  
            
            StatusLine statusLine = response.getStatusLine(); 
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseMessage = out.toString();
            }else{
                Log.w("HTTP1:",statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
  
        } catch (ClientProtocolException e) {
            Log.w("HTTP2:",e );
            //responseMessage = e.getMessage();
        } catch (IOException e) {
            Log.w("HTTP3:",e );
            //responseMessage = e.getMessage();
        }catch (Exception e) {
            Log.w("HTTP4:",e );
            //responseMessage = e.getMessage();
        }
        System.out.println("1111111111111111111111111111111111111111111111111111");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyWebReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_MESSAGE, responseMessage);
        sendBroadcast(broadcastIntent);
        
    }
  
}