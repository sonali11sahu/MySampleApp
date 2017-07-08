package com.mysampleapp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMappingException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobilehelper.auth.IdentityManager;
import com.amazonaws.mobilehelper.auth.user.IdentityProfile;
import com.amazonaws.models.nosql.ReportDO;
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
import com.mysampleapp.demo.DemoFragmentBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by sonalisahu on 5/26/17.
 */

public class HomeMapFragment extends DemoFragmentBase implements GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,OnMapReadyCallback,
        GoogleMap.OnMapClickListener,GoogleApiClient.OnConnectionFailedListener, LocationListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener  {

    private static final LatLng LIBRARY = new LatLng(37.655621, -122.056668);
    private static final LatLng RAW = new LatLng(37.654568, -122.053514);
    private static final LatLng STADIUM = new LatLng(37.657082, -122.060401);
    private static final LatLng UNIVERSITY_VILLGE = new LatLng(37.659703, -122.063931);
    private static final LatLng MUSIC_BLD = new LatLng(37.656464, -122.058567);
    private static  LatLng BUS = new LatLng(37.655963,-122.057601);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Location mLastLocation;
    Marker mCurrLocationMarker;

    private static final String TAG = HomeMapFragment.class.getSimpleName();
    private GoogleMap mMap = null;

    private IdentityManager identityManager;
    private GoogleApiClient mClient;
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
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
    SupportMapFragment fragment;
    String nTime;
    String nxtTime;

    /**
     * Keeps track of the selected marker.
     */
    private Marker mSelectedMarker;
    private String userID;
    private List<StopDO> results;
    private UserInfoDO user = new UserInfoDO();
    private View mFragmentView;
    List<LatLng> dirPoints;
    String lastSeen;
     String startTime = "7:30:00";
     String endTime = "19:30:00";
    Date esTime;
    private TextView etaMsg;
    private TextView nxtMsg;
    private TextView tmMsg;

    List<String> stopseq;
    Map<String, Integer> stopInterval = new HashMap<String, Integer>();


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        mFragmentView = inflater.inflate(R.layout.report_delay, container, false);

        return mFragmentView;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(getActivity(), "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.i(TAG,"onActivityCreated start");
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
//            new OnMapAndViewReadyListener(fragment, this);


            mClient = new GoogleApiClient.Builder(getActivity(), this, this).addApi(LocationServices.API).build();


            etaMsg = (TextView) mFragmentView.findViewById(R.id.eta_msg);
            tmMsg = (TextView) mFragmentView.findViewById(R.id.time_msg);
            nxtMsg = (TextView) mFragmentView.findViewById(R.id.nxt_msg);


            final Button button = (Button) mFragmentView.findViewById(R.id.button22);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(mCurrentLocation != null)
                    {
                        userID = AWSMobileClient.defaultMobileClient()
                                .getIdentityManager()
                                .getIdentityProfile().getUserName();

                        Log.i(TAG,"USER LOCATION CLICK:: "+userID+" - "+mCurrentLocation.getLatitude()+" ; "+mCurrentLocation.getLongitude());
                        String msg = "University Shuttle Delay Reported at "+ new SimpleDateFormat("HH:mm").format(new Date()).toString();
                        new SaveUser().execute();
                        button.setEnabled(false);
                        Toast.makeText(getContext(),msg, Toast.LENGTH_LONG).show();
                    }

                }

       final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

                // Obtain a reference to the identity manager.
                final IdentityManager identityManager = awsMobileClient.getIdentityManager();
                final IdentityProfile identityProfile = identityManager.getIdentityProfile();
            });


            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                        PERMISSION_ACCESS_COARSE_LOCATION);
            }

            if (mClient != null) {
                mClient.connect();
            }

        }


