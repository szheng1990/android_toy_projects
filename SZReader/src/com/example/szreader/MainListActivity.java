package com.example.szreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainListActivity extends ListActivity {
	
	protected String[] mBlogPostTitles;
	public static final int NUMBER_OF_POSTS = 20;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		if (isNetworkAvailable()){
		   mProgressBar.setVisibility(View.VISIBLE);
           GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
           getBlogPostsTask.execute();
		}else{
			Toast.makeText(this, "Network not available", Toast.LENGTH_LONG).show();
		}
		
		//Resources resources = getResources();
		
		//mBlogPostTitles = resources.getStringArray(R.array.android_names);
		
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
		//setListAdapter(adapter);
		
		//String message = getString(R.string.no_items);
		//Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);
		JSONArray jsonPosts;
		try {
			jsonPosts = mBlogData.getJSONArray("posts");
			JSONObject jsonPost = jsonPosts.getJSONObject(position);
			String blogUrl = jsonPost.getString("url");
			// access web page through intent
			// action_view makes the most reasonable choice based on URI (email, webpage, etc)
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(blogUrl));
			startActivity(intent);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private boolean isNetworkAvailable() {
	    ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
	    
	    boolean isAvailable = false;
	    if (networkInfo != null && networkInfo.isConnected()){
	    	isAvailable = true;
	    }
	    return isAvailable;
	}

	
	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
	    if (mBlogData == null){
	    	updateDisplayError();
	    	
	    }else{
	    	try {
				//Log.d(TAG, mBlogData.toString(2));
	    		JSONArray jsonPosts = mBlogData.getJSONArray("posts");
	    		mBlogPostTitles = new String[jsonPosts.length()];
	    		for (int i=0; i<jsonPosts.length();i++){
	    			JSONObject post = jsonPosts.getJSONObject(i);
	    			String title = post.getString("title");
	    			title = Html.fromHtml(title).toString();
	    			mBlogPostTitles[i] = title;
	    		}
	    		
	    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
	    		setListAdapter(adapter);
	    		
	    		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error!", e);
			}
	    }
		
	}

	private void updateDisplayError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title));
		builder.setMessage(getString(R.string.error_message));
		builder.setPositiveButton(android.R.string.ok, null); //android's own (R)esources, different from the project's Resources)
		
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}
	
	private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

		// background thread cannot interact with the UI thread
		@Override
		protected JSONObject doInBackground(Object... arg0) { // zero or more objects can be passed in
			int responseCode = -1;
			JSONObject jsonResponse = null;
			try {
				   URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count="+NUMBER_OF_POSTS);
				   HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
				   connection.connect();
				   
				   responseCode = connection.getResponseCode();
				   if (responseCode==HttpURLConnection.HTTP_OK){
					   InputStream is = connection.getInputStream();
					   Reader reader = new InputStreamReader(is);
					   int contentLength = connection.getContentLength();
					   char[] charArray = new char[contentLength];
					   reader.read(charArray);
					   String responseData = new String(charArray);
					   //Log.v(TAG, responseData);
					   
					   jsonResponse = new JSONObject(responseData);
					   
					   
				   }else{
				       Log.i(TAG, "Unsuccessful Code: "+responseCode);
				   }
				   
				}catch (MalformedURLException e) {
					Log.e(TAG, "Exception caught: ", e);
				}catch (IOException e){
					Log.e(TAG, "Exception caught: ", e);
				}catch (Exception e){
					Log.e(TAG, "Exception caught: ", e);
				}
			return jsonResponse;
		}
		
		@Override
		protected void onPostExecute(JSONObject result){
			mBlogData = result;
			handleBlogResponse();
		}
		
	}



}
