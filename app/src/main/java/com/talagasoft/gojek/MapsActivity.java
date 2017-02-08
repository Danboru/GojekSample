package com.talagasoft.gojek;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.w3c.dom.Document;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AbstractMapActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        LocationListener {

    Double mToLat,mToLng;
    LatLng myLatLng;
    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();


    // Button
    Button btnShowOnMap;

    Location location;

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();


    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "jakarta"; // Place area name

    private boolean needsInit=false;
    LocationManager locationManager;
    GoogleMap map;
    Criteria criteria;

    List<Driver> arrDriver = new ArrayList<Driver>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mToLat = getIntent().getExtras().getDouble("Latitude");
        //mToLng = getIntent().getExtras().getDouble("Longitude");

        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(MapsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
        if (readyToGo()) {
            setContentView(R.layout.activity_maps);

            MapFragment mapFrag=(MapFragment)getFragmentManager().findFragmentById(R.id.map);

            if (savedInstanceState == null) {
                needsInit=true;
            }

            mapFrag.getMapAsync(this);

        }
    }
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map=map;
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.criteria = new Criteria();
        if (needsInit) {
           /*
            CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(40.76793169992044,
                            -73.98180484771729));
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            map.moveCamera(center);
            map.animateCamera(zoom);
            */
        }

        map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        map.setOnInfoWindowClickListener(this);

        map.setMyLocationEnabled(true);

        myLocation();

        getDriverCurrentLocation();
        addMarkerDrivers();


    }
    private double getRadius(int inKm){
        double latDistance = Math.toRadians(myLatLng.latitude - inKm);
        double lngDistance = Math.toRadians(myLatLng.longitude - inKm);
        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
                (Math.cos(Math.toRadians(myLatLng.latitude))) *
                        (Math.cos(Math.toRadians(inKm))) *
                        (Math.sin(lngDistance / 2)) *
                        (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = 6371 / c;
        if (dist<50){
                    /* Include your code here to display your records */
        }
        return dist;

    }
    private void getDriverCurrentLocation(){
        arrDriver.add( new Driver(1, "Andri",-6.584711,107.4667,"Rumah Makan Ganto",1));
        arrDriver.add( new Driver(2, "Iyang",-6.586847,107.4681,"Kantor Pos",1));
        arrDriver.add( new Driver(3, "Nita",-6.586388,107.4701,"Toko Pipin",1));
        arrDriver.add( new Driver(4, "Bagus",-6.586388,107.4701,"Salabaya",1));
    }
    public void addMarkerDrivers(){
        for(int i=0;i<arrDriver.size();i++) {
            Driver driver=arrDriver.get(i);
            addMarker(map, driver.getLat(), driver.getLng(),
                    driver.getName(), driver.getPhone());
        }
    }
    public void myLocation(){
        location = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 1, (LocationListener) this);
        myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveToCurrentLocation(map,myLatLng);
        // push to server myLatLng and get driver for current locations
        PushMyLatLng();
    }
    private void PushMyLatLng(){

    }
    public void DrawRoute(LatLng from,LatLng to){

        String mDistance = getDistance(from,to);

        Toast.makeText(getApplicationContext(), ""+mDistance, Toast.LENGTH_LONG).show();

        Polygon polygon = map.addPolygon(new PolygonOptions()
                .add(from,to)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        Document document= null;
        try {
            document = new GMapV2Direction().getDocument(from,to,"drive");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ArrayList<LatLng> oLat=new GMapV2Direction().getDirection(document);

    }


    public String getDistance(LatLng my_latlong, LatLng frnd_latlong) {
        Location l1 = new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2 = new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance = l1.distanceTo(l2);
        String dist = distance + " M";

        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }
        return dist;
    }
    public double CalculationByDistance(double initialLat, double initialLong, double finalLat, double finalLong){
    /*PRE: All the input values are in radians!*/

        double latDiff = finalLat - initialLat;
        double longDiff = finalLong - initialLong;
        double earthRadius = 6371; //In Km if you want the distance in km

        double distance = 2*earthRadius*Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff/2.0),2)+Math.cos(initialLat)*Math.cos(finalLat)*Math.pow(Math.sin(longDiff/2),2)));

        return distance;

    }
    private void moveToCurrentLocation(GoogleMap mMap, LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    private void addMarker(GoogleMap map, double lat, double lon,
                           int title, int snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(getString(title))
                .snippet(getString(snippet)));
    }
    private void addMarker(GoogleMap map, double lat, double lon,
                           String title, String snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(title)
                .snippet(snippet));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getClass().getSimpleName(),
                String.format("%f:%f", location.getLatitude(),
                        location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