/***at this time google play services are not initialize so get map and add what ever you want to it in onResume() or onStart() **/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap == null) {
            fragment.getMapAsync(this);
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
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


            new OnMapAndViewReadyListener(fragment, this);
           RetrieveData rd = new RetrieveData();
            LogUser lg = new LogUser();
            try {
                results = rd.execute().get();

                nearestStopId = clacNearestMark();
                dirPoints = lg.execute().get();

                Log.i(TAG,"onActivityCreated Estimated Shuttle time "+nearestStopId+" :: "+calDelay());
                String msg = "The Scheduled shuttle Arrival time: "+calDelay();
                etaMsg.setText(msg);
                msg = "The Current time: "+nTime;
                tmMsg.setText(msg);
                msg = "The Next shuttle Arrival time: "+nxtTime;
                nxtMsg.setText(msg);

            }catch (Exception e)
            {
                Log.i(TAG,"in running RetrieveData | LogUser");
                e.printStackTrace();
            }
            addMarkersToMap();

            if(mCurrentLocation != null)
                addSelfToMap();

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

        Log.i(TAG,"onLocationChanged NOW NEW LOC :: "+location.getLatitude()+" :: "+location.getLongitude());
        addSelfToMap();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed Can't connect to Google Play Services!");
    }



    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Add lots of markers to the map.



        // Set listener for marker click event.  See the bottom of this class for its behavior.
        mMap.setOnMarkerClickListener(this);

        // Set listener for map click event.  See the bottom of this class for its behavior.
        mMap.setOnMapClickListener(this);

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
        drawPath(dirPoints);

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
                    stopInterval.put(stopseq.get(i),i*2);

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
                Log.i(TAG, "SaveUser the entries  in nearestStopId :: "+nearestStopId );

                ReportDO sh = new ReportDO();
                sh.setReportedBy(userID);
                sh.setShuttleId(shutlleID);
                sh.setReportTime(time);
                sh.setStopId(nearestStopId);
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

    public String calDelay(){
        double delay = 0;
        DateFormat sdf = new SimpleDateFormat("HH:mm");

        try{

            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            Calendar nc = Calendar.getInstance();
            nc.setTime(now);

            Date stime = sdf.parse(startTime);
            cal.set(now.getYear(),now.getMonth(),now.getDate(),stime.getHours(),stime.getMinutes());
            cal.add(Calendar.MINUTE,stopInterval.get(nearestStopId));
            stime = cal.getTime();

            Date etime = sdf.parse(endTime);
            cal.set(now.getYear(),now.getMonth(),now.getDate(),etime.getHours(),etime.getMinutes());
            cal.add(Calendar.MINUTE,stopInterval.get(nearestStopId));
            etime = cal.getTime();

            nTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

            Log.i(TAG,"in calDelay NOW : "+nTime);

           // Log.i(TAG," Time start: "+sdf.parse(stime.toString()));
           // Log.i(TAG," Time end: "+sdf.parse(etime.toString()));

            //if(now.after(stime)) {

                for (int i = 0; i < 48; i++) {
                    cal.setTime(stime);
                    cal.add(Calendar.MINUTE, 15 * i);
                    Date expTime = cal.getTime();
                    Log.i(TAG, "est Time : " + sdf.format(expTime));

                    Log.i(TAG,"now.before(etime) : "+((expTime.getHours() == now.getHours()) && (expTime.getMinutes() >= now.getMinutes())));

                    if ((expTime.getHours() == now.getHours()) && (expTime.getMinutes() >= now.getMinutes()))
                        break;
                    else
                        esTime = expTime;
                }
           // }
            cal.setTime(esTime);
            cal.add(Calendar.MINUTE, 15);
            Date nt = cal.getTime();
            nxtTime = sdf.format(nt);

            Log.i(TAG,"previous shuttle Time : "+esTime);
            Log.i(TAG,"current Time : "+nTime);
            Log.i(TAG,"Next shuttle time : "+nxtTime);

        }catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
        return sdf.format(esTime);
    }

    public double rad(double x) { return x*Math.PI/180; }

    public String clacNearestMark()
    {
        Log.i(TAG,"in clacNearestMark");
        if(mCurrentLocation != null) {
            double lat = Double.valueOf(mCurrentLocation.getLatitude());
            double lng = Double.valueOf(mCurrentLocation.getLongitude());
            double R = 6371; // radius of earth in km
            ArrayList<Double> distances = new ArrayList<Double>();
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
                distances.add(d);
                Log.i(TAG, results.get(i).getStopId()+" :: distance is :: " +d);
                if (closest == -1 || d < distances.get(closest)) {
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



            Log.i(TAG,"addMarkersToMap | RESULTS :: "+results.size());

            for (int i = 0; i < results.size(); i++) {
                LatLng position = new LatLng(Double.valueOf(results.get(i).getStopLat()), Double.valueOf(results.get(i).getStopLon()));
                Log.i(TAG,"position | RESULTS :: "+position.toString());
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(results.get(i).getStopName()));
                        //.snippet("ETA: "+i+" min")); // call db for ETA
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

    private void drawPath(List<LatLng> list)
    {
        Log.i(TAG,"drawPath | start");

        if(list != null)
            mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(10)
                    .color(Color.GREEN)//Google maps blue color
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

                        Date ls =sdf.parse(latestReplies.get(latest).getReportTime().toString());
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
