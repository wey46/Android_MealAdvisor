package com.wey46.mealadvisor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.wey46.mealadvisor.Helper.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ListActivity extends AppCompatActivity {
    String email;
    private String TAG = ListActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;
    private ListAdapter adapter ;

    // URL to get contacts JSON
    private static String url = "https://developers.zomato.com/api/v2.1/search?count=10&lat=40.44203&lon=-79.95415&radius=100&sort=real_distance&order=asc";

    ArrayList<HashMap<String, String>> restaurantGroup;


    private LocationManager locationManager;
    private Location onlyOneLocation;
    private final int REQUEST_FINE_LOCATION = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        email = getIntent().getExtras().get("EMAIL").toString();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        restaurantGroup = new ArrayList<>();

        lv = findViewById(R.id.list);
        new ListActivity.GetRestaurants().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetRestaurants extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ListActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray restaurants = jsonObj.getJSONArray("restaurants");

                    // looping through All Contacts
                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject c = restaurants.getJSONObject(i).getJSONObject("restaurant");
                        String name = c.getString("name");
                        String address =  c.getJSONObject("location").getString("address");
                        String cuisines = c.getString("cuisines");

                        HashMap<String, String> restaurant = new HashMap<>();

                        // adding each child node to HashMap key => value
                        restaurant.put("name", name);
                        restaurant.put("address", address);
                        restaurant.put("cuisines", cuisines);

                        //adding contact to contact list
                        restaurantGroup.add(restaurant);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            adapter = new SimpleAdapter(
                    ListActivity.this, restaurantGroup,
                    R.layout.list_item, new String[]{"name", "cuisines",
                    "address"}, new int[]{R.id.name,
                    R.id.address, R.id.cuisines});
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+restaurantGroup.get(position).get("address")+"&mode=w");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Toast.makeText(getApplicationContext(),"You have selected profile btn",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
