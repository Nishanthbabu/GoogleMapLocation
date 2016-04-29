package com.example.skilledanswers_d1.map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skilledanswers_d1.map.MapDistance.DirectionsJSONParser;
import com.example.skilledanswers_d1.map.MapMotion.TouchableMapFragment;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UberMap extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {


    Context mContext = null;       ////// context
    TextView tvAddress = null;  //// texview for address display
    private boolean pickupselected = false;   //// flags for using the same textview for both pickup and drop
    private boolean destinationselected = false;    /// same as above
    //    TextView tvAddress2;
    public TextView pointer = null;         ///// pointer.. to set locqation... click lisner is there for this
    private TextView tvAddress_lable = null;
    public LinearLayout locationlayout = null;  ///////
    ImageView location_icon = null;
    private LocationManager mlocManager = null;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_PICKUP = 1;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_DROP = 0;
    public static boolean PLACE_AUTOCOMPLETE_ENTERED_FOR_PICKUP = false;
    public static boolean PLACE_AUTOCOMPLETE_ENTERED_FOR_DROP = false;


    private GoogleApiClient mGoogleApiClient = null;
    //A data object that contains quality of service parameters for requests to the FusedLocationProviderApi.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    // Google Map
    private GoogleMap googleMap = null;      // google map variable

    TouchableMapFragment mapFragment = null;  // my customized google map fragment  for implementing touch and touch remove lisner..
    private static String startLocation = null; // temp string that holds the start location for filling it in textview
    private static String endLocation = null;// temp string that holds the end location for filling it in textview
    private LatLng startLatlong = null; // holds the latlong of startpoint used in calculating the address distance and time
    private LatLng endLatlong = null; // holds the latlong of endpoint used in calculating the address distance and time
    ///////
    ProgressDialog progressDialog = null;  //// dilog to show while asinktask like getting the location json from google and
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    // from rthat adddress finding the location distance and time and drawing the polyline

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uber_map);
        mContext = this;
        ////
        locationlayout = (LinearLayout) findViewById(R.id.locationlayout);
        tvAddress = (TextView) findViewById(R.id.uber_tvAddress);
        tvAddress_lable = (TextView) findViewById(R.id.uber_tvAddress_lable);
        pointer = (TextView) findViewById(R.id.locationMarkertext);              //// widgits to java
        location_icon = (ImageView) findViewById(R.id.location_icon);
        mapFragment = ((TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.uber_map));
        ///

