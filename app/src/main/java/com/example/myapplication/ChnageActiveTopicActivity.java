package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivityChnageActiveTopicBinding;
import com.example.myapplication.databinding.ActivityTopicsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChnageActiveTopicActivity extends AppCompatActivity {

    private ActivityChnageActiveTopicBinding binding;
    private DatabaseReference mRef;
    private String catergoryNo ;
    private Uri imageUri = null;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private long totalTopics, deactivatedTopics;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChnageActiveTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        catergoryNo = getIntent().getStringExtra("categoriesNo").toString();
        mRef = FirebaseDatabase.getInstance().getReference().child("categories").child(catergoryNo);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        binding.submittopicbtn.setOnClickListener(view ->  checkTopicDetails());
        binding.changeimagebtn.setOnClickListener(view -> openGallery());
        binding.changeNodeactivetopicBtn.setOnClickListener(view -> changeNumberOfdeactiveTopic());

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

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    totalTopics = snapshot.child("topics").getChildrenCount();

                    if(snapshot.child("nodeactivetopic").exists()){
                        deactivatedTopics = Integer.parseInt(Objects.requireNonNull(snapshot.child("nodeactivetopic").getValue()).toString());
                        binding.spinner.setSelection((int) deactivatedTopics);
                    }else{
                        binding.spinner.setSelection(0);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    public void checkTopicDetails(){

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        if (!binding.topicDescription.getText().equals(null)
                && !binding.topicTitle.getText().equals(null)
                && imageUri != null){
            StoreImagetoFirebase( pd);
        }else{
            pd.dismiss();
            Toast.makeText(ChnageActiveTopicActivity.this, "All the fields must be fulfilled", Toast.LENGTH_SHORT).show();
        }
    }

    public void StoreImagetoFirebase(ProgressDialog pd){
        if (imageUri != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully
                        // Get download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Store the URL in Firebase Realtime Database
                            String serialNumber =mRef.child("topics").push().getKey();

                            String imageUrl = uri.toString();
                            String title  = binding.topicTitle.getText().toString();
                            String desc = binding.topicDescription.getText().toString();
                            long timestamp = System.currentTimeMillis();


                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("image", imageUrl);
                            childUpdates.put("title",title );
                            childUpdates.put("description",desc);
                            childUpdates.put("timestamp",String.valueOf(timestamp));
                            childUpdates.put("noofvotes","0");
                            childUpdates.put("gd","5000");
                            childUpdates.put("voteslocation", "0");


                            Toast.makeText(this, serialNumber, Toast.LENGTH_SHORT).show();
                           // String imageId = databaseReference.push().getKey(); // Generate unique key
                            assert serialNumber != null;


                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.child("activetopic").exists()){
                                        Toast.makeText(ChnageActiveTopicActivity.this, "snapshot existed", Toast.LENGTH_SHORT).show();

                                        mRef.child("topics").child(serialNumber).setValue(snapshot.child("activetopic").getValue()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                mRef.child("activetopic").updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getApplicationContext(),"New active topic added", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(ChnageActiveTopicActivity.this,TopicsActivity.class);
                                                        intent.putExtra("categoriesNo",catergoryNo);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        pd.dismiss();
                                                        Toast.makeText(ChnageActiveTopicActivity.this,"Active topic updated successfully",Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(ChnageActiveTopicActivity.this,"Error :"+ e.getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnCanceledListener(new OnCanceledListener() {
                                            @Override
                                            public void onCanceled() {
                                                pd.dismiss();
                                                Toast.makeText(ChnageActiveTopicActivity.this,"Error" , Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(ChnageActiveTopicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else{
                                       // Toast.makeText(ChnageActiveTopicActivity.this, "snapshot does not exists", Toast.LENGTH_SHORT).show();
                                        mRef.child("activetopic").setValue(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Toast.makeText(getApplicationContext(),"New active topic added", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ChnageActiveTopicActivity.this,TopicsActivity.class);
                                                intent.putExtra("categoriesNo",catergoryNo);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                pd.dismiss();
                                                Toast.makeText(ChnageActiveTopicActivity.this,"Active topic updated successfully",Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(ChnageActiveTopicActivity.this,"Error :"+ e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
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

    public void changeNumberOfdeactiveTopic(){

                ProgressDialog pd = new ProgressDialog(ChnageActiveTopicActivity.this);
                pd.setMessage("Updating");
                pd.setCanceledOnTouchOutside(false);
                pd.show();

        String selectedValueString =  binding.spinner.getSelectedItem().toString();

        int selectedValue = Integer.parseInt(selectedValueString);

        if (selectedValue <= (int)totalTopics &&  totalTopics != 0){
            Map<String,Object> map = new HashMap();
            map.put("nodeactivetopic", Integer.toString(selectedValue));
            mRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    pd.dismiss();
                  //  Toast.makeText(getApplicationContext(),"New active topic added", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChnageActiveTopicActivity.this,TopicsActivity.class);
                    intent.putExtra("categoriesNo",catergoryNo);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(ChnageActiveTopicActivity.this, "Total Number of deactivated topic updated successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(ChnageActiveTopicActivity.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            pd.dismiss();
            Toast.makeText(ChnageActiveTopicActivity.this, "There are only "+Integer.toString((int)totalTopics)+ " deactivated topics in your database. ", Toast.LENGTH_SHORT).show();
        }

    }

}