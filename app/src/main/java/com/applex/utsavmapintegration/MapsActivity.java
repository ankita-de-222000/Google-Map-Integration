package com.applex.utsavmapintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.applex.utsavmapintegration.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import java.util.List;
import java.util.Locale;

import static com.applex.utsavmapintegration.Points.point1;
import static com.applex.utsavmapintegration.Points.point2;
import static com.applex.utsavmapintegration.Points.point3;
import static com.applex.utsavmapintegration.Points.point4;
import static com.applex.utsavmapintegration.Points.point5;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ActivityMapsBinding binding;

    private boolean isGpsCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Here is the GeoCoder which is working perfectly
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                // clearing the previous markers and circle in order to create new one
                mMap.clear();


                // getting current location
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // adding marker in current location
                mMap.addMarker(new MarkerOptions()
                        .position(myLocation)
                        .title("Marker in Sydney")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                // moving the camera in current location
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


//                // Here I am trying to create a 300m radius, getting all the points and adding marker to them
//                // I have created some static points around my location in Points.java and it's working properly
//                if (getDistance(myLocation, point1) < 300) {
//                    mMap.addMarker(new MarkerOptions().position(point1));
//                }
//                if (getDistance(myLocation, point2) < 300) {
//                    mMap.addMarker(new MarkerOptions().position(point2));
//                }
//                if (getDistance(myLocation, point3) < 300) {
//                    mMap.addMarker(new MarkerOptions().position(point3));
//                }
//                if (getDistance(myLocation, point4) < 300) {
//                    mMap.addMarker(new MarkerOptions().position(point4));
//                }
//                if (getDistance(myLocation, point5) < 300) {
//                    mMap.addMarker(new MarkerOptions().position(point5));
//                }

                // drawing a 500m circle just to check the area
                mMap.addCircle(new CircleOptions()
                        .center(myLocation)
                        .radius(500)
                        .strokeWidth(1f)
                        .strokeColor(getResources().getColor(R.color.blue_500)));

                locationManager.removeUpdates(locationListener);

                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addressList != null && addressList.size() > 0) {

                        // getting the full address here
                        Log.d(TAG, "onLocationChanged: " + addressList.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(!isGpsCalled) {
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 2);
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        isGpsCalled = true;
                    }
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        // get current location
        binding.fab.setOnClickListener(view -> {
            locationManager.removeUpdates(locationListener);
            // asking for network permissions here and you have to turn on your gps manually here
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 2);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                isGpsCalled = false;
            }
        });

        // Search by a city name
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                locationManager.removeUpdates(locationListener);
                try {
                    List<Address> addressList = geocoder.getFromLocationName(query, 1);
                    Log.d(TAG, "onLocationChanged: " + query);
                    if (addressList != null && addressList.size() > 0) {

                        // getting the full address here
                        Log.d(TAG, "onLocationChanged: " + addressList.toString());
                        mMap.clear();

                        // getting current location
                        LatLng newLocation = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());

                        // adding marker in current location
                        mMap.addMarker(new MarkerOptions()
                                .position(newLocation)
                                .title(addressList.get(0).getLocality())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                        // moving the camera in current location
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    public float getDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

}