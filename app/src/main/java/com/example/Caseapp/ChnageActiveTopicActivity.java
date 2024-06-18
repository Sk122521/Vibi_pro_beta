package com.example.Caseapp;

import static com.example.Caseapp.utils.LocationUtils.checkLocationPermission;
import static com.example.Caseapp.utils.LocationUtils.getZipCode;
import static com.example.Caseapp.utils.LocationUtils.requestLocationPermission;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Caseapp.adapter.spinneradapter;
import com.example.Caseapp.databinding.ActivityChnageActiveTopicBinding;
import com.example.Caseapp.databinding.ActivityTopicsBinding;
import com.example.Caseapp.utils.GeocodingHelper;
import com.example.Caseapp.utils.OperatedXcode2;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChnageActiveTopicActivity extends AppCompatActivity {

    private ActivityChnageActiveTopicBinding binding;
    private DatabaseReference mRef, userref;
    private String catergoryNo, xcode1, xcode2, vcode5, category, status;
    private Uri imageUri = null;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private long totalTopics, deactivatedTopics;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude, longitude;

    private Context mContext;

    private ProgressDialog pd;
    private ArrayList<OperatedXcode2> userArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChnageActiveTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //  catergoryNo = getIntent().getStringExtra("categoriesNo").toString();
        mContext = this;

        CheckSpinners();

        userref = FirebaseDatabase.getInstance().getReference().child("Users");
        mRef = FirebaseDatabase.getInstance().getReference().child("categories");//.child(catergoryNo);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        binding.submittopicbtn.setOnClickListener(view -> checkLocationPermissions());
        binding.changeimagebtn.setOnClickListener(view -> openGallery());

        // changeNumberOfdeactiveTopic();
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                binding.selectedImage.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

//        mRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (snapshot.exists()){
//                    totalTopics = snapshot.child("topics").getChildrenCount();
//
//                    if(snapshot.child("nodeactivetopic").exists()){
//                        deactivatedTopics = Integer.parseInt(Objects.requireNonNull(snapshot.child("nodeactivetopic").getValue()).toString());
//                        binding.spinner.setSelection((int) deactivatedTopics);
//                    }else{
//                        binding.spinner.setSelection(0);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//
        initUsers();
        spinneradapter customAdapter = new spinneradapter(this, R.layout.spinner_item, userArrayList);
        binding.xcode2.setAdapter(customAdapter);

    }


    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    public void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Toast.makeText(this, "permission not given", Toast.LENGTH_SHORT).show();

        } else {
            checkTopicDetails();
        }
    }

    public void checkTopicDetails() {

        pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        if (!binding.topicDescription.getText().equals(null)
                && !binding.topicTitle.getText().equals(null)
                && !binding.topicLocation.getText().equals(null)
                && imageUri != null && CheckSpinners()) {

            if (getCaseLocation(binding.topicLocation.getText().toString())){
                StoreImagetoFirebase(pd);
            }


        } else {
            pd.dismiss();
            Toast.makeText(ChnageActiveTopicActivity.this, "All the fields must be fulfilled ", Toast.LENGTH_SHORT).show();
        }
    }

    public void StoreImagetoFirebase(ProgressDialog pd) {
        if (imageUri != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        // Get download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Store the URL in Firebase Realtime Database
                            String serialNumber = mRef.child("topics").push().getKey();

                            String imageUrl = uri.toString();
                            String title = binding.topicTitle.getText().toString();
                            String desc = binding.topicDescription.getText().toString();
                            long timestamp = System.currentTimeMillis();


                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("image", imageUrl);
                            childUpdates.put("title", title);
                            childUpdates.put("description", desc);
                            childUpdates.put("timestamp", String.valueOf(timestamp));
                            childUpdates.put("noofvotes", "0");
                            childUpdates.put("gd", "5000");
                            childUpdates.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            childUpdates.put("voteslocation", "0");
                            childUpdates.put("xcode1", xcode1);
                            childUpdates.put("xcode2", xcode2);
                            childUpdates.put("vcode5", vcode5);
                            childUpdates.put("category", category);
                            childUpdates.put("status", status);
                            childUpdates.put("latitude", Double.toString(latitude));
                            childUpdates.put("longitude", Double.toString(longitude));
                            childUpdates.put("key", serialNumber);


                            //    Toast.makeText(this, serialNumber, Toast.LENGTH_SHORT).show();
                            // String imageId = databaseReference.push().getKey(); // Generate unique key
                            assert serialNumber != null;


                            mRef.child(category).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    mRef.child(category).child("topics").child(serialNumber).setValue(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            userref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    int x = Integer.parseInt(snapshot.child("noofcases").getValue().toString());

                                                    userref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("noofcases").setValue(x + 1).addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    pd.dismiss();
                                                                    Toast.makeText(ChnageActiveTopicActivity.this, "Case added successfully", Toast.LENGTH_SHORT).show();

                                                                    Intent i = new Intent(ChnageActiveTopicActivity.this, MapsActivity.class);
                                                                    i.putExtra("categoryNo", category);
                                                                    startActivity(i);
                                                                }
                                                            }
                                                    ).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(ChnageActiveTopicActivity.this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    pd.dismiss();
                                                    Toast.makeText(ChnageActiveTopicActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            pd.dismiss();
                                            Toast.makeText(ChnageActiveTopicActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(ChnageActiveTopicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

//                                       // Toast.makeText(ChnageActiveTopicActivity.this, "snapshot does not exists", Toast.LENGTH_SHORT).show();
//                                        mRef.child("activetopic").setValue(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//
//                                                Toast.makeText(getApplicationContext(),"New active topic added", Toast.LENGTH_SHORT).show();
//                                                Intent intent = new Intent(ChnageActiveTopicActivity.this,TopicsActivity.class);
//                                                intent.putExtra("categoriesNo",catergoryNo);
//                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                startActivity(intent);
//                                                pd.dismiss();
//                                                Toast.makeText(ChnageActiveTopicActivity.this,"Active topic updated successfully",Toast.LENGTH_SHORT).show();
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                pd.dismiss();
//                                                Toast.makeText(ChnageActiveTopicActivity.this,"Error :"+ e.getMessage(),Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
                                }


                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

//                                    .addOnSuccessListener(aVoid ->)
//                                    .addOnFailureListener(e -> Toast.makeText(ChnageActiveTopicActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ChnageActiveTopicActivity.this, "Failed to upload topic: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public boolean CheckSpinners() {

        xcode1 = binding.xcode1.getSelectedItem().toString();
        // xcode2 =  binding.xcode2.getSelectedItem().toString();
        vcode5 = binding.vcode5.getSelectedItem().toString();
        category = binding.category.getSelectedItem().toString();
        status = binding.status.getSelectedItem().toString();

        binding.xcode2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Cast the selected view to LinearLayout since we know the structure

                // Find the TextView within the LinearLayout
                // Get the text from the TextView
                // Assuming you have the context available (e.g., within an Activity)
                String[] myStringArray = getResources().getStringArray(R.array.xcode2);

// Now you can access elements by index
                // String itemAtIndex2 =  // This will get "Item 3" since arrays are 0-based in Java

                xcode2 = myStringArray[position];

                // Toast.makeText(ChnageActiveTopicActivity.this,xcode2, Toast.LENGTH_SHORT).show();

                // Use the selectedText as needed
                // For example, display it in a TextView
//                 TextView resultTextView = findViewById(R.id.selected_text_view);
//                 resultTextView.setText(selectedText);
                // return null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return checkNonNull();


    }

    public boolean checkNonNull() {
        // Check if all variables are non-null
        if (xcode1 != null && xcode2 != null && vcode5 != null && category != null && status != null) {
            return true; // All variables are non-null
        } else {
            return false; // At least one variable is null
        }
    }

    @SuppressLint("MissingPermission")
    public Boolean getCaseLocation(String location) {
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
//                            StoreImagetoFirebase(pd);
//                            Toast.makeText(ChnageActiveTopicActivity.this,Double.toString(latitude)+" location .....", Toast.LENGTH_SHORT).show();
//                        } else {
//                            pd.dismiss();
//                            Toast.makeText(ChnageActiveTopicActivity.this, "Location is getting null", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
     //   Toast.makeText(this,Double.toString(latitude), Toast.LENGTH_SHORT).show();

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && addresses.size() >= 1) {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                Toast.makeText(this, Double.toString(latitude), Toast.LENGTH_SHORT).show();
                return true;
                // Use latitude and longitude as needed
            } else {
                Toast.makeText(this, "coordinates is null", Toast.LENGTH_SHORT).show();
                return false;
                // Handle invalid location name
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error :"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
          //  return false;
        }
    }




//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                if (ContextCompat.checkSelfPermission(this,
////                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
////                    if(getCaseLocation(binding.topicLocation.getText().toString())){
////                        StoreImagetoFirebase( pd);
////                    }else{
////                        pd.dismiss();
////                        Toast.makeText(this, "Entered more precise or full location details", Toast.LENGTH_SHORT).show();
////                    }
////                }
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your task
                checkTopicDetails();
            } else {
                // Permission denied, handle accordingly (show a message, request again, etc.)
                // For example:
                Toast.makeText(mContext, "Permissions are necessary", Toast.LENGTH_SHORT).show();
              //  showPermissionDeniedDialog();
            }
        }
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

}