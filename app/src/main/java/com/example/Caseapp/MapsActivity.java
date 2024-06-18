package com.example.Caseapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.Caseapp.adapter.spinneradapter;
import com.example.Caseapp.databinding.ActivityMapBinding;
import com.example.Caseapp.model.resultModel;
import com.example.Caseapp.utils.OperatedXcode2;
import com.example.Caseapp.utils.ResultUtils;
import com.example.Caseapp.utils.getLatLngBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.widget.SearchView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{

    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context mContext;
    private DatabaseReference ActiveTopicRef,mRef,dref;


    private String categoryNo,gd,no_of_votes,continent,xcode2select;

    private  int pos;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker searchMarker;

    private resultModel resultModel;
    private ArrayList<OperatedXcode2> userArrayList = new ArrayList<>();

    private HashMap<String, Object> dataMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
     //   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        setSupportActionBar(binding.toolbar);
        setUpDrawer();




        mContext = this;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        categoryNo = getIntent().getStringExtra("categoryNo");
        continent = categoryNo;

        mRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryNo);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query,null,"n");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchLocation(newText,null,"n");
                return false;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        initUsers();
        spinneradapter customAdapter = new spinneradapter(this,R.layout.spinner_item, userArrayList);
        binding.spinner2.setAdapter(customAdapter);

    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for permission to access location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            zoomToCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        getSpinner2SelectedValue();
        filterCasesBasedOnSpinnerSelection();

        // Add marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                Intent i  = new Intent(MapsActivity.this, OldTopicActivity.class);
                i.putExtra("category", categoryNo);
                i.putExtra("topicKey",marker.getTag().toString());
                startActivity(i);

               // float currentZoomLevel = googleMap.getCameraPosition().zoom;

//                if (currentZoomLevel < 5){
//                    showresult();
//                }else{
//                    getDataPoints(LocationUtils.getZipCode(marker.getPosition().latitude,marker.getPosition().longitude,getApplicationContext()));
//                }
              //  showMarkerDialog(marker.getPosition());
                return false;
            }
        });

        binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSpinner2SelectedValue();
                int selectedIndex = binding.spinner2.getSelectedItemPosition();
                if (selectedIndex != AdapterView.INVALID_POSITION) {
                    mMap.clear();
                    filterCasesBasedOnSpinnerSelection();
                } else {
                    Toast.makeText(MapsActivity.this, "Select option from xcode2", Toast.LENGTH_SHORT).show();
                }
               // return null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MapsActivity.this, "Please select xcode1 value from items", Toast.LENGTH_SHORT).show();
            }
        });

        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] myStringArray = getResources().getStringArray(R.array.xcode2);
                xcode2select = myStringArray[position];
                pos = position;

              //  Toast.makeText(MapsActivity.this,xcode2select+" true",Toast.LENGTH_SHORT).show();

                int selectedIndex = binding.spinner1.getSelectedItemPosition();
                if (selectedIndex != AdapterView.INVALID_POSITION) {
                    mMap.clear();
                    filterCasesBasedOnSpinnerSelection();
                } else {
                    Toast.makeText(MapsActivity.this, "Select option from xcode1", Toast.LENGTH_SHORT).show();
                }

               // return null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MapsActivity.this, "Please select xcode2 value from items", Toast.LENGTH_SHORT).show();
            }
        });

       // filterCasesBasedOnSpinnerSelection();

    }

    private void filterCasesBasedOnSpinnerSelection(){
        mRef.child("topics").addValueEventListener(new ValueEventListener() {
            //  orderByChild("status").equalTo("active")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //continent = snapshot.child("continent").getValue().toString();
            //    zoomToCurrentLocation();
                for (DataSnapshot snap : snapshot.getChildren()){
                   //Toast.makeText(MapsActivity.this,xcode2select, Toast.LENGTH_SHORT).show();
                    if (Objects.requireNonNull(snap.child("xcode1").getValue()).toString().equals(binding.spinner1.getSelectedItem().toString()) && Objects.requireNonNull(snap.child("xcode2").getValue()).toString().equals(xcode2select)){
                        String vcode =  snap.child("vcode5").getValue().toString();
                        String caseUid =  snap.child("uid").getValue().toString();
                        String lat =  snap.child("latitude").getValue().toString();
                        String lon = snap.child("longitude").getValue().toString();//
                        String status = snap.child("status").getValue().toString();
                        String Casekey =  snap.getKey();
                       // Toast.makeText(MapsActivity.this,"showing",Toast.LENGTH_SHORT).show();
                        if (status.equals("active")){
                            showMap(vcode,caseUid,lat,lon,Casekey);
                        }
                       // Toast.makeText(MapsActivity.this, "Jai Shree Ram", Toast.LENGTH_SHORT).show();
                    }


//                        for (DataSnapshot s : snap.child("voteslocation").getChildren()){
//                            String zipcode =  s.getKey();
//                            showPercentage(zipcode);
//                        }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSpinner2SelectedValue(){


        String[] myStringArray = getResources().getStringArray(R.array.xcode2);
        xcode2select = myStringArray[binding.spinner2.getSelectedItemPosition()];
        pos  = binding.spinner2.getSelectedItemPosition();
       // Toast.makeText(MapsActivity.this,xcode2select+"   yes",Toast.LENGTH_SHORT).show();

    }


    private void searchLocation(String locationName,Float percentage, String s) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // Zoom to searched location
                if (s.equals("s")){
                    View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
                    TextView markerText = customMarkerView.findViewById(R.id.markerText);

                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    String PercentValue = decimalFormat.format(percentage);


                    markerText.setText(PercentValue+ " %");

                    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

                    customMarkerView.setDrawingCacheEnabled(true);
                    customMarkerView.buildDrawingCache();
                    Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getDrawingCache());
                    customMarkerView.setDrawingCacheEnabled(false);

                    BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(bitmap);

                    searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(PercentValue+" %").icon(customMarker));
                }else{
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
                    searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showMap(String Vcode,String caseUid, String lat,String lon, String CaseKey ){
        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
        TextView markerText = customMarkerView.findViewById(R.id.markerText);
        CircleImageView i =  customMarkerView.findViewById(R.id.xcode_img);

//        DecimalFormat decimalFormat = new DecimalFormat("#.##");
//        String PercentValue = decimalFormat.format(percentage);


        markerText.setText(Vcode);
        i.setImageResource(setImage());

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());

        customMarkerView.setDrawingCacheEnabled(true);
        customMarkerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(customMarkerView.getDrawingCache());
        customMarkerView.setDrawingCacheEnabled(false);

        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(bitmap);

        searchMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon))).icon(customMarker));
        searchMarker.setTag(CaseKey); // You can set any object as the tag, such as a String or a custom object

