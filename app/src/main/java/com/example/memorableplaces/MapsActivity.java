package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.memorableplaces.MainActivity.places;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    public void centerMapOnLocation(Location location,String title){
        LatLng userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userCurrentLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation,12));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(lastKnownLocation,"You are here");
                }
            }
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if(intent.getIntExtra("placeNumber",0)==0)
        {   //zoom-in on user's location
            locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                 centerMapOnLocation(location,"You are here");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1); //asking permission from user to access user's location
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //setting up GPS Location if the user gives permission
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"You are here");
            }
        }
        else{
            Location placeLocation = new Location((LocationManager.GPS_PROVIDER));
            placeLocation.setLatitude(MainActivity.location.get(MainActivity.p).latitude);
            placeLocation.setLongitude(MainActivity.location.get(MainActivity.p).longitude);
            Log.i("info",String.valueOf(MainActivity.p));
            centerMapOnLocation(placeLocation, places.get(MainActivity.q));
        }


    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try{
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddresses!=null && listAddresses.size()>0){
                if(listAddresses.get(0).getFeatureName() != null){
                    address += listAddresses.get(0).getFeatureName() + ", ";
                }
                if(listAddresses.get(0).getThoroughfare()!=null){
                    address += listAddresses.get(0).getThoroughfare() + ", ";
                }
                if(listAddresses.get(0).getSubAdminArea()!= null);{
                    address += listAddresses.get(0).getSubAdminArea() + ", ";
                }
                if (listAddresses.get(0).getAdminArea()!= null){
                    address += listAddresses.get(0).getAdminArea() ;
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        places.add(address);
        MainActivity.location.add(latLng);
        MainActivity.myArrayAdapter.notifyDataSetChanged();
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        try{
            ArrayList<String> latitude = new ArrayList<String>();
            ArrayList<String> longitude = new ArrayList<String>();
            for(LatLng coord: MainActivity.location){
                latitude.add(Double.toString(coord.latitude));
                longitude.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("latitude", ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("longitude", ObjectSerializer.serialize(longitude)).apply();
            Log.i("memorable places:",ObjectSerializer.serialize(places));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Toast.makeText(this, "Location has been saved!", Toast.LENGTH_LONG).show();
    }
}
