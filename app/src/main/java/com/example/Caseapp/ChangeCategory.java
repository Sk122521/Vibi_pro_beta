package com.example.Caseapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.Caseapp.databinding.ActivityChangeCategoryBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ChangeCategory extends AppCompatActivity {

    private ActivityChangeCategoryBinding binding;
    private Uri imageUri = null;

    private ActivityResultLauncher<Intent> galleryLauncher;

    private long totalTopics, deactivatedTopics;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mRef;
    private String catergoryNo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        catergoryNo = getIntent().getStringExtra("categoriesNo").toString();

        binding.toolbarc.setTitle("Set Image for Category "+ catergoryNo);

        mRef = FirebaseDatabase.getInstance().getReference().child("categories").child(catergoryNo);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.submittopicbtn.setOnClickListener(view ->  checkTopicDetails());
        binding.changeimagebtn.setOnClickListener(view -> openGallery());

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

        if (imageUri != null && !binding.categoryHeader.getText().equals(null) && !binding.continent.getText().equals(null)){
            StoreImagetoFirebase( pd);
        }else{
            pd.dismiss();
            Toast.makeText(ChangeCategory.this, "All the fields must be fulfilled", Toast.LENGTH_SHORT).show();
        }
    }
    public void StoreImagetoFirebase(ProgressDialog pd){
        if (imageUri != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri ->{

                            String imageUrl = uri.toString();
                          //  String country = binding.topicTitle.getText().toString();

                            Map<String, Object> map  = new HashMap<>();
                            map.put("image",imageUrl);
//                            map.put("header",binding.categoryHeader.getText());
//                            map.put("continent", binding.continent.getText());
                          //  map.put("country",country);

                            mRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    pd.dismiss();
                                    Toast.makeText(ChangeCategory.this, "Image Updated successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ChangeCategory.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });


                        });
                    });


        }
    }
}