//        searchMarker.setTag(Vcode);
//        String dataId = UUID.randomUUID().toString()
//        dataMap = new HashMap<>();
//        dataMap.put(Vcode, CaseKey);
//        if (from.equals("deactive")){
//
//            dref =  mRef.child("topics").child(topicno);
//            dref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snap) {
//                    String zipcodevotes, GdZipcodeWise;
//
//                    if (snap.child("voteslocation").child(zipcode).exists()){
//                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
//                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
//                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
//                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
//                        }
//                    }else{
//                        zipcodevotes = "0";
//                        GdZipcodeWise  = Integer.toString(2000);
//                    }
//                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
//                    resultModel = resultUtils.getResult();
//                    searchLocation(zipcode,resultModel.getAverage_percent(),"s");
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//
//        }else{
//            dref =  mRef.child("activetopic");
//            dref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snap) {
//                    String zipcodevotes, GdZipcodeWise;
//
//                    if (snap.child("voteslocation").child(zipcode).exists()){
//                        zipcodevotes = snap.child("voteslocation").child(zipcode).child("noofvotes").getValue().toString();
//                        GdZipcodeWise = snap.child("voteslocation").child(zipcode).child("gdzipcodewise").getValue().toString();
//                        if (((Integer.parseInt(zipcodevotes)+1)*100 )< (0.8 * Integer.parseInt(GdZipcodeWise))){
//                            GdZipcodeWise  = Integer.toString(Integer.parseInt(GdZipcodeWise)+1000);
//                        }
//                    }else{
//                        zipcodevotes = "0";
//                        GdZipcodeWise  = Integer.toString(2000);
//                    }
//                    ResultUtils resultUtils = new ResultUtils(Integer.parseInt(zipcodevotes),Integer.parseInt(GdZipcodeWise));
//                    resultModel = resultUtils.getResult();
//                    searchLocation(zipcode,resultModel.getAverage_percent(),"s");
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    zoomToCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void zoomToCurrentLocation() {
//        if (continent.equals("Asia")){
//            zoom(new getLatLngBounds().AsiaBounds());
//        }else if(continent.equals("Europe")){
//            zoom(new getLatLngBounds().EuropeBounds());
//        }else if(continent.equals("Africa")){
//            zoom(new getLatLngBounds().AfricaBounds());
//        }else if(continent.equals("America")){
//            zoom(new getLatLngBounds().NorthAmericaBounds());
//        }else if(continent.equals("Australia")){
//            zoom(new getLatLngBounds().AustraliaBounds());
//        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            //Toast.makeText(getApplicationContext(), "location...", Toast.LENGTH_SHORT).show();
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            //mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    }
                });
    }

    public void zoom(LatLngBounds bounds){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 0));
       // mMap.setLatLngBoundsForCameraTarget(bounds);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

