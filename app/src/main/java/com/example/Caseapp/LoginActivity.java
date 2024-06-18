package com.example.Caseapp;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Caseapp.databinding.ActivityLoginBinding;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding activityLoginBinding;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivity";
    private ProgressDialog loadingbar;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mauth;
    public String username, photourl;
    private DatabaseReference userref;
    private CallbackManager mcallbackmanager;
    private static final String TAGGED = "FacebookAuthentication";
    private AccessTokenTracker accessTokenTracker;
    // private LinearLayout fb;

    /* access modifiers changed from: protected */
//    @Test
//    @LargeTest
    @Override
    // androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());

        loadingbar = new ProgressDialog(this);
        mauth = FirebaseAuth.getInstance();
        mcallbackmanager = CallbackManager.Factory.create();
        userref = FirebaseDatabase.getInstance().getReference();

        SignInButton signInButton = (SignInButton) findViewById(R.id.google_login_button);
        signInButton.setSize(0);
        mGoogleSignInClient = GoogleSignIn.getClient((Activity) this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("314078118540-q60n0eplfjpkvuap37jtij8uhdiu781o.apps.googleusercontent.com").requestEmail().build());
        signInButton.setOnClickListener(new View.OnClickListener() {
            /* class com.hfad.travelx.LoginActivity.AnonymousClass1 */

            public void onClick(View view) {
                SignIn();
            }
        });

    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void SignIn() {
        startActivityForResult(this.mGoogleSignInClient.getSignInIntent(), 1);
    }

    @Override // androidx.fragment.app.FragmentActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            try {
                loadingbar.setTitle("Google Sign in");
                loadingbar.setMessage("Please wait a while! you are signing in through Google....");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            }
        }
        mcallbackmanager.onActivityResult(requestCode,resultCode,data);
    }

    private void firebaseAuthWithGoogle(String Idtoken, GoogleSignInAccount account) {
        mauth.signInWithCredential(GoogleAuthProvider.getCredential(Idtoken, null)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            /* class com.hfad.travelx.LoginActivity.AnonymousClass2 */

            @Override // com.google.android.gms.tasks.OnCompleteListener
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingbar.dismiss();
                    username = mauth.getCurrentUser().getDisplayName();
                    CheckUserNewOrOld();
                } else {
                    loadingbar.dismiss();
                    LoginActivity.this.sendusertologinactivity();
                    Toast.makeText(LoginActivity.this, "not authenticated try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity
    public void onStart() {
        super.onStart();
        if (mauth.getCurrentUser() != null) {

          userref.child("Users").child(mauth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        if(Objects.requireNonNull(snapshot.child("noofcases").getValue()).toString().equals("0")){
                            Intent mainintent = new Intent(LoginActivity.this, ChnageActiveTopicActivity.class);
                            mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainintent);
                            finish();
                        }else{
                            SendUserToMainActivity();
                        }
                    }else{
                        SendUserToRegisterActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            mGoogleSignInClient.signOut();
        }
    }

    public void CheckUserNewOrOld(){
      //  FirebaseUser user = mauth.getCurrentUser();
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(LoginActivity.this, "old user ...", Toast.LENGTH_LONG).show();
                if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if (Objects.requireNonNull(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("noofcases").getValue()).toString().equals("0")){
                        Intent mainintent = new Intent(LoginActivity.this, ChnageActiveTopicActivity.class);
                        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainintent);
                        finish();
                    }else{
                        SendUserToMainActivity();
                    }
                } else {
                    //Toast.makeText(LoginActivity.this, "new user ...", Toast.LENGTH_LONG).show();
                    SendUserToRegisterActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getDetails());
                Toast.makeText(LoginActivity.this, databaseError.toString(), Toast.LENGTH_LONG).show();
            }

        } ;
        userref.child("Users").addListenerForSingleValueEvent(eventListener);

    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(LoginActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent mainintent = new Intent(LoginActivity.this, RegisterActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
    private void sendusertologinactivity() {
        startActivity(new Intent(this, LoginActivity.class));}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingbar != null && loadingbar.isShowing()) {
            loadingbar.cancel();
        }
    }

}
