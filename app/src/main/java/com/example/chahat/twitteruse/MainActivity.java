package com.example.chahat.twitteruse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "mC7swOvlEBvMoeCGhREclN4i0";
    private static final String TWITTER_SECRET = "cDuzWM3DBf8noj64Z5D6CEONsVm1P1bJddIgiYghdefNgrpNhB";

    String token;
    Long ids[];
    LinearLayout myLayout,llgone;

    SearchView searchView;
    ProgressDialog progress;

    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);

        /*myLayout = (LinearLayout) findViewById(R.id.activity_main);*/
        myLayout = (LinearLayout) findViewById(R.id.myLayout);
        llgone = (LinearLayout) findViewById(R.id.llgone);
        searchView = (SearchView) findViewById(R.id.searchView);

        try {
            String encodedConsumerKey = URLEncoder.encode(TWITTER_KEY,"UTF-8");
            String encodedConsumerSecret = URLEncoder.encode(TWITTER_SECRET,"UTF-8");
            String authString = encodedConsumerKey +":"+encodedConsumerSecret;
            String base64Encoded = Base64.encodeToString(authString.getBytes("UTF-8"), Base64.NO_WRAP);

            accesToken(base64Encoded);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });

        handler = new Handler();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(final String query) {

                /*long startTime = System.nanoTime();
                Log.v("startTime",startTime+"");

                long endTime = System.nanoTime();
                Log.v("endTime",endTime+"");
                final long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds

*/
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        getTweets(query);
                        handler.postDelayed(runnable,5000);
                    }
                };

                runnable.run();

                progress = new ProgressDialog(MainActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();

                /*final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(this,5000);
                        getTweets(query);
                    }
                }, 0);*/

                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(runnable);

                return false;
            }
        });


    }

    public void accesToken(final String base64Encoded){

        String uriString = "https://api.twitter.com/oauth2/token";

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,uriString, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("response",response.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("access_token");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }

                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                headers.put("Authorization", "Basic " + base64Encoded);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.start();
        requestQueue.add(stringRequest);

    }

   public void getTweets(final String q) {


       String uriString = "https://api.twitter.com/1.1/search/tweets.json?q=%23" + q;

       StringRequest stringRequest = new StringRequest(
               Request.Method.GET, uriString, new Response.Listener<String>() {

           @Override
           public void onResponse(String response) {
               Log.d("response", response.toString());

               try {
                   JSONObject jsonObject = new JSONObject(response);
                   JSONArray jsonArray = jsonObject.getJSONArray("statuses");

                   ids = new Long[jsonArray.length()];

                   for (int i = 0; i < jsonArray.length(); i++) {
                       JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                       final ViewGroup parentView = (ViewGroup) getWindow().getDecorView().getRootView();
                       // TODO: Base this Tweet ID on some data from elsewhere in your app
                       long tweetId = Long.parseLong(jsonObject1.getString("id"));

                       ids[i] = tweetId;
                   }

                   final List<Long> tweetIds = Arrays.asList(ids);
                   myLayout.removeAllViewsInLayout();
                   for (Long longvar : tweetIds) {
                       TweetUtils.loadTweet(longvar, new Callback<Tweet>() {
                           @Override
                           public void success(Result<Tweet> result) {
                               final CompactTweetView compactTweetView = new CompactTweetView(MainActivity.this, result.data);
                               myLayout.addView(compactTweetView);
                               llgone.setVisibility(View.GONE);
                                progress.dismiss();
                           }

                           @Override
                           public void failure(TwitterException exception) {

                           }
                       });

                   }

               } catch (JSONException e) {
                   e.printStackTrace();
               }


           }
       }, new Response.ErrorListener() {

           @Override
           public void onErrorResponse(VolleyError error) {

           }

       }) {
            /*@Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");

                return params;
            }*/

           @Override
           public Map<String, String> getHeaders() throws AuthFailureError {
               HashMap<String, String> headers = new HashMap<String, String>();
               headers.put("Authorization", "Bearer " + token);
               return headers;
           }
       };
       RequestQueue requestQueue = Volley.newRequestQueue(this);
       requestQueue.start();
       requestQueue.add(stringRequest);
   }

}

