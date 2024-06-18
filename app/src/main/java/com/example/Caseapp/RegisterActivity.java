package com.example.Caseapp;


import static com.example.Caseapp.utils.LocationUtils.checkLocationPermission;
import static com.example.Caseapp.utils.LocationUtils.getZipCode;
import static com.example.Caseapp.utils.LocationUtils.requestLocationPermission;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.Caseapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 111;
    private ActivityRegisterBinding binding;
    private String phone;
    private FirebaseAuth mauth;
    private DatabaseReference dref;
    private ProgressDialog pd;
    private double longitude, latitude;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mauth = FirebaseAuth.getInstance();
        dref = FirebaseDatabase.getInstance().getReference("Users");
        pd = new ProgressDialog(this);
        pd.setMessage("Saving Your Information....");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        binding.nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                if (!binding.NameEdtxt.getText().toString().equals("") && !binding.phnEdtxt.getText().toString().equals("")) {

                    if (isValidPhoneNumber(binding.phnEdtxt.getText().toString())){
                       checkLocationPermissions();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Invalid phone Number Format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "Fill up all Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void checkLocationPermissions (){
        if (checkLocationPermission(RegisterActivity.this)) {
            getLastLocation();
        } else {
            // Permission has not been granted yet, request it
            requestLocationPermission(RegisterActivity.this);
        }

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission(RegisterActivity.this);
        }else{
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                              latitude = location.getLatitude();
                              longitude = location.getLongitude();
                            String zipcode = getZipCode(latitude, longitude,this);
                            if (zipcode != null){
                                Toast.makeText(this, zipcode, Toast.LENGTH_SHORT).show();
                               SaveData();
                            }else{
                                pd.dismiss();
                                Toast.makeText(this, "Please wait , Currently Could not get Location ", Toast.LENGTH_SHORT).show();
                            }
                            //   Toast.makeText(TopicsActivity.this, "Latitude: " + latitude + "\nLongitude: " + longitude + "\nZipcode: " + zipcode, Toast.LENGTH_LONG).show();
                        } else {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Location is null , Please click on device location button", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(RegisterActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, proceed with your logic
                // For example, start location updates or show the user's location on a map
                // startLocationUpdates();
                getLastLocation();
            } else {
//                requestLocationPermission(TopicsActivity.this);
                // Location permission denied, inform the user accordingly
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Denied");
        builder.setMessage("Please grant location permission in settings to use this feature.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Request location permission again
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

        public static boolean isValidPhoneNumber(String phoneNumber) {
            // Define a regex pattern for a valid phone number format
            String phonePattern = "^[+]?[0-9]{10,13}$";

            // Check if the input phone number matches the pattern
            return phoneNumber != null && phoneNumber.matches(phonePattern);
        }



    private void SaveData() {

        HashMap<String, String> usermap = new HashMap<>();
        usermap.put("name", binding.NameEdtxt.getText().toString());
        usermap.put("phone", binding.phnEdtxt.getText().toString());
        usermap.put("uid", mauth.getCurrentUser().getUid());
        usermap.put("latitude",Double.toString(latitude));
        usermap.put("longitude",Double.toString(longitude));
        usermap.put("noofcases","0");


//        Toast.makeText(this, mauth.getCurrentUser().getPhoneNumber(), Toast.LENGTH_SHORT).show();
        dref.child(mauth.getCurrentUser().getUid()).setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "Welcome...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, ChnageActiveTopicActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                pd.dismiss();
            }
        });
        //usermap.put("password",binding.passEdtxt.getText().toString());
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
//            if (grantResults[0] == PERMISSION_GRANTED) {
//                getCurrentLocation();
//            } else {
//                Toast.makeText(this, "permission_denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void getCurrentLocation() {
//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setInterval(1000);
//        locationRequest.setFastestInterval(3000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//            LocationServices.getFusedLocationProviderClient(RegisterActivity.this).requestLocationUpdates(locationRequest,
//                    new LocationCallback() {
//                        @Override
//                        public void onLocationResult(LocationResult locationResult) {
//                            super.onLocationResult(locationResult);
//                            LocationServices.getFusedLocationProviderClient(RegisterActivity.this).removeLocationUpdates(this);
//                            if (locationResult != null && locationResult.getLocations().size() > 0) {
//                                int latestlocationindex = locationResult.getLocations().size() - 1;
//                                  latitude = locationResult.getLocations().get(latestlocationindex).getLatitude();
//                                  longitude = locationResult.getLocations().get(latestlocationindex).getLongitude();
//                                Toast.makeText(RegisterActivity.this, latitude + " " + longitude, Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                    }, Looper.myLooper());
//
//            return;
//        }
//    }
}