package com.stl.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

public class DatabaseUtil {
	private static final String DB_NAME = "SMARTCAMP";
	private static final int DB_VERSION = 11;

	SQLiteDatabase db;
	MyHelper helper;
	public DatabaseUtil(Context context){
		 System.out.println("context "+context);
		 helper = new MyHelper(context);
		 db = helper.getWritableDatabase();
	}

	public class MyHelper extends SQLiteOpenHelper{

		public MyHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("create db----");
			try{
				/*db.execSQL("CREATE TABLE  if not exists regd_details ("+
						  "id integer PRIMARY KEY,"+
						  "first_name text,"+
						  "last_name text," +
						  "phone text," +
						  "email text,"+
						  "regd_date text," +
						  "end_date text," +
						  "duration text," +
						  "imei_no text);"
				);*/
				db.execSQL("CREATE TABLE  if not exists regd_details ("+
								"id integer PRIMARY KEY,"+
								"imei_no text,"+
								"state text,"+
								"bus_name text," +
								"bus_reg_no text," +
								"sit_cap text,"+
								"bus_st text," +
								"bus_et text," +
								"own_nm text," +
								"own_cn text," +
								"agent_nm text," +
								"agent_cn text,"+
								"status text,"+
								"created_by text,"+
								"created_on text,"+
								"updated_on text"+
							");"
				);
				db.execSQL("CREATE TABLE  if not exists play_list ("+
						  "id integer PRIMARY KEY," +
						  "playlist_id text,"+
						  "song_id integer,"+
						  "song_name text,"+
						  "start_date text," +
						  "end_date text," +
						  "created_date text);"
				);				
				db.execSQL("CREATE TABLE  if not exists play_song_details ("+
						  "id integer PRIMARY KEY," +
						  "song_name text,"+
						  "play_date date,"+
						  "start_time text,"+
						  "end_time text,"+
						  "latitude text,"+
						  "longitude text," +
						  "end_latitude text," +
						  "end_longitude text,"+
						  "imei_no text,"+
						  "us text," +
						  "plylist_id text);"
				);
				db.execSQL("CREATE TABLE  if not exists play_song_pause_detail ("+
						  "id integer PRIMARY KEY," +
						  "play_seq_id integer," +
						  "song_name text,"+
						  "play_date date,"+
						  "start_time text,"+
						  "pause_time text,"+
						  "resume_time text," +
						  "pause_reason text,"+
						  "imei_no text,"+
						  "us text," +
						  "plylist_id text);"
				);
				db.execSQL("CREATE TABLE  if not exists jack_details ("+
						  "id integer PRIMARY KEY," +
						  "play_seq_id integer," +
						  "song_name text,"+
						  "play_date date,"+
						  "start_time text,"+
						  "jack_status text,"+
						  "alter_time text,"+
						  "imei_no text,"+
						  "us text," +
						  "plylist_id text);"
				);
				db.execSQL("CREATE TABLE  if not exists song_volume_label ("+
						  "id integer PRIMARY KEY," +
						  "play_seq_id integer," +
						  "song_name text,"+
						  "play_date date,"+
						  "start_time text,"+
						  "volume_label integer,"+
						  "volume_change_time text,"+			  
						  "imei_no text,"+
						  "us text," +
						  "plylist_id text);"
				);
				db.execSQL("create table if not exists bus_img_details("+
						  "id integer PRIMARY KEY," +
						  "image_name text,"+
						  "capture_time text,"+
						  "us text);"						  
				);	
				
				db.execSQL("create table if not exists flash_news(id integer PRIMARY KEY, news_name text, mapping_date text, is_downloaded text, download_time text);");
				
				db.execSQL("create table if not exists play_song_index(id integer, play_date text, song_index integer)");
				
				db.execSQL("create table if not exists host_detail(ip_address text, port_no text)");
				//db.execSQL("insert or replace into host_detail(ip_address, port_no) values('208.109.208.91','80')");
				//db.execSQL("insert or replace into host_detail(ip_address, port_no) values('192.168.0.124','8080')");
				db.execSQL("insert or replace into host_detail(ip_address, port_no) values('192.168.0.38','8080')");
				
				String query = String.format("insert or replace into play_song_index(id, play_date, song_index) values('1','%s','0')", Utils.getDate("yyyy-MM-dd"));
				db.execSQL(query);
				
				
			}catch(Exception e){
				e.printStackTrace();
				//Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
			}

