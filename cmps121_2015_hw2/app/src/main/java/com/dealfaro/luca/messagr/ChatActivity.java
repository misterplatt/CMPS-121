package com.dealfaro.luca.messagr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


public class ChatActivity extends ActionBarActivity {

    Location lastLocation;
    private double lastAccuracy = (double) 1e10;
    private long lastAccuracyTime = 0;

    //Latitude and Longitude variables
    private double currentLat = 0;
    private double currentLong = 0;

    private AppInfo appInfo;
    private String sentBy;

    private static final String LOG_TAG = "messagr";

    private static final float GOOD_ACCURACY_METERS = 100;

    // This is an id for my app, to keep the key space separate from other apps.
    private static final String MY_APP_ID = "luca_bboard";

    private static final String SERVER_URL_PREFIX = "https://hw3n-dot-luca-teaching.appspot.com/store/default/";

    // To remember the favorite account.
    public static final String PREF_ACCOUNT = "pref_account";

    // To remember the post we received.
    public static final String PREF_POSTS = "pref_posts";

    // Uploader.
    private ServerCall uploader;

    // Remember whether we have already successfully checked in.
    private boolean checkinSuccessful = false;

    private ArrayList<String> accountList;

    private class ListElement {
        ListElement() {};

        public String textLabel;

        public String msg;
        public String userid;
        public String dest;
        public String ts;
        public String msgid;
        public Boolean conversation;
    }

    private ArrayList<ListElement> aList;

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            final ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            ImageView privateDot = (ImageView) newView.findViewById(R.id.imageView);
            tv.setText(w.textLabel);

            //Hide dot when inside conversation
            privateDot.setVisibility(View.INVISIBLE);

