package com.gpslocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GpsLocation extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isContinue = false;
    private boolean isGPS = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds
        //checking GPS Status
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        /*if (!isContinue) {
                            txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        } else {
                            stringBuilder.append(wayLatitude);
                            stringBuilder.append("-");
                            stringBuilder.append(wayLongitude);
                            stringBuilder.append("\n\n");
                            txtContinueLocation.setText(stringBuilder.toString());

                        }*/
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };
        getLocation();
    }

    public boolean checkGps(){
        if (!isGPS) {
            Toast.makeText(GpsLocation.this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return false;
        }
        isContinue = false;
        return true;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(GpsLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(GpsLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GpsLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        } else {
            if (checkGps()) {
                if (isContinue) {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(GpsLocation.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                             //   txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }
                    });

                }
            }
        }
    }

    public double getLatitude(){
        return wayLatitude;

    }
    public double getLongitude(){
        return wayLongitude;
    }

    public String getAddress(){
        if (wayLatitude == 0.0)
            return "";
        else {
            return getCompleteAddressString(wayLatitude, wayLongitude);
        }
    }

    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction", "Canont get Address!");
        }
        return strAdd;
    }

    public String getCity(){
        getLocation();
        String curCity = "";
        if (wayLatitude == 0.0)
            return curCity;
        else {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1);
                curCity = addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
           // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
          /*  String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();*/
        }
        return curCity;
    }

    public String getState(){
        getLocation();
        String curState = "";
        if (wayLatitude == 0.0)
            return curState;
        else {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1);
                curState = addresses.get(0).getAdminArea();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return curState;
    }

    public String getCountry(){
        getLocation();
        String curCountry = "";
        if (wayLatitude == 0.0)
            return curCountry;
        else {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1);
                curCountry = addresses.get(0).getCountryName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return curCountry;
    }

    public String getPostalCode(){
        getLocation();
        String postalCode = "";
        if (wayLatitude == 0.0)
            return postalCode;
        else {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1);
                postalCode = addresses.get(0).getPostalCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return postalCode;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