//        LatLng newDelhi = new LatLng(28.6139, 77.2090); // Coordinates for New Delhi
//        mMap.addMarker(new MarkerOptions().position(newDelhi).title("India"));
    }
    public void  setUpDrawer(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.open_nav,
                R.string.close_nav
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_show_case) {
                handleShowCase();
                return true;
            } else if (itemId == R.id.nav_create_case) {
                handleCreateCase();
                return true;
            }else if (itemId == R.id.delete_profile) {
                deleteProfile();
                return true;
            }else if (itemId == R.id.logout_profile) {
                Logout();
                return true;
            }
            return false;
        });
    }

    public void deleteProfile(){
        ProgressDialog pd  =  new ProgressDialog(this);
        pd.setMessage("Deleting account");
        pd.show();
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(MapsActivity.this, "Account removed successfully", Toast.LENGTH_SHORT).show();
               // FirebaseAuth.getInstance().getCurrentUser().delete();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

//                FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        pd.dismiss();
//                        Toast.makeText(MapsActivity.this, "Account removed successfully", Toast.LENGTH_SHORT).show();
//                       // FirebaseAuth.getInstance().signOut();
//                        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        finish();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        pd.dismiss();
//                        Toast.makeText(MapsActivity.this, "Failed to delete account : "+ e.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(MapsActivity.this, "Failed to delete account : "+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public void Logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            binding.drawerLayout.openDrawer(binding.navView);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void handleShowCase() {
        // Toast.makeText(this, "Show Case clicked", Toast.LENGTH_SHORT).show();
        Intent i  =  new Intent(this, TopicsActivity.class);
        i.putExtra("categoriesNo",categoryNo);
        startActivity(i);
        // Add your logic for handling "Show Case" action here
      //  Intent i  =  new Intent(this, ListYourCases.class);
      //  startActivity(i);
    }
    private void handleCreateCase() {

        Intent i  =  new Intent(this, ChnageActiveTopicActivity.class);
        startActivity(i);
      //  Toast.makeText(this, "Create Case clicked", Toast.LENGTH_SHORT).show();
        // Add your logic for handling "Create Case" action here
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMap.clear();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.clear();
    }
    public void initUsers()
    {
        OperatedXcode2 user1 = new OperatedXcode2("Xzone2standardstats", 0);
        userArrayList.add(user1);

        OperatedXcode2 user2 = new OperatedXcode2("Xzone2.0standardstats", 1);
        userArrayList.add(user2);

        OperatedXcode2 user3 = new OperatedXcode2("Xzone2.1standardstats", 2);
        userArrayList.add(user3);

        OperatedXcode2 user4 = new OperatedXcode2("Xzone2.2standardstats", 3);
        userArrayList.add(user4);

        OperatedXcode2 user5 = new OperatedXcode2("Xzone2.3standardstats", 4);
        userArrayList.add(user5);

    }
    public int setImage(){
        switch (pos)
        {
            case 0:
                return R.mipmap.xcode2_0;
            case 1:
                return R.mipmap.xcode2_1;
            case 2:
                return R.mipmap.xcode2_2;
            case 3:
                return R.mipmap.xcode2_3;
            case 4:
                return R.mipmap.xcode2_4;
        }
        return R.mipmap.xcode2_0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSpinner2SelectedValue();
        filterCasesBasedOnSpinnerSelection();
     //   Toast.makeText(this, "resumed", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        getSpinner2SelectedValue();
//        filterCasesBasedOnSpinnerSelection();
//        Toast.makeText(this, "restore", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        getSpinner2SelectedValue();
//        filterCasesBasedOnSpinnerSelection();
//        Toast.makeText(this, "restartr", Toast.LENGTH_SHORT).show();
//    }
}

