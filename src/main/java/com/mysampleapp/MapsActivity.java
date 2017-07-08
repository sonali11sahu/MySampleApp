package com.mysampleapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMappingException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobilehelper.auth.IdentityManager;
import com.amazonaws.mobilehelper.auth.user.IdentityProfile;
import com.amazonaws.models.nosql.RoutesDO;
import com.amazonaws.models.nosql.ShuttleDO;
import com.amazonaws.models.nosql.StopDO;
import com.amazonaws.models.nosql.UserInfoDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,OnMapReadyCallback,
        GoogleMap.OnMapClickListener,GoogleApiClient.OnConnectionFailedListener, LocationListener,AdapterView.OnItemSelectedListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    private static final LatLng LIBRARY = new LatLng(37.655621, -122.056668);
    private static final LatLng RAW = new LatLng(37.654568, -122.053514);
    private static final LatLng STADIUM = new LatLng(37.657082, -122.060401);
    private static final LatLng UNIVERSITY_VILLGE = new LatLng(37.659703, -122.063931);
    private static final LatLng MUSIC_BLD = new LatLng(37.656464, -122.058567);
    private static  LatLng BUS = new LatLng(37.655963,-122.057601);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    final Context context = this;

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap = null;

    private IdentityManager identityManager;
    private GoogleApiClient mClient;
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private Location mCurrentLocation;
    private Spinner mStartCapSpinner;
    boolean trackEnabled = false;

    String busSelected;
    String shutlleID ="ULoop";
    LatLng nearestStop;
    String nearestStopId;
    double nearetStopDis;
    double shuttleDis;
    String lastSeen;

    /**
     * Keeps track of the selected marker.
     */
    private Marker mSelectedMarker;
    private String userID;
    private List<StopDO> results;
    private UserInfoDO user = new UserInfoDO();
    List<LatLng> dirPoints;

    List<String> stopseq;
    Map<String, Integer> stopInterval = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();


        setContentView(R.layout.shuttle_track);



        mStartCapSpinner = (Spinner)findViewById(R.id.startCapSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.shuttle_list, android.R.layout.simple_spinner_item);
        mStartCapSpinner.setAdapter(adapter);
        busSelected = (String)mStartCapSpinner.getItemAtPosition(0);

        Switch toggle = (Switch) findViewById(R.id.switch2);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   Log.i(TAG,"ON");
                    trackEnabled = true;
                } else {
                    Log.i(TAG,"OFF");
                    trackEnabled = false;
                    // The toggle is disabled
                }
            }
        });



        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(R.string.board_shuttle);
                builder.setMessage(getString(R.string.nosql_dialog_message_confirm_board)+" "+busSelected+"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                if(mCurrentLocation != null) {
                                    userID = AWSMobileClient.defaultMobileClient()
                                            .getIdentityManager()
                                            .getIdentityProfile().getUserName();

                                    Log.i(TAG, busSelected+" - USER LOCATION CLICK:: " + userID + " - " + mCurrentLocation.getLatitude() + " ; " + mCurrentLocation.getLongitude());
                                    new SaveUser().execute();
                                    button.setEnabled(false);
                                }

                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
                builder.show();
            }




