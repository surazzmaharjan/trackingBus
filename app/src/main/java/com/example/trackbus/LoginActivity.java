package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.widget.NestedScrollView;

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.example.trackbus.service.BusTrackNotification;
import com.example.trackbus.service.WifiConnection;
import com.example.trackbus.validation.InputValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static NestedScrollView loginnestedScrollView;

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;

    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;

    private AppCompatButton appCompatButtonLogin;


    private AppCompatTextView textViewLinkRegister;
    private InputValidation inputValidation;
    private FirebaseAuth mAuth;
    private String email, password;
    DatabaseReference rootRef;
    DatabaseReference userRef;

    private Spinner loginspinner;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    NotificationManagerCompat notificationManagerCompat;
    WifiConnection wifiConnection = new WifiConnection();


    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiConnection,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(wifiConnection);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        notificationManagerCompat= NotificationManagerCompat.from(this);
        BusTrackNotification channel = new BusTrackNotification(this);
        channel.createNotificationChannel();


        loginnestedScrollView = findViewById(R.id.nestedinScrollView);

        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);

        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);

        appCompatButtonLogin = findViewById(R.id.appCompatButtonLogin);


        textViewLinkRegister = findViewById(R.id.textViewLinkRegister);
        textViewLinkRegister.setPaintFlags(textViewLinkRegister.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        loginspinner = findViewById(R.id.loginchooser_spinner);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if(nInfo != null && nInfo.isConnected()){

        }
        else {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(LoginActivity.this) ;
            a_builder.setMessage("Please enable internet connection !!!")
                    .setCancelable(false)
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                            Intent in = new Intent(Settings.ACTION_WIFI_SETTINGS  );
                            startActivity(in);

                        }
                    } )
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alert = a_builder.create();
            alert.setTitle("No Internet Connection");
            alert.show();
        }



        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Boolean ut = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.isDriver), false);
                    if (ut) {
                        displayNotification();
                        Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        displayNotification();
                        Intent intent = new Intent(LoginActivity.this, PassengerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

        };

        appCompatButtonLogin.setOnClickListener(this);
        textViewLinkRegister.setOnClickListener(this);
        inputValidation = new InputValidation(this);


         rootRef = FirebaseDatabase.getInstance().getReference();
         userRef = rootRef.child("users");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.appCompatButtonLogin:

                String email =textInputEditTextEmail.getText().toString();
                String password =textInputEditTextPassword.getText().toString();

                if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail,
                       "Enter a email")) {
                    return;
                }
                if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, "Ente a valid email")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, "Enter a password")) {
                    return;
                }

                if (loginspinner.getSelectedItem().toString().equals("Driver")) {
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.isDriver), true);
                    editor.commit();
                }else{
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.remove(getString(R.string.isDriver));
                    editor.commit();
                }



                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d("s", "signInWithEmail:success");


//
////                                    / Read from the database
//                                    userRef.addValueEventListener(new ValueEventListener() {
//                                        String usertype;
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            for (DataSnapshot keyId: dataSnapshot.getChildren()) {
//                                                String emails =textInputEditTextEmail.getText().toString();
//                                                String user_type = loginspinner.getSelectedItem().toString();
//
//                                                if (keyId.child("email").getValue().equals(emails)) {
//                                                    usertype = keyId.child("usertype").getValue(String.class);
//
//                                                            if(user_type.equals(usertype)){
//                                                                FirebaseUser user = mAuth.getCurrentUser();
//                                                                updateUI(user);
//
//                                                            }
//                                                            else{
//                                                                Snackbar.make(loginnestedScrollView, "User type does not match.", Snackbar.LENGTH_LONG).show();
//
//                                                            }
//                                                }
//                                            }
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError error) {
//                                            // Failed to read value
//                                            Log.d("d", "Failed to read value.", error.toException());
//                                        }
//                                    });

//                                }
                                if (!task.isSuccessful()){
                                    // If sign in fails, display a message to the user.
                                    Log.w("s", "signInWithEmail:failure", task.getException());
//                                    Snackbar.make(loginnestedScrollView, "Authentication failed.", Snackbar.LENGTH_LONG).show();
                                    Snackbar.make(loginnestedScrollView, "Authentication failed.", Snackbar.LENGTH_LONG).show();

                                }

                                // ...
                            }
                        });

                break;
            case R.id.textViewLinkRegister:
                // Navigate to Signup Activity
                Intent intentRegister = new Intent(this, RegisterActivity.class);
                startActivity(intentRegister);
                break;
        }
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            updateUI(currentUser);
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            updateUI(currentUser);
//        }
//    }

//    public void updateUI(FirebaseUser currentUser) {
//        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
//        mainIntent.putExtra("email", currentUser.getEmail());
//        Log.v("DATA", currentUser.getUid());
//        startActivity(mainIntent);
//    }

    /**
     * Notification for login
     */

    public void displayNotification(){
        Notification notification= new NotificationCompat
                .Builder(this,BusTrackNotification.Login_Channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Login")
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentText("Successfully login to dashboard")
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        notificationManagerCompat.notify(1,notification);
    }

}