            return newView;
        }
    }

    private MyAdapter aa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList = new ArrayList<ListElement>();
        aa = new MyAdapter(this, R.layout.list_element, aList);
        ListView myListView = (ListView) findViewById(R.id.listView);
        myListView.setAdapter(aa);
        aa.notifyDataSetChanged();

        //Obtain the singleton object
        appInfo = AppInfo.getInstance(this);
        Intent intent = getIntent();
        sentBy = intent.getStringExtra("dest");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Remember the last received location.
        //lastLocation.setLongitude(currentLong);
        //lastLocation.setLatitude(currentLat);

        //Display them.
        displayCoords();
    }


    @Override
    protected void onResume() {
        // First super, then do stuff.
        super.onResume();

        /*// Let us display the previous posts, if any.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String result = settings.getString(PREF_POSTS, null);
        if (result != null) {
            displayResult(result);
        }*/

        //Re-enable the submit button.
        Button submitButton = (Button) findViewById(R.id.button);
        submitButton.setEnabled(true);

        // Then start to request location updates, directing them to locationListener.
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onPause() {
        // Stops the upload if any.
        if (uploader != null) {
            uploader.cancel(true);
            uploader = null;
        }

        // Stops the location updates.
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);

        //Disable the submit button.
        Button submitButton = (Button) findViewById(R.id.button);
        submitButton.setEnabled(false);

        //Then super.
        super.onPause();
    }

    //Listens for and picks up the most recent location
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Do something with the location you receive.
            double newAccuracy = location.getAccuracy();
            long newTime = location.getTime();
            //Check if new accuracy is more precise
            boolean accComp = ((lastLocation == null) || newAccuracy < lastAccuracy + (newTime - lastAccuracyTime));
            if(accComp){
                lastLocation = location;
                lastAccuracy = location.getAccuracy();
                lastAccuracyTime = location.getTime();
            }

            //Update the latitude and longitude, then display them.
            currentLat = lastLocation.getLatitude();
            currentLong = lastLocation.getLongitude();
            displayCoords();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };


    public void clickButton(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        //Make sure the message is not empty
        if(msg.equals("")){
            Toast toast = Toast.makeText(getApplicationContext(), "Can't send an empty message!",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Log.i(LOG_TAG, "Message to send: " + msg);

        // Then, we start the call.
        PostMessageSpec myCallSpec = new PostMessageSpec();

        //Show progress bar
        ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar);
        wheel.setVisibility(View.VISIBLE);

        myCallSpec.url = SERVER_URL_PREFIX + "put_local";
        myCallSpec.context = ChatActivity.this;

        //Make sure location has been set before proceeding.
        if(lastLocation == null) {
            Log.i(LOG_TAG, "No location found, cannot send.");
            return;
        }
        Location currentLocation = lastLocation;


        //Declare the map.
        HashMap<String,String> m = new HashMap<String,String>();

        // Add the parameters.
        m.put("msgid", reallyComputeHash(msg));
        m.put("msg", msg);
        m.put("lng", Double.toString(currentLong));
        m.put("lat", Double.toString(currentLat));
        m.put("userid", appInfo.userid);
        m.put("dest", sentBy);

        myCallSpec.setParams(m);

        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    public void clickButton2(View v) {

        // Get the text we want to send.
        EditText et = (EditText) findViewById(R.id.editText);
        String msg = et.getText().toString();

        // Then, we start the call.
        RefreshMessageSpec myCallSpec = new RefreshMessageSpec();

        //Show progress bar
        ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar);
        wheel.setVisibility(View.VISIBLE);

        myCallSpec.url = SERVER_URL_PREFIX + "get_local";
        myCallSpec.context = ChatActivity.this;

        //Make sure location has been set before proceeding.
        if(lastLocation == null) {
            Log.i(LOG_TAG, "No location found, cannot send.");
            return;
        }
        Location currentLocation = lastLocation;
        Log.i(LOG_TAG, "Long to send: " + currentLong);
        Log.i(LOG_TAG, "Lat to send: " + currentLat);

        //Declare the map.
        HashMap<String,String> m = new HashMap<String,String>();

        // Add the parameters.
        m.put("lng", Double.toString(currentLong));
        m.put("lat", Double.toString(currentLat));
        m.put("userid", appInfo.userid);
        m.put("dest", sentBy);

        myCallSpec.setParams(m);

        // Actual server call.
        if (uploader != null) {
            // There was already an upload in progress.
            uploader.cancel(true);
        }
        uploader = new ServerCall();
        uploader.execute(myCallSpec);
    }

    private String reallyComputeHash(String s) {
        // Computes the crypto hash of string s, in a web-safe format.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());
            digest.update("My secret key".getBytes());
            byte[] md = digest.digest();
            // Now we need to make it web safe.
            String safeDigest = Base64.encodeToString(md, Base64.URL_SAFE);
            return safeDigest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * This class is used to do the post HTTP call, and it specifies how to use the result.
     */
    class PostMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");

                Toast toast = Toast.makeText(getApplicationContext(), "Message failed to send.",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                //Post successful, clear the input field
                EditText input = (EditText) findViewById(R.id.editText);
                input.setText("");
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();
            }

            //Hide progress bar
            ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar);
            wheel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This class is used to do the refresh HTTP call, and does not clear the editText.
     */
    class RefreshMessageSpec extends ServerCallSpec {
        @Override
        public void useResult(Context context, String result) {
            if (result == null) {
                // Do something here, e.g. tell the user that the server cannot be contacted.
                Log.i(LOG_TAG, "The server call failed.");

                Toast toast = Toast.makeText(getApplicationContext(), "Unable to refresh.",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                // Translates the string result, decoding the Json.
                Log.i(LOG_TAG, "Received string: " + result);
                displayResult(result);
                // Stores in the settings the last messages received.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_POSTS, result);
                editor.commit();
            }

            //Hide progress bar
            ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar);
            wheel.setVisibility(View.INVISIBLE);
        }
    }


    private void displayResult(String result) {
        Gson gson = new Gson();
        MessageList ml = gson.fromJson(result, MessageList.class);
        if (ml == null) Log.i(LOG_TAG, "No message list!!!");

        // Fills aList, so we can fill the listView.
        aList.clear();
        for (int i = 0; i < ml.messages.length && i < 10; i++) {
            ListElement ael = new ListElement();
            String displayTS = getMessageAge(ml.messages[i].ts);
            ael.textLabel = ml.messages[i].msg + "\n    " + displayTS;

            ael.conversation = ml.messages[i].conversation;
            ael.dest = ml.messages[i].dest;
            ael.userid = ml.messages[i].userid;
            ael.ts = displayTS;

            aList.add(ael);
        }
        aa.notifyDataSetChanged();
    }

    private void displayCoords() {
        //Update current longitude display
        TextView longText = (TextView) findViewById(R.id.longView);
        longText.setText("Long: " + Double.toString(currentLong));

        //Update current latitude display
        TextView latText = (TextView) findViewById(R.id.latView);
        latText.setText("Lat: " + Double.toString(currentLat));
    }

    //Function to convert timestamp for displaying
    private String getMessageAge(String ts){
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        targetFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date timestampDate = new Date();
        String age = "";
        //Parse the date, catching a parse exception if it occurs
        try {
            timestampDate = targetFormat.parse(ts);
            //Get current UTC time
            Date currentTime = new Date();

            long diff = currentTime.getTime() - timestampDate.getTime();
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffMinutes = diff / (60 * 1000) % 60;

            if(diffHours == 0) age = Long.toString(diffMinutes) + "m ago";
            else age = Long.toString(diffHours) + "h " + Long.toString(diffMinutes) + "m ago";
            Log.d(LOG_TAG, "Message age: " + diff);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return age;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