//        location_icon.setVisibility(View.GONE);
//        tvAddress.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    Intent intent=new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(UberMap.this);
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_PICKUP);
//                } catch (GooglePlayServicesRepairableException e) {
//                    e.printStackTrace();
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        tvAddress2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!tvAddress.getText().toString().equals(""))
//                {
//                    try {
//                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(UberMap.this);
//                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_DROP);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        e.printStackTrace();
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        e.printStackTrace();
//                    }
//            }else
//                {
//                    Toast.makeText(UberMap.this, "Please add pickup location", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        mapFragment.getMapAsync(this); //This class automatically initializes the maps system and the view.

        mGoogleApiClient = new GoogleApiClient.Builder(mContext) //Specify which Apis should attempt to connect
                .addApi(LocationServices.API)    /// here i use location service for map
                .addConnectionCallbacks(this) //Registers a listener to receive connection events from this GoogleApiClient.
                .addOnConnectionFailedListener(this) //Adds a listener to register to receive connection failed events from this GoogleApiClient.
                .build();  //Builds a new GoogleApiClient object for communicating with the Google APIs.

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_PICKUP) {
//            Toast.makeText(UberMap.this, ""+requestCode, Toast.LENGTH_SHORT).show();
//            if (resultCode == RESULT_OK) {
//                Place place = PlaceAutocomplete.getPlace(this, data);
//                tvAddress.setText(""+place.getAddress());
//                Log.i("Result", "Place: " + place.getName());
//                Toast.makeText(UberMap.this, ""+place.getName().toString(), Toast.LENGTH_SHORT).show();
//                PLACE_AUTOCOMPLETE_ENTERED_FOR_PICKUP =true;   ///// places selected from google map
//            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
//                Status status = PlaceAutocomplete.getStatus(this, data);
//                // TODO: Handle the error.
//                Log.i("error", status.getStatusMessage());
//
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//        }
////        else
////            if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_FOR_DROP)
////            {
////                Toast.makeText(UberMap.this, ""+requestCode, Toast.LENGTH_SHORT).show();
////                if (resultCode == RESULT_OK) {
////                    Place place = PlaceAutocomplete.getPlace(this, data);
////                    tvAddress2.setText(""+place.getAddress());
////                    Log.i("Result", "Place: " + place.getName());
////                    Toast.makeText(UberMap.this, ""+place.getName().toString(), Toast.LENGTH_SHORT).show();
////                    PLACE_AUTOCOMPLETE_ENTERED_FOR_DROP =true;   ///// places selected from google map
////                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
////                    Status status = PlaceAutocomplete.getStatus(this, data);
////                    // TODO: Handle the error.
////                    Log.i("error", status.getStatusMessage());
////
////                } else if (resultCode == RESULT_CANCELED) {
////                    // The user canceled the operation.
////                }
////            }
//
//    }


    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect(); // dissconnnecting the google api client before exiting the activity
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();//the GoogleApiClient must be connected using the connect() method before using
        }


    }

    @Override
    public void onLocationChanged(Location loc) {
        // TODO Auto-generated method stub
        if (loc == null)
            return;


//        if (markerCurre == null) {     //// commented by nishanth bcz i think no use
//
//            markerCurre = googleMap.addMarker(new MarkerOptions()
//                    .flat(true)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon))
//                    .title("Your Current Position")
//                    .anchor(0.5f, 0.5f)
//                    .position(new LatLng(loc.getLatitude(), loc.getLongitude())));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16.0f));

//        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(UberMap.this, "Plzz give location access permission", Toast.LENGTH_SHORT).show();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    REQUEST,
                    this);  // LocationListener
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapReady(GoogleMap map) {
        // TODO Auto-generated method stub
        this.googleMap = map;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(UberMap.this, "Plzzz give location access permission", Toast.LENGTH_SHORT).show();
        } else {

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            location_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mlocManager = (LocationManager) UberMap.this
                            .getSystemService(Context.LOCATION_SERVICE);
                    if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if (ActivityCompat.checkSelfPermission(UberMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UberMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Toast.makeText(UberMap.this, "Plzzz give location access permission", Toast.LENGTH_SHORT).show();
                        } else {
//                        LocationManager locationManager = (LocationManager)
//                                getSystemService(Context.LOCATION_SERVICE);
//                        Criteria criteria = new Criteria();
//                        Location location = locationManager.getLastKnownLocation(locationManager
//                                .getBestProvider(criteria, false));
//
////                        double latitude = location.getLatitude();
////                        double longitude = location.getLongitude();
//                        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude());
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
//                        googleMap.animateCamera(cameraUpdate);
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient,
                                    REQUEST,
                                    UberMap.this);  // LocationListener
                        }
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                UberMap.this);
                        alertDialogBuilder
                                .setMessage("GPS is disabled in your device. Enable it?")
                                .setCancelable(false)
                                .setPositiveButton("Enable GPS",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                Intent callGPSSettingIntent = new Intent(
                                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                UberMap.this.startActivity(callGPSSettingIntent);

                                            }

                                        });
                        alertDialogBuilder.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();
                    }
                }
            });


        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //googleMap.getUiSettings().setRotateGesturesEnabled(false);

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition position) {
                // TODO Auto-generated method stub

                Log.w("" + position.target.latitude, "" + position.target.longitude);

                new ReverseGeocodingTask().execute(position.target);


            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "UberMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.skilledanswers_d1.map/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "UberMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.skilledanswers_d1.map/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, String> {
        double _latitude, _longitude;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvAddress.setText("Getting address please wait");
            pointer.setClickable(false);

        }

        @Override
        protected String doInBackground(LatLng... params) {

            Geocoder geocoder = new Geocoder(mContext);
            _latitude = params[0].latitude;
            _longitude = params[0].longitude;

            List<Address> addresses = null;
            String addressText = "";

            try {
                addresses = geocoder.getFromLocation(_latitude, _longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    if (returnedAddress.getMaxAddressLineIndex() == (i - 1)) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i));
                    } else {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",");
                    }
                }
                addressText = strReturnedAddress.toString();
                Log.w("My loction address", "" + strReturnedAddress.toString());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {

            final String result = addressText;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    tvAddress.setText(result);
                    pointer.setClickable(true);
                    pointer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (!tvAddress.getText().toString().equals("") && !pickupselected) {
                                googleMap.clear();
                                startLocation = tvAddress.getText().toString();
                                startLatlong = new LatLng(_latitude, _longitude);
                                System.out.println("*************************s" + startLatlong);
                                tvAddress_lable.setText("choose ur destination");
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(startLatlong);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                googleMap.addMarker(markerOptions);
                                pointer.setText("choose ur destination");
                                pickupselected = true;
                            } else if (!tvAddress.getText().toString().equals("") && pickupselected && !destinationselected) {
                                endLocation = tvAddress.getText().toString();
                                endLatlong = new LatLng(_latitude, _longitude);
                                System.out.println("*************************e" + endLatlong);
                                tvAddress_lable.setText("choose ur destination");
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(endLatlong);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                googleMap.addMarker(markerOptions);
                                pointer.setText("calculate distance");
                                destinationselected = true;
                            } else {
                                try {


                                    String url = getDirectionsUrl(startLatlong, endLatlong);
                                    DownloadTask downloadTask = new DownloadTask();
                                    downloadTask.execute(url);
                                    pickupselected = false;
                                    destinationselected = false;
                                    tvAddress_lable.setText("choose ur start point");
                                    pointer.setText("choose ur start point");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "your internet is not stable..", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloa", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = null;
            progressDialog = new ProgressDialog(UberMap.this);
            progressDialog.setMessage("Please wait calculating time and distance for ur Service");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = null;
            progressDialog = new ProgressDialog(UberMap.this);
            progressDialog.setMessage("Please wait we are routing ur service");
            progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duriation = "";

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duriation = (String) point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            Toast.makeText(getApplicationContext(), "Distance- " + distance + "time " + duriation, Toast.LENGTH_SHORT).show();
            // Drawing polyline in the Google Map for the i-th route
            try {
                googleMap.addPolyline(lineOptions);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Please select location within banglore", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
