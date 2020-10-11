package com.netpaisa.faceneed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private StringBuilder stringBuilder;
    private boolean isContinue = false;
    private boolean isGPS = false;
    private TextView location_txt;
    public static final String LOCATION ="LOCATION";
    public static final String PINCODE="PINCODE";
    public static final String HOSPITAL="HOSPITAL";
    public static final String RESTURANT="RESTURANT";
    public static final String CITY="CITY";
    public static final String DESTINATION="DESTINATION";
    private Button dest,pin,loc,city,restaurants,hospital,bus,wine,malls,hotel;
    String address,city_l,state,country,postalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location_txt = findViewById(R.id.location_txt);
        dest =findViewById(R.id.destination);
        pin = findViewById(R.id.pincode);
        loc = findViewById(R.id.location);
        city = findViewById(R.id.city);
        restaurants = findViewById(R.id.restaurants);
        hospital = findViewById(R.id.hospital);
        bus = findViewById(R.id.bus);
        hotel = findViewById(R.id.hotel);
        wine = findViewById(R.id.wine);
        malls = findViewById(R.id.malls);
        dest.setOnClickListener(this);
        pin.setOnClickListener(this);
        loc.setOnClickListener(this);
        city.setOnClickListener(this);
        restaurants.setOnClickListener(this);
        hospital.setOnClickListener(this);
        bus.setOnClickListener(this);
        hotel.setOnClickListener(this);
        wine.setOnClickListener(this);
        malls.setOnClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        new GpsUtils(this).turnGPSOn(isGPSEnable -> {
            // turn on GPS
            isGPS = isGPSEnable;
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
                        //Log.e("city", String.valueOf(wayLongitude));
                        if (!isContinue) {
                            //txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                address = addresses.get(0).getAddressLine(0);
                                city_l = addresses.get(0).getLocality();
                                state = addresses.get(0).getAdminArea();
                                country = addresses.get(0).getCountryName();
                                postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                Log.e("city1",address+city+state+country);
                                //location_txt.setText("Location : " +address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            stringBuilder.append(wayLatitude);
                            stringBuilder.append("-");
                            stringBuilder.append(wayLongitude);
                            stringBuilder.append("\n\n");
                            //txtContinueLocation.setText(stringBuilder.toString());
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                address = addresses.get(0).getAddressLine(0);
                                city_l = addresses.get(0).getLocality();
                                state = addresses.get(0).getAdminArea();
                                country = addresses.get(0).getCountryName();
                                postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                Log.e("city2",address+city+state+country);
                               // location_txt.setText("Location : " +address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        isContinue = false;
        getLocation();
        if (!isGPS) {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        isContinue = true;
        stringBuilder = new StringBuilder();
        getLocation();
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constant.LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        //txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                                //txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.destination:
                openDialog(DESTINATION);
                break;
            case R.id.pincode:
                openDialog(PINCODE);
                break;
            case R.id.location:
                openDialog(LOCATION);
                break;
            case R.id.city:
                openDialog(CITY);
                break;
            case R.id.restaurants:
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=restaurants+near+me");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case R.id.hospital:
                Uri uri = Uri.parse("geo:"+ 0 + "," + 0 +"?q=hospitals+near+me");
                Intent map = new Intent(Intent.ACTION_VIEW, uri);
                map.setPackage("com.google.android.apps.maps");
                startActivity(map);
                break;
            case R.id.hotel:
                Uri h_uri = Uri.parse("geo:"+ 0 + "," + 0 +"?q=hotels+near+me");
                Intent h_map = new Intent(Intent.ACTION_VIEW, h_uri);
                h_map.setPackage("com.google.android.apps.maps");
                startActivity(h_map);
                break;
            case R.id.bus:
                Uri b_uri = Uri.parse("geo:"+ 0 + "," + 0 +"?q=busstops+near+me");
                Intent b_map = new Intent(Intent.ACTION_VIEW, b_uri);
                b_map.setPackage("com.google.android.apps.maps");
                startActivity(b_map);
                break;
            case R.id.wine:
                Uri w_uri = Uri.parse("geo:"+ 0 + "," + 0 +"?q=wineshop+near+me");
                Intent w_map = new Intent(Intent.ACTION_VIEW, w_uri);
                w_map.setPackage("com.google.android.apps.maps");
                startActivity(w_map);
                break;
            case R.id.malls:
                Uri m_uri = Uri.parse("geo:"+ 0 + "," + 0 +"?q=shopingmalls+near+me");
                Intent m_map = new Intent(Intent.ACTION_VIEW, m_uri);
                m_map.setPackage("com.google.android.apps.maps");
                startActivity(m_map);
                break;
            default:
                break;
        }
    }


    private  void openDialog(final String s){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_layout);
        dialog.setCancelable(true);
        EditText edtPin  = dialog.findViewById(R.id.pin_edt);
        EditText source_edt = dialog.findViewById(R.id.source_edt);
        TextView master_pin =dialog.findViewById(R.id.master_pin);
        TextView locat = dialog.findViewById(R.id.locat);
        Button btnSubmit     = dialog.findViewById(R.id.subbmit);

        switch (s){
            case DESTINATION:
                master_pin.setText(DESTINATION);
                locat.setVisibility(View.GONE);
                edtPin.setHint("Enter Your Destination");
                source_edt.setHint("Enter Your Source");
                locat.setVisibility(View.GONE);
                btnSubmit.setOnClickListener(view -> {
                    if (!edtPin.getText().toString().equals("")&& !source_edt.getText().toString().equals("")){
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + source_edt.getText().toString()+ "&daddr=" + edtPin.getText().toString()));
                        startActivity(intent);
                    }else
                    showToast("Kindly fill Details!");
                });
                break;
            case PINCODE:
                master_pin.setText(PINCODE);
                edtPin.setVisibility(View.GONE);
                source_edt.setVisibility(View.GONE);
                locat.setText("PIN Code : "+postalCode);
                btnSubmit.setText("Go");
                btnSubmit.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.streetview:cbll="+address));
                startActivity(intent);
                dialog.dismiss();
                });
                break;
            case LOCATION:
                edtPin.setVisibility(View.GONE);
                source_edt.setVisibility(View.GONE);
                master_pin.setText(LOCATION);
                locat.setText("Address : "+address);
                btnSubmit.setOnClickListener(v -> dialog.dismiss());
                break;
            case CITY:
                edtPin.setVisibility(View.GONE);
                source_edt.setVisibility(View.GONE);
                master_pin.setText(CITY);
                locat.setText("You Are in  "+city_l);
                btnSubmit.setOnClickListener(v -> dialog.dismiss());
                break;
        }
        dialog.show();

    }

    public void showToast(String toast){
        Toast.makeText(this,toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