			System.out.println("create db success");
		}
		

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(MyHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			//db.execSQL("drop table if exists play_song_details" );
			//db.execSQL("drop table if exists play_song_pause_detail" );
			//db.execSQL("drop table if exists jack_details" );
			//db.execSQL("drop table if exists song_volume_label" );
			
			//db.execSQL("ALTER TABLE play_song_details ADD COLUMN plylist_id text;");
			//db.execSQL("ALTER TABLE play_song_pause_detail ADD COLUMN plylist_id text;");
			//db.execSQL("ALTER TABLE jack_details ADD COLUMN plylist_id text;");
			//db.execSQL("ALTER TABLE song_volume_label ADD COLUMN plylist_id text;");
			
			//db.execSQL("ALTER TABLE song_volume_label ADD COLUMN plylist_id text;");
			
			
			try{
				db.execSQL("delete from host_detail");
				db.execSQL("ALTER TABLE play_list ADD COLUMN created_date text;");
			}catch(Exception e){
				e.printStackTrace();
			}
			onCreate(db);			
		}

	}
	public void truncateTable(String table){
		db.execSQL("DELETE FROM " + table);
	}
	public void close(){
		helper.close();
	}
	
	public void deleteFlashNewsAfterPlay(long id){		
		db.execSQL("DELETE FROM flash_news where id='" + id + "'");
	}
	public long logFlashNews(JSONObject news, String downloadTime){
		//flash_news(id integer PRIMARY KEY, news_name text, mapping_date text, is_downloaded text, download_time text)
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{
			ContentValues value = new ContentValues();
			value.put("id",  	        news.getString("id").trim());
			value.put("news_name",  	news.getString("news_name").trim());
			value.put("mapping_date",  	news.getString("mapping_date").trim());
			value.put("is_downloaded",  "F");
			rowId = db.insert("flash_news", null, value);		
		}catch(Exception e){
			e.printStackTrace();
		}
		return rowId;
	}	
	public int updateFlashNewsDownloadStatus(long id, String news_name, String is_downloaded) {
		//flash_news(id integer PRIMARY KEY, news_name text, mapping_date text, is_downloaded text, download_time text)
		db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
        values.put("is_downloaded", is_downloaded);
        values.put("download_time", Utils.getDate("yyyy-MM-dd HH:mm:ss"));
        return db.update("flash_news", values, "id = ? and news_name = ?", new String[] { String.valueOf(id), String.valueOf(news_name) });
	}	
	public ArrayList<HashMap<String, String>> getDownloadedFlashNews(String downloadStatus){
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		String path;
		if(isSDPresent){
			path = Utils.androidBuildingMusicPlayerActivity.getExternalCacheDir().getAbsolutePath() + "/FlashNews";
	    }else{
	        path = Utils.androidBuildingMusicPlayerActivity.getFilesDir() + "/FlashNews";
	    }
		System.out.println("Flash News Path :: " + path);
		final String MEDIA_PATH = Environment.getExternalStorageDirectory().toString() + "/FlashNews";		
		
		ArrayList<HashMap<String, String>> newsList = new ArrayList<HashMap<String, String>>();
		db = helper.getWritableDatabase();
		try{
			//flash_news(id integer PRIMARY KEY, news_name text, is_downloaded text, download_time text)
			String qry = String.format("select * from flash_news where is_downloaded='%s' order by mapping_date, id", downloadStatus);
			System.out.println("query : "+qry);
			Cursor c = db.rawQuery(qry, null);
			if(c.moveToFirst()){
				do{
					HashMap<String, String> news = new HashMap<String, String>();
					String id = c.getString(c.getColumnIndex("id"));
					String news_name = c.getString(c.getColumnIndex("news_name"));
					
					news.put("id", id);
					news.put("news_name", news_name);
					news.put("news_path", MEDIA_PATH + "/" + news_name);
					newsList.add(news);
				}while(c.moveToNext());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return newsList;		
	}
	
	
	
	
	public long logRegdDetails(JSONObject input) {
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{
			ContentValues value = new ContentValues();
			/*value.put("first_name",  	input.getString("fname").trim());
			value.put("last_name", 		input.getString("lname"));
			value.put("phone", 			input.getString("phone").trim());
			value.put("email",  		input.getString("email").trim());
			value.put("regd_date",  	input.getString("regd_date").trim());
			value.put("end_date",  		input.getString("end_date").trim());
			value.put("duration",  		input.getString("duration").trim());
			value.put("imei_no",  		input.getString("imei").trim());*/

			value.put("imei_no",  		input.getString("imei_no").trim());
			value.put("state",  	    input.getString("state").trim());
			value.put("bus_name", 		input.getString("bus_name"));
			value.put("bus_reg_no",     input.getString("bus_reg_no").trim());
			value.put("sit_cap",  		input.getString("sit_cap").trim());
			value.put("bus_st",  		input.getString("bus_st").trim());
			value.put("bus_et",  		input.getString("bus_et").trim());
			value.put("own_nm",  		input.getString("own_nm").trim());
			value.put("own_cn",  		input.getString("own_cn").trim());
			value.put("agent_nm",  		input.getString("agent_nm").trim());
			value.put("agent_cn",  		input.getString("agent_cn").trim());
			value.put("status",  		"1");//change to 0 after server side registration servlet implementation
			value.put("created_by",  	input.getString("agent_nm").trim());
			value.put("created_on",  	Utils.getDate("yyyy-MM-dd HH:mm:ss"));

			rowId=db.insert("regd_details", null, value);
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			db.close();
		}
		return rowId;
	}//end of logRegdDetails
	public int updateRegdStatus(long id, String status) {
		int count = 0 ;
		try{
			db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("status", status);
			values.put("updated_on", Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			count = db.update("regd_details", values, "id = ?", new String[] { String.valueOf(id) }) ;
		}finally{
			db.close();
		}
		return count ;
	}//end of updateRegdStatus
	public JSONObject getRegdDetails() {
		JSONObject obj = null;
		try {
			db = helper.getReadableDatabase();
			String qry = String.format("select * from regd_details");
			Cursor c = db.rawQuery(qry, null);

			if (c.moveToFirst()) {
				obj = new JSONObject();
				obj.put("id", c.getString(c.getColumnIndex("id")));
				obj.put("imei_no", c.getString(c.getColumnIndex("imei_no")));
				obj.put("state", c.getString(c.getColumnIndex("state")));
				obj.put("bus_name", c.getString(c.getColumnIndex("bus_name")));
				obj.put("bus_reg_no", c.getString(c.getColumnIndex("bus_reg_no")));
				obj.put("sit_cap", c.getString(c.getColumnIndex("sit_cap")));
				obj.put("bus_st", c.getString(c.getColumnIndex("bus_st")));
				obj.put("bus_et", c.getString(c.getColumnIndex("bus_et")));
				obj.put("own_nm", c.getString(c.getColumnIndex("own_nm")));
				obj.put("own_cn", c.getString(c.getColumnIndex("own_cn")));
				obj.put("agent_nm", c.getString(c.getColumnIndex("agent_nm")));
				obj.put("agent_cn", c.getString(c.getColumnIndex("agent_cn")));
				obj.put("status", c.getString(c.getColumnIndex("status")));
				obj.put("created_by", c.getString(c.getColumnIndex("created_by")));
				obj.put("created_on", c.getString(c.getColumnIndex("created_on")));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.close();
		}
		return obj;
	}//end of getRegdDetails

	public long logPhotoDetails(String imageName) {
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{
			ContentValues value = new ContentValues();
			//value.put("id",  	input.getString("fname").trim());
			value.put("image_name", 	imageName);
			value.put("capture_time", 	Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			value.put("us",  			"F");
			
			rowId=db.insert("bus_img_details", null, value);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return rowId;
	}
	public void deleteExistingPlayList(String playlistId, String playdate){		
		db.execSQL("DELETE FROM play_list where playlist_id='"+playlistId+"' and start_date='"+playdate+"'");
	}
	public void deleteExistingAllPlayListOfPlayDate(String playdate){
		db.execSQL("DELETE FROM play_list where start_date='"+playdate+"'");
	}
	public long logSongScheduleData(JSONObject input, String created_date, String downloadTime) {
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{		
			//id, playlist_id, song_id, song_name, start_date, end_date, created_date
			ContentValues value = new ContentValues();
			value.put("playlist_id",  	input.getString("playlistid").trim());
			value.put("song_id",  		input.getString("song_id").trim());
			value.put("song_name", 		input.getString("song_name"));
			value.put("start_date", 	input.getString("start_date").trim());
			value.put("end_date",  		downloadTime);
			value.put("created_date",   created_date);
			rowId = db.insert("play_list", null, value);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return rowId;
	}
	public ArrayList<HashMap<String, String>> checkSong(){
		//new File("/mnt/external_sd/"),new File("/mnt/extSdCard/")
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		String path;
		if(isSDPresent){
			path = Utils.androidBuildingMusicPlayerActivity.getExternalCacheDir().getAbsolutePath() + "/Music";
	    }else{
	        path = Utils.androidBuildingMusicPlayerActivity.getFilesDir() + "/Music";
	    }
		System.out.println("Media Path :: "+path);
		final String MEDIA_PATH = Environment.getExternalStorageDirectory().toString()+"/Music";
		
		
		ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
		db = helper.getWritableDatabase();
		try{
			//id, playlist_id, song_id, song_name, start_date, end_date
			String qry = String.format("select * from play_list where Date(start_date) = Date('%s') order by created_date, song_id", Utils.getDate("yyyy-MM-dd"));
			System.out.println("query : "+qry);
			Cursor c = db.rawQuery(qry, null);
			if(c.moveToFirst()){
				Utils.playDate = c.getString(c.getColumnIndex("start_date"));
				do{
					//System.out.println("inside loop........");
					HashMap<String, String> song = new HashMap<String, String>();
					String sname = c.getString(c.getColumnIndex("song_name"));    
					song.put("songTitle", c.getString(c.getColumnIndex("song_name")).substring(0, (sname.length() - 4)));
					song.put("songPath", MEDIA_PATH + "/" + sname);
					song.put("plylistname", c.getString(c.getColumnIndex("playlist_id")));
					// Adding each song to SongList
					songsList.add(song);
										
				}while(c.moveToNext());
			}
			//System.out.println("----------------"+songsList.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		return songsList;		
	}

	public long logPlaySongDetails(String songTitle, double latitude, double longitude, String plylistid) {
		db = helper.getWritableDatabase();
	 	long seqId=0;
		long rowId = 0;
		try{
			ContentValues value = new ContentValues();
			value.put("plylist_id",  	plylistid);
			value.put("song_name", 		songTitle);
			value.put("play_date", 		Utils.getDate("yyyy-MM-dd"));
			value.put("start_time",  	Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			//value.put("end_time",  	"playing");
			value.put("latitude",  		latitude);
			value.put("longitude",  	longitude);
			//value.put("imei_no",  	"");
			value.put("us",  	"F");
			rowId = db.insert("play_song_details", null, value);
			if(rowId > 0){
				String qry = "select max(id) from play_song_details";
				Cursor  c = db.rawQuery(qry,null);
				if(c.moveToFirst()){
					do{
						seqId = c.getLong(0);
					}while(c.moveToNext());
				}
				
				System.out.println("start seq id is : "+seqId);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return seqId;
	}
	public int updatePlayDetEndTime(long songid,String clmName,double end_lat,double end_long) {
		System.out.println("db end time seq id : "+songid);
		db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
        values.put("us", "F");
        values.put("end_latitude", end_lat);
        values.put("end_longitude", end_long);
        values.put(clmName, Utils.getDate("yyyy-MM-dd HH:mm:ss"));
        return db.update("play_song_details", values, "id = ?", new String[] { String.valueOf(songid) });
	}
	public long logPlaySongPauseResumeDetails(String songTitle, long songSeqId,String pause_reason,String plylistid) {
		db = helper.getWritableDatabase();
	 	long seqId=0;
		long rowId = 0;
		try{
			String start_time=null;
			String qry2 =String.format("select start_time from play_song_details where id=%s",songSeqId);
			Cursor  c1 = db.rawQuery(qry2,null);
			if(c1.moveToFirst()){
				   do{
					   start_time=c1.getString(0); 
				   }while(c1.moveToNext());
			}
			ContentValues value = new ContentValues();
			value.put("play_seq_id",  	songSeqId);
			value.put("plylist_id",  	plylistid);
			value.put("song_name", 		songTitle);
			value.put("play_date", 		Utils.getDate("yyyy-MM-dd"));
			value.put("start_time",  	start_time);
			value.put("pause_time",  	Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			value.put("pause_reason",  	pause_reason);
			//value.put("imei_no",  	"");
			value.put("us",  	"F");
			rowId=db.insert("play_song_pause_detail", null, value);
			if(rowId>0){
				String qry = "select max(id) from play_song_pause_detail";
				Cursor  c = db.rawQuery(qry,null);
				if(c.moveToFirst()){
					   do{
						   seqId=c.getLong(0);
					   }while(c.moveToNext());
				}
				
				System.out.println("seq id is : "+seqId);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return seqId;
	}
	public int updatePlayDetResumeTime(long pauseid) {
		System.out.println("pause id : "+pauseid);
		db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
        values.put("us", "F");
        values.put("resume_time",Utils.getDate("yyyy-MM-dd HH:mm:ss"));
        return db.update("play_song_pause_detail", values, "id = ?", new String[] { String.valueOf(pauseid) });
	}
	public long logPlaySongJackRemoveDetails(String songTitle,long songSeqId,String status,String plylistid) {
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{
			String start_time=null;
			String qry = String.format("select start_time from play_song_details where id=%s",songSeqId);;
			Cursor  c = db.rawQuery(qry,null);
			if(c.moveToFirst()){
				   do{
					   start_time=c.getString(0); 
				   }while(c.moveToNext());
			}
			ContentValues value = new ContentValues();
			value.put("play_seq_id",  	songSeqId);
			value.put("plylist_id",  	plylistid);
			value.put("song_name", 		songTitle);
			value.put("play_date", 		Utils.getDate("yyyy-MM-dd"));
			value.put("start_time",  	start_time);
			value.put("alter_time",  	Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			value.put("jack_status", 	status);
			//value.put("imei_no",  	"");
			value.put("us",  			"F");
			rowId=db.insert("jack_details", null, value);
			if(rowId>0){
				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return rowId;
	}
	
	public long logPlaySongVolumeDetails(String songTitle,long songSeqId,int label,String plylistid) {
		db = helper.getWritableDatabase();
		long rowId = 0;
		try{
			String start_time=null;
			String qry = String.format("select start_time from play_song_details where id=%s",songSeqId);
			Cursor  c = db.rawQuery(qry,null);
			if(c.moveToFirst()){
				   do{
					   start_time=c.getString(0); 
				   }while(c.moveToNext());
			}
			
			ContentValues value = new ContentValues();
			value.put("play_seq_id",  		songSeqId);
			value.put("plylist_id",  		plylistid);
			value.put("song_name", 			songTitle);
			value.put("play_date", 			Utils.getDate("yyyy-MM-dd"));
			value.put("start_time",  		start_time);
			value.put("volume_label",  		label);
			value.put("volume_change_time", Utils.getDate("yyyy-MM-dd HH:mm:ss"));
			//value.put("imei_no",  	"");
			value.put("us",  	"F");
			rowId=db.insert("song_volume_label", null, value);
			if(rowId>0){
				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return rowId;
	}
	
	public void deleteOldPlaylistAndData(){
		System.out.println("Delete old playlist and successfully uploaded data");
		db = helper.getWritableDatabase();
		//String qry 	= String.format("DELETE FROM play_list where Date(end_date) < Date('%s')", Utils.getDate("yyyy-MM-dd"));
		String qry 	= String.format("DELETE FROM play_list where Date(start_date) < Date('%s')", Utils.getDate("yyyy-MM-dd"));
		String qry1 = String.format("DELETE FROM play_song_details where us='%s' and Date(play_date)< Date('%s')", "S", Utils.getDate("yyyy-MM-dd"));
		String qry2 = String.format("DELETE FROM play_song_pause_detail where us='%s' and Date(play_date)< Date('%s')", "S", Utils.getDate("yyyy-MM-dd"));
		String qry3 = String.format("DELETE FROM jack_details where us='%s' and Date(play_date)< Date('%s')", "S", Utils.getDate("yyyy-MM-dd"));
		String qry4 = String.format("DELETE FROM song_volume_label where us='%s' and Date(play_date)< Date('%s')", "S", Utils.getDate("yyyy-MM-dd"));
		String qry5 = String.format("select * FROM bus_img_details where us='%s' and Date(capture_time)< Date('%s')", "S", Utils.getDate("yyyy-MM-dd"));
		//String qry5 = String.format("select * FROM bus_img_details where us='%s'", 'S');
		db.execSQL(qry);
		db.execSQL(qry1);
		db.execSQL(qry2);
		db.execSQL(qry3);
		db.execSQL(qry4);
		
		Cursor  c = db.rawQuery(qry5,null);
		System.out.println("delete query : "+qry5);
		if(c.moveToFirst()){
		   do{
			   Utils.deletePhoto(c.getString(c.getColumnIndex("image_name")));
		   }while(c.moveToNext());
		}
		
	}
	
	//.......................... methods for upload data...........................\\
	public void getPlaySongDetDataToUpload(Context context) throws JSONException{
		  db=helper.getReadableDatabase();
		  //String[] conarr=con_no.split("[|]");
		  String imeino = Utils.getImeiNo(context);
		  JSONObject obj = null;
		  String qry=String.format("select * from play_song_details where us = 'F'");
		  System.out.println("query---"+qry);
		  Cursor c = db.rawQuery(qry,null);
		  if(c.moveToFirst()){
		   do{
			   obj=new JSONObject();
			   obj.put("song_id", 		c.getString(c.getColumnIndex("id")));
			   obj.put("song_name", 	c.getString(c.getColumnIndex("song_name"))!=null ?c.getString(c.getColumnIndex("song_name")) : "");
			   obj.put("play_date",		c.getString(c.getColumnIndex("play_date"))!=null ?c.getString(c.getColumnIndex("play_date")) : "");
			   obj.put("start_time", 	c.getString(c.getColumnIndex("start_time"))!=null ?c.getString(c.getColumnIndex("start_time")) : "");
			   obj.put("end_time",		c.getString(c.getColumnIndex("end_time"))!=null ?c.getString(c.getColumnIndex("end_time")) : "");
			   obj.put("latitude", 		c.getString(c.getColumnIndex("latitude"))!=null ?c.getString(c.getColumnIndex("latitude")) : "");
			   obj.put("longitude", 	c.getString(c.getColumnIndex("longitude"))!=null ?c.getString(c.getColumnIndex("longitude")) : "");
			   obj.put("end_latitude", 	c.getString(c.getColumnIndex("end_latitude"))!=null ?c.getString(c.getColumnIndex("end_latitude")) : "");
			   obj.put("end_longitude", c.getString(c.getColumnIndex("end_longitude"))!=null ?c.getString(c.getColumnIndex("end_longitude")) : "");
			   obj.put("imei", 			imeino);
			   obj.put("playlist_name", c.getString(c.getColumnIndex("plylist_id"))!=null ?c.getString(c.getColumnIndex("plylist_id")) : "");
			   
			   String status = Utils.sendBulkDataToServer(obj, "2");
			   if(status.equalsIgnoreCase("SUCCESS")){
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"S","play_song_details");
				   System.out.println("success");
			   }else{
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"F","play_song_details");
				   System.out.println("failed");
			   }
		   }while(c.moveToNext());
		  }
		//return obj;
	 }
	
	
	 public  void getPauseResumeDetailDataToUpload(Context context) throws JSONException{
		  db=helper.getReadableDatabase();
		  //String[] conarr=con_no.split("[|]");
		  String imeino=Utils.getImeiNo(context);
		  JSONObject obj = null;
		  String qry=String.format("select * from play_song_pause_detail where us = 'F'");
		  System.out.println("query---"+qry);
		  Cursor  c = db.rawQuery(qry,null);
		  if(c.moveToFirst()){
		   do{
			   obj=new JSONObject();
			   obj.put("pause_id", 		c.getString(c.getColumnIndex("id")));
			   obj.put("play_seq_id", 	c.getString(c.getColumnIndex("play_seq_id"))!=null ?c.getString(c.getColumnIndex("play_seq_id")):"");
			   obj.put("song_name", 	c.getString(c.getColumnIndex("song_name"))!=null ?c.getString(c.getColumnIndex("song_name")):"");
			   obj.put("play_date",		c.getString(c.getColumnIndex("play_date"))!=null ?c.getString(c.getColumnIndex("play_date")) : "");
			   obj.put("start_time", 	c.getString(c.getColumnIndex("start_time"))!=null ?c.getString(c.getColumnIndex("start_time")) : "");
			   obj.put("pause_time", 	c.getString(c.getColumnIndex("pause_time"))!=null ?c.getString(c.getColumnIndex("pause_time")) : "");
			   obj.put("resume_time", 	c.getString(c.getColumnIndex("resume_time"))!=null ?c.getString(c.getColumnIndex("resume_time")) : "");
			   obj.put("imei", 			imeino);
			   obj.put("pause_reason", 	c.getString(c.getColumnIndex("pause_reason"))!=null ?c.getString(c.getColumnIndex("pause_reason")) : "");
			   obj.put("playlist_name", c.getString(c.getColumnIndex("plylist_id"))!=null ?c.getString(c.getColumnIndex("plylist_id")) : "");
			   String status=Utils.sendBulkDataToServer(obj, "3");
			   if(status.equalsIgnoreCase("SUCCESS")){
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"S","play_song_pause_detail");
				   System.out.println("success");
			   }else{
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"F","play_song_pause_detail");
				   System.out.println("failed");
			   }
		   }while(c.moveToNext());
		  }
		//return obj;
	 }
	
	 public  void getJackRemoveDetailDataToUpload(Context context) throws JSONException{
		  db=helper.getReadableDatabase();
		  //String[] conarr=con_no.split("[|]");
		  String imeino=Utils.getImeiNo(context);
		  JSONObject obj = null;
		  String qry=String.format("select * from jack_details where us = 'F'");
		  System.out.println("query---"+qry);
		  Cursor  c = db.rawQuery(qry,null);
		  if(c.moveToFirst()){
		   do{
			   obj=new JSONObject();
			   obj.put("play_seq_id", 	c.getString(c.getColumnIndex("play_seq_id"))!=null ?c.getString(c.getColumnIndex("play_seq_id")) : "");
			   obj.put("song_name", 	c.getString(c.getColumnIndex("song_name"))!=null ?c.getString(c.getColumnIndex("song_name")) : "");
			   obj.put("play_date",		c.getString(c.getColumnIndex("play_date"))!=null ?c.getString(c.getColumnIndex("play_date")) : "");
			   obj.put("start_time", 	c.getString(c.getColumnIndex("start_time"))!=null ?c.getString(c.getColumnIndex("start_time")) : "");
			   obj.put("jack_status",	c.getString(c.getColumnIndex("jack_status"))!=null ?c.getString(c.getColumnIndex("jack_status")) : "");
			   obj.put("alter_time", 	c.getString(c.getColumnIndex("alter_time"))!=null ?c.getString(c.getColumnIndex("alter_time")) : "");
			   obj.put("imei", 			imeino);
			   obj.put("playlist_name", c.getString(c.getColumnIndex("plylist_id"))!=null ?c.getString(c.getColumnIndex("plylist_id")) : "");
			   String status=Utils.sendBulkDataToServer(obj, "4");
			   if(status.equalsIgnoreCase("SUCCESS")){
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"S","jack_details");
				   System.out.println("success");
			   }else{
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"F","jack_details");
				   System.out.println("failed");
			   }
		   }while(c.moveToNext());
		  }
		//return obj;
	 }
	 
	 public  void getVolumeDataToUpload(Context context) throws JSONException{
		  db=helper.getReadableDatabase();
		  //String[] conarr=con_no.split("[|]");
		  String imeino=Utils.getImeiNo(context);
		  JSONObject obj = null;
		  String qry=String.format("select * from song_volume_label where us = 'F'");
		  System.out.println("query---"+qry);
		  Cursor  c = db.rawQuery(qry,null);
		  if(c.moveToFirst()){
		   do{
			   obj=new JSONObject();
			   obj.put("play_seq_id", 		c.getString(c.getColumnIndex("play_seq_id"))!=null ?c.getString(c.getColumnIndex("play_seq_id")) : "");
			   obj.put("song_name", 		c.getString(c.getColumnIndex("song_name"))!=null ?c.getString(c.getColumnIndex("song_name")) : "");
			   obj.put("play_date",			c.getString(c.getColumnIndex("play_date"))!=null ?c.getString(c.getColumnIndex("play_date")) : "");
			   obj.put("start_time", 		c.getString(c.getColumnIndex("start_time"))!=null ?c.getString(c.getColumnIndex("start_time")) : "");
			   obj.put("volume_label",		c.getString(c.getColumnIndex("volume_label"))!=null ?c.getString(c.getColumnIndex("volume_label")) : "");
			   obj.put("volume_change_time",c.getString(c.getColumnIndex("volume_change_time"))!=null ?c.getString(c.getColumnIndex("volume_change_time")) : "");
			   obj.put("imei", 				imeino);
			   obj.put("playlist_name", c.getString(c.getColumnIndex("plylist_id"))!=null ?c.getString(c.getColumnIndex("plylist_id")) : "");
			   String status=Utils.sendBulkDataToServer(obj, "5");
			   if(status.equalsIgnoreCase("SUCCESS")){
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"S","song_volume_label");
				   System.out.println("success");
			   }else{
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"F","song_volume_label");
				   System.out.println("failed");
			   }
		   }while(c.moveToNext());
		  }
		//return obj;
	 }
	 public  void getImageDataToUpload(Context context) throws JSONException{
		  db=helper.getReadableDatabase();
		  //String[] conarr=con_no.split("[|]");
		  String imeino=Utils.getImeiNo(context);
		  //JSONObject obj = null;
		  String qry=String.format("select * from bus_img_details where us = 'F'");
		  System.out.println("query---"+qry);
		  Cursor  c = db.rawQuery(qry,null);
		  if(c.moveToFirst()){
		   do{
			  // obj=new JSONObject();
			 //  obj.put("id", 		c.getString(c.getColumnIndex("id")));
			  // obj.put("image_name", 	c.getString(c.getColumnIndex("image_name"))!=null ?c.getString(c.getColumnIndex("image_name")) : "");
			  // obj.put("capture_time",		c.getString(c.getColumnIndex("capture_time"))!=null ?c.getString(c.getColumnIndex("capture_time")) : "");
			  // obj.put("imei", 			imeino);
			   String status=Utils.sendImagesToServer(context, imeino, c.getString(c.getColumnIndex("image_name")),c.getString(c.getColumnIndex("capture_time")));
			   if(status.equalsIgnoreCase("SUCCESS")){
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"S","bus_img_details");
				   System.out.println("image success");
			   }else{
				   updateUploadStatus(c.getString(c.getColumnIndex("id")),"F","bus_img_details");
				   System.out.println("image failed");
			   }
		   }while(c.moveToNext());
		  }
		//return obj;
	 }
	 
	 public int updateUploadStatus(String songid,String us,String tabelName) {
			//System.out.println("end time seq id : "+songid);
			db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
	        values.put("us", us);
	        return db.update(tabelName, values, "id = ?", new String[] { String.valueOf(songid) });
	 }
	 
	 
	 public JSONObject getHostDetails() {
		 JSONObject obj = null;
		 try {
			 db = helper.getReadableDatabase();
			 //host_detail(ip_address, port_no)
			 String qry = String.format("select * from host_detail");
			 Cursor c = db.rawQuery(qry, null);			 
			 
			 if (c.moveToFirst()) {
				 do {
					 obj = new JSONObject();
					 obj.put("ip_address", c.getString(c.getColumnIndex("ip_address")));
					 obj.put("port_no", c.getString(c.getColumnIndex("port_no")));
				 } while (c.moveToNext());
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return obj;
	 } 
	 
	 public String updateHostDetails(String ip_address, String port_no) {
		 String msg = "Failure";
		 try{
			 truncateTable("host_detail");
			 db = helper.getWritableDatabase();
	         ContentValues value = new ContentValues();
			 value.put("ip_address", ip_address);
			 value.put("port_no", port_no);
			 long rowId = db.insert("host_detail", null, value);
			 if(rowId>0){
		        msg = "Success";
			 }	        		 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return msg;
	 }
	 
	 	
	 public int updatePlaySongIndex(String playDate, int songIndex) {
		 System.out.println("update play song index after played complete : playDate: "+ playDate + "    songIndex: "+songIndex);
		 db = helper.getWritableDatabase();
		 ContentValues values = new ContentValues();
	     values.put("play_date", playDate);
	     values.put("song_index", songIndex);
	     return db.update("play_song_index", values, "id=?", new String[] { String.valueOf("1")});
	 }
	 
	 public JSONObject getPlaySongIndex() {
		 System.out.println("get play song index..................");
		 JSONObject obj = null;
		 try {
			 db = helper.getReadableDatabase();
			 String qry=String.format("select * from play_song_index where id=1");
			 System.out.println("get play song index query : " + qry);
			 
			 Cursor c = db.rawQuery(qry, null);			 
			 
			 if (c.moveToFirst()) {
				 do {
					 obj = new JSONObject();
					 obj.put("id", c.getInt(c.getColumnIndex("id")));
					 obj.put("play_date", c.getString(c.getColumnIndex("play_date")));
					 obj.put("song_index", c.getInt(c.getColumnIndex("song_index")));
				 } while (c.moveToNext());
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 System.out.println("Play song index: " + obj.toString());
		 return obj;
	 }
	//================================================================================================================\\	 
}
