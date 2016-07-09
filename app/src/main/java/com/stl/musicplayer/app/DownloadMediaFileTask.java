package com.stl.musicplayer.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class DownloadMediaFileTask extends AsyncTask<String, Void, String> {
	private static final String TAG = DownloadMediaFileTask.class.getSimpleName()+": " ;
	Context context;
	String msg;
	
	public DownloadMediaFileTask(Context context){
		this.context = context;
	}
	@Override
	protected void onPreExecute() {
		System.out.println("Media File Downloading Start........ msg: " + msg);
	}

	@Override
	protected String doInBackground(String... arg0) {
		System.out.println("Media File Downloading........ msg: " + msg);
		
		try{
			msg = Utils.downLoadMediaFile(context, "9", Utils.getImeiNo(context));
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		System.out.println(TAG+"Media File Downloading Finish........ msg: " + msg);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				new DownloadMediaFileTask(context).execute("");
			}
		}, 15*60*1000);
    }
}