// Specify the layout to use when the list of choices appears
           // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
       // mStartCapSpinner.setAdapter(adapter);
            // Obtain a reference to the mobile client. It is created in the Application class.
            final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

            // Obtain a reference to the identity manager.
           final IdentityManager identityManager = awsMobileClient.getIdentityManager();
           final IdentityProfile identityProfile = identityManager.getIdentityProfile();
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        busSelected = (String)parent.getItemAtPosition(pos);
        Log.i(TAG,"ITEM SELECT :: "+busSelected);


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't do anything here.

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mClient != null) {
            mClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        trackEnabled = false;
        super.onStop();
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mClient,
                        builder.build());

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(1);
            request.setInterval(10000);
            request.setFastestInterval(5000);

            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mClient);
            if (lastLocation != null) {
                double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
                mCurrentLocation = lastLocation;
                mLastLocation = lastLocation;
                Log.i(TAG, "onConnected location from NEW LA :: " + mCurrentLocation.getLatitude() + " LO :: " + mCurrentLocation.getLongitude());
            }
            else
            {
                PendingResult<Status> res = LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, this);
            }


            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            new OnMapAndViewReadyListener(mapFragment, this);

           /* LatLng near = clacNearestMark();
            if(near != null)
                Log.i("NEAR","stop get ::"+near.latitude+" , "+near.latitude);*/

            RetrieveData rd = new RetrieveData();
            LogUser lg = new LogUser();
            try {
                results = rd.execute().get();
                clacNearestMark();
                dirPoints = lg.execute().get();

            }catch (Exception e)
            {
                Log.i(TAG,"in running RetrieveData | LogUser");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.i(TAG,"onLocationChanged NOW IN ");
        mLastLocation = location;
        mCurrentLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Log.i(TAG,"onLocationChanged NOW NEW LOC :: "+location.getLatitude()+" :: "+location.getLongitude());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);*/
        Log.i(TAG,"onLocationChanged NOW NEW LOC :: "+location.getLatitude()+" :: "+location.getLongitude());
        addSelfToMap();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
        if(trackEnabled)
            if(calShuttleDis()< 0.1)
                new UpdateUserLoc().execute();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed Can't connect to Google Play Services!");
    }



    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

       // mMap.setOnMyLocationButtonClickListener(this);
      //  enableMyLocation();

        // Hide the zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add lots of markers to the map.


        addMarkersToMap();

        if(mCurrentLocation != null)
        addSelfToMap();

        // Set listener for marker click event.  See the bottom of this class for its behavior.
        mMap.setOnMarkerClickListener(this);

        // Set listener for map click event.  See the bottom of this class for its behavior.
        mMap.setOnMapClickListener(this);
        mStartCapSpinner.setOnItemSelectedListener(this);

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localized.
        map.setContentDescription("Demo showing how to close the info window when the currently"
                + " selected marker is re-tapped.");

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(LIBRARY)
                .include(RAW)
                .include(STADIUM)
                .include(UNIVERSITY_VILLGE)
                .include(MUSIC_BLD)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

        if(dirPoints != null)
        drawPath(dirPoints,Color.GREEN);

        Log.i(TAG,"onMapReady | END");

    }

    private class RetrieveData extends AsyncTask<String, Void, List<StopDO> > {

        @Override
        protected List<StopDO>  doInBackground(String... params) {
            //Toast.makeText(MainActivity.this, "in do in background", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "RetrieveData execute start");
            try {
                final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

                List<StopDO> itemList = mapper.scan(StopDO.class, new DynamoDBScanExpression());


                for (int i = 0; i < itemList.size(); i++) {
                    System.out.println(itemList.get(i).getStopName());
                    System.out.println(itemList.get(i).getStopLat());
                    System.out.println(itemList.get(i).getStopLon());
                }
                Log.i(TAG, "RetrieveData the entries execute " + itemList.size());


                List<RoutesDO> route = mapper.scan(RoutesDO.class, new DynamoDBScanExpression());
                for (int i = 0; i < itemList.size(); i++) {
                    route.get(0).getStopSeq();
                    stopseq =  Arrays.asList(route.get(0).getStopSeq().split("\\s*,\\s*"));
                    stopInterval.put(stopseq.get(i),i);

                }

                return itemList;

            } catch (DynamoDBMappingException ex) {
                ex.printStackTrace();
                return null;
            }
        }



        @Override
        protected void onPostExecute(List<StopDO> result) {

            Log.i(TAG, "the entries RETRIVE DATA in onPostExecute :: " + result.size());
            results = result;
          //  markerLoad = true;
            //onMapReady(mMap);
           // addMarkersToMap();

        }
    }

    private class SaveUser extends AsyncTask<String, Void, String > {

        @Override
        protected String  doInBackground(String... params) {
            //Toast.makeText(MainActivity.this, "in do in background", Toast.LENGTH_SHORT).show();

            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

            String OLD_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
            SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
            Date d = new Date();
            String time =sdf.format(d);

            try {
                Log.i(TAG, "SaveUser the entries  in stopSelected :: "+busSelected );
                user.setUserId(userID);
                user.setRecentReportTime(time);
                user.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
                user.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
                mapper.save(user);

                ShuttleDO sh = new ShuttleDO();
                sh.setReporteBy(userID);
                sh.setShuttleId(shutlleID);
                sh.setReportTime(time);
                if(nearetStopDis <= 0.2 )//|| shuttleDis < 0.1
                {
                    sh.setShuttleLat(String.valueOf(mCurrentLocation.getLatitude()));
                    sh.setShuttleLon(String.valueOf(mCurrentLocation.getLongitude()));
                }
                else
                {
                    sh.setShuttleLat(String.valueOf(nearestStop.latitude));
                    sh.setShuttleLon(String.valueOf(nearestStop.longitude));
                }
                if(nearetStopDis >= 1.0)
                    trackEnabled = false;
                sh.setShuttleLon(String.valueOf(mCurrentLocation.getLongitude()));
                sh.setShuttleLatestStop(nearestStopId); //get nearest stop
                mapper.save(sh);

                return "UPDATED";
            } catch (final AmazonClientException ex) {
                // Restore original data if save fails, and re-throw.
                ex.printStackTrace();
                return "FAIL";

            }
        }


        @Override
        protected void onPostExecute(String res) {

            Log.i(TAG, "SaveUser | the entries are in onPostExecute :: "+res );

        }
    }

    private class UpdateUserLoc extends AsyncTask<String, Void, String > {

        @Override
        protected String doInBackground(String... params) {
            //Toast.makeText(MainActivity.this, "in do in background", Toast.LENGTH_SHORT).show();

            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

            String OLD_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
            SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
            Date d = new Date();
            String time = sdf.format(d);

            try {
                Log.i(TAG, "UpdateUserLoc the entries  in stopSelected :: " + busSelected);
                user.setUserId(userID);
                user.setRecentReportTime(time);
                user.setLatitude(String.valueOf(mCurrentLocation.getLatitude()));
                user.setLongitude(String.valueOf(mCurrentLocation.getLongitude()));
                mapper.save(user);

                ShuttleDO sh = new ShuttleDO();
                sh.setReporteBy(userID);
                sh.setShuttleId(shutlleID);
                sh.setReportTime(time);
                if (shuttleDis < 0.1)//|| shuttleDis < 0.1
                {
                    sh.setShuttleLat(String.valueOf(mCurrentLocation.getLatitude()));
                    sh.setShuttleLon(String.valueOf(mCurrentLocation.getLongitude()));
                } else {
                    sh.setShuttleLat(String.valueOf(nearestStop.latitude));
                    sh.setShuttleLon(String.valueOf(nearestStop.longitude));
                }
                if (shuttleDis >= 1.0)
                    trackEnabled = false;
                sh.setShuttleLon(String.valueOf(mCurrentLocation.getLongitude()));
                sh.setShuttleLatestStop(nearestStopId); //get nearest stop
                mapper.save(sh);

                return "UPDATED";
            } catch (final AmazonClientException ex) {
                // Restore original data if save fails, and re-throw.
                ex.printStackTrace();
                return "FAIL";

            }
        }

    @Override
    protected void onPostExecute(String res) {

        Log.i(TAG, "SaveUser | the entries are in onPostExecute :: "+res );

    }
}


    public double rad(double x) { return x*Math.PI/180; }

    public String clacNearestMark()
    {
        Log.i(TAG,"in clacNearestMark");
        if(mCurrentLocation != null) {
            double lat = Double.valueOf(mCurrentLocation.getLatitude());
            double lng = Double.valueOf(mCurrentLocation.getLongitude());
            double R = 6371; // radius of earth in km
            //ArrayList<Double> distances = new ArrayList<Double>();
            int closest = -1;
            for (int i = 0; i < results.size(); i++) {
                double mlat = Double.valueOf(results.get(i).getStopLat());
                double mlng = Double.valueOf(results.get(i).getStopLon());
                double dLat = rad(mlat - lat);
                double dLong = rad(mlng - lng);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(rad(lat)) * Math.cos(rad(lat)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double d = R * c;
                results.get(i).distance = d; //distances.add(d);
                Log.i(TAG, results.get(i).getStopId()+" :: distance is :: " +d);
                if (closest == -1 || d < results.get(closest).distance) {
                    closest = i;
                    nearetStopDis = d;
                }
            }
            Log.i(TAG, "closest distance is :: " +nearetStopDis+" -> "+results.get(closest).getStopName()+" : "+results.get(closest).getStopLat()+", "+results.get(closest).getStopLon());

            nearestStop = new LatLng(Double.valueOf(results.get(closest).getStopLat()), Double.valueOf(results.get(closest).getStopLon()));
            return results.get(closest).getStopId();
        }
        return null;
    }

    public double calShuttleDis()
    {
        double d = 0;
        double R = 6371;
        if(mCurrentLocation != null) {
            double lat = Double.valueOf(mCurrentLocation.getLatitude());
            double lng = Double.valueOf(mCurrentLocation.getLongitude());
            double mlat = Double.valueOf(BUS.latitude);
            double mlng = Double.valueOf(BUS.longitude);
            double dLat = rad(mlat - lat);
            double dLong = rad(mlng - lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(lat)) * Math.cos(rad(lat)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            d = R * c;
        }
        shuttleDis = d;
        return shuttleDis;
    }

    private List<LatLng> decodePoly(String encoded) {

        Log.i(TAG, "drawPath | start");

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    public List<LatLng> getPath(String  result) {

        try {
            //Tranform the string into a json object

            Log.i(TAG, "drawPath | start");

            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            //dirPoints = list;
            for(int i =0; i<list.size();i++)
            {
                Log.i(TAG, "drawPath | point : "+i+" : " +list.get(i).latitude+", "+list.get(i).longitude);
            }

           return list;
        }
        catch (JSONException e) {
        return null;
        }
    }

    private class LogUser extends AsyncTask<Void, Void, List<LatLng> > {

        @Override
        protected List<LatLng> doInBackground(Void... urls) {

            if(mCurrentLocation != null) {
                double lat1 = Double.valueOf(mCurrentLocation.getLatitude());

                double lon1 = Double.valueOf(mCurrentLocation.getLongitude());
//AIzaSyDAVPQQ_H6R_HQjR0UaneW9DiItItph44E
// https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626&key=AIzaSyDAVPQQ_H6R_HQjR0UaneW9DiItItph44E
                  try {
                    String sURL = getString(R.string.calc_direction_url) + getString(R.string.url_origin) + lat1 + "%2C" + lon1
                            + getString(R.string.url_destination) + nearestStop.latitude+"%2C"+nearestStop.longitude+ getString(R.string.api_key);
                    Log.i(TAG,"LogUser | Req url :: "+sURL);
                    URL url = new URL(sURL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        String response = stringBuilder.toString();

                        return getPath(response);
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    return null;
                }
            }
            return null;
        }

        private class Path extends AsyncTask<LatLng, Void, List<LatLng> > {

            LatLng param1, param2;
            @Override
            protected List<LatLng> doInBackground(LatLng... pParams) {

                param1 = pParams[0];
                param2 = pParams[1];

                try {
                        String sURL = getString(R.string.calc_direction_url) + getString(R.string.url_origin) + param1.latitude + "%2C" + param1.latitude
                                + getString(R.string.url_destination) + param2.latitude+"%2C"+param2.longitude+ getString(R.string.api_key);
                        Log.i(TAG,"LogUser | Req url :: "+sURL);
                        URL url = new URL(sURL);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line).append("\n");
                            }
                            bufferedReader.close();
                            String response = stringBuilder.toString();
                            return getPath(response);

                        } finally {
                            urlConnection.disconnect();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        return null;
                    }
                }

            }

        protected void onPostExecute(List<LatLng> response) {
            if (response == null) {
                Log.i(TAG, "LogUser onPostExecute | Res :THERE WAS AN ERROR");
            }
            dirPoints = response;
        }
    }

    private  void adMar(){
        Log.i(TAG,"addMarkersToMap | RESULTS LIST NULL");

        mMap.addMarker(new MarkerOptions()
                .position(LIBRARY)
                .title("LIBRARY")
                .snippet("ETA: 20min"));

        mMap.addMarker(new MarkerOptions()
                .position(RAW)
                .title("RAW")
                .snippet("ETA: 50min"));

        mMap.addMarker(new MarkerOptions()
                .position(UNIVERSITY_VILLGE)
                .title("UNIVERSITY_VILLGE")
                .snippet("ETA: 10min"));

        mMap.addMarker(new MarkerOptions()
                .position(MUSIC_BLD)
                .title("MUSIC_BLD")
                .snippet("ETA: 12min"));

        mMap.addMarker(new MarkerOptions()
                .position(STADIUM)
                .title("STADIUM")
                .snippet("ETA: 15min"));

        mMap.addMarker(new MarkerOptions()
                .position(BUS)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
                .title("BUS")
                .snippet("Going to University"));

    }

    private MarkerOptions getBus()
    {
        MarkerOptions bus = new MarkerOptions();
        bus.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        bus.title("BUS");
        try {
            BUS  = new RetrieveShuttle().execute().get();
        }catch (Exception e)
        {
            Log.i(TAG, "getBus | EXCEPTION :: "+e.getCause());
            e.printStackTrace();
        }
        if(BUS == null)
        {
            BUS = new LatLng(37.655963,-122.057601);
            Log.i(TAG, "getBus | STATIC VALUE :: ");
        }

        bus.position(BUS);
        bus.snippet("Last reported here at "+lastSeen);
        return bus;

    }
    private void addMarkersToMap() {

        Log.i(TAG, "addMarkersToMap |TEST DATA FROM DB");
        DecimalFormat df = new DecimalFormat("###.##");



            Log.i(TAG,"addMarkersToMap | RESULTS :: "+results.size());

            for (int i = 0; i < results.size(); i++) {
                LatLng position = new LatLng(Double.valueOf(results.get(i).getStopLat()), Double.valueOf(results.get(i).getStopLon()));
                Log.i(TAG,"position | RESULTS :: "+position.toString());
                String m = String.valueOf(df.format(results.get(i).distance)) +" miles away";
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(results.get(i).getStopName())
                        .snippet(m)); // call db for ETA
                Log.i(TAG, "addMarkersToMap | stopName :: " + results.get(i).getStopName());
            }

        mMap.addMarker(getBus());


        /*mMap.addPolyline(new PolylineOptions()
                .add(RAW, LIBRARY,MUSIC_BLD, STADIUM, UNIVERSITY_VILLGE )
                .width(10)
                .geodesic(true)
                .color(Color.DKGRAY));*/

    }

    private void drawPathUser()
    {
        Log.i(TAG,"drawPath | start");

        if(dirPoints != null)
        mMap.addPolyline(new PolylineOptions()
                .addAll(dirPoints)
                .width(10)
                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                .geodesic(true)
        );
        else
            Log.i(TAG,"drawPath | NO points loaded");

    }

    private void drawPath(List<LatLng> list, int color)
    {
        Log.i(TAG,"drawPath | start");

        if(list != null)
            mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(10)
                    .color(color)//Google maps blue color
                    .geodesic(true)
            );
        else
            Log.i(TAG,"drawPath | NO points loaded");

    }

    private void addSelfToMap() {

        if(mCurrentLocation == null)
            mCurrentLocation = mLastLocation;
        double longitude = mCurrentLocation.getLongitude();
        double latitude = mCurrentLocation.getLatitude();

        Log.i(TAG,"location from intent LA :: "+latitude+" LO :: "+longitude);

        mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
               .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)) //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title("YOU")
                .snippet("YEH"));

    }


    private class RetrieveShuttle extends AsyncTask<String, Void, LatLng > {

        @Override
        protected LatLng  doInBackground(String... params) {
            //Toast.makeText(MainActivity.this, "in do in background", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "RetrieveShuttle execute start");
            try {
                final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

                String OLD_FORMAT = "yyyy-MM-dd";
                String FORMAT = "yyyy-MM-dd HH:mm:ssZ";
                SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
                Date d = new Date();
                String time =sdf.format(d);
                Log.i(TAG, "RetrieveShuttle searchTime : "+time);

                Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
                eav.put(":val1", new AttributeValue().withS(shutlleID));
                eav.put(":val2", new AttributeValue().withS(time));
                //eav.put(":val2", new AttributeValue().withS());
                //.withFilterExpression("begins_with(reportTime, :val2)")

                DynamoDBScanExpression scanQ = new DynamoDBScanExpression()
                        .withFilterExpression("shuttleId = :val1 AND begins_with(reportTime, :val2)")
                        .withExpressionAttributeValues(eav);
                 int latest = 0;
                Date lTime;
                sdf  = new SimpleDateFormat(FORMAT);
                List<ShuttleDO> latestReplies = mapper.scan(ShuttleDO.class, scanQ);
                if(latestReplies !=null){
                    try {
                    for (int i = 0; i < latestReplies.size(); i++) {
                        lTime = sdf.parse(latestReplies.get(latest).getReportTime());

                        Date cTime = sdf.parse(latestReplies.get(i).getReportTime());

                        Log.i(TAG, "RetrieveShuttle Time UnFormatted : "+ latestReplies.get(i).getReportTime());
                        Log.i(TAG, "RetrieveShuttle Time : "+ cTime.toString());

                        if((lTime.getHours()== cTime.getHours() && lTime.getMinutes()<=cTime.getMinutes()) || (lTime.getHours()< cTime.getHours()))
                        {
                            latest = i;
                        }
                    }
                        Log.i(TAG, "RetrieveShuttle latest pos "+latest);
                    BUS = new LatLng(Double.valueOf(latestReplies.get(latest).getShuttleLat()),Double.valueOf(latestReplies.get(latest).getShuttleLon()));

                       Date ls =sdf.parse(latestReplies.get(latest).getReportTime());
                        sdf  = new SimpleDateFormat("HH:mm");
                        lastSeen = sdf.format(ls);
                        Log.i(TAG, "RetrieveShuttle lastSeen : "+lastSeen);


                    }catch (Exception e){
                        Log.i(TAG, "RetrieveShuttle execute error "+e.getCause());
                        e.printStackTrace();

                    }
                }


              //  BUS = new LatLng(Double.valueOf(latestReplies.get(0).getShuttleLat()),Double.valueOf(latestReplies.get(0).getShuttleLon()));
                Log.i(TAG, "RetrieveShuttle | ShuttleDO entries execute " + BUS.latitude+","+BUS.longitude);
                return BUS;

            } catch (DynamoDBMappingException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LatLng result) {

            Log.i(TAG, "the entries RetrieveShuttle in onPostExecute :: " + result.latitude+","+result.longitude);

        }
    }

    @Override
    public void onMapClick(final LatLng point) {
        // Any showing info window closes when the map is clicked.
        // Clear the currently selected marker.
        mSelectedMarker = null;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // The user has re-tapped on the marker which was already showing an info window.
        if (marker.equals(mSelectedMarker)) {
            // The showing info window has already been closed - that's the first thing to happen
            // when any marker is clicked.
            // Return true to indicate we have consumed the event and that we do not want the
            // the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            mSelectedMarker = null;
            return true;
        }

        mSelectedMarker = marker;

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur.
        return false;
    }
}
