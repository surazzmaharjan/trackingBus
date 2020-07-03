package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.widget.NestedScrollView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackbus.model.User;
import com.example.trackbus.service.BusTrackNotification;
import com.example.trackbus.validation.InputValidation;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {



    private NestedScrollView nestedScrollViewprofile;

    private TextInputLayout textInputLayoutFullNameupate;
    private TextInputLayout textInputLayoutProfessionUpdate;
    private TextInputLayout textInputLayoutWorkPlaceUpdate;
    private TextInputLayout textInputLayoutPhoneUpdate;

    private TextInputEditText textInputEditTextFullNameUpdate;
    private TextInputEditText textInputEditTextprofessionUpdate;
    private TextInputEditText textInputEditTextPlaceUpdate;
    private TextInputEditText textInputEditTextPhoneUpdate;

    private TextView textViewemail;
    private TextView textViewusertype;


    private AppCompatButton appCompatButtonUpdate,appCompatButtonBack;
    private InputValidation inputValidation;

    String _fullname,_workplace,_profession,_phone;

    private FirebaseAuth mAuth;
    private DatabaseReference  databaseReference;
    NotificationManagerCompat notificationManagerCompat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar =(Toolbar)findViewById(R.id.protoolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ProfileActivity.this, DriverActivity.class);
                startActivity(mainIntent);
            }
        });


        notificationManagerCompat= NotificationManagerCompat.from(this);
        BusTrackNotification channel = new BusTrackNotification(this);
        channel.createNotificationChannel();


        nestedScrollViewprofile = findViewById(R.id.nestedupdateScrollView);

        textInputLayoutFullNameupate =findViewById(R.id.text_input_layout_updatefullname);
        textInputLayoutProfessionUpdate =findViewById(R.id.text_input_layout_professionupdate);
        textInputLayoutWorkPlaceUpdate = findViewById(R.id.text_input_layout_workplaceupdate);
        textInputLayoutPhoneUpdate = findViewById(R.id.text_input_layout_phoneupdate);

        textInputEditTextFullNameUpdate = findViewById(R.id.updatefullname_edittext);
        textInputEditTextprofessionUpdate = findViewById(R.id.updateprofession_edittext);
        textInputEditTextPlaceUpdate =findViewById(R.id.updateworkplace_edittext);
        textInputEditTextPhoneUpdate =findViewById(R.id.updatephone_edittext);


        textViewemail = findViewById(R.id.updatemail);


        appCompatButtonUpdate = findViewById(R.id.updatebutton);
        appCompatButtonBack = findViewById(R.id.appCompatbackbutton);

        inputValidation = new InputValidation(this);

        appCompatButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textInputEditTextFullNameUpdate.getText().toString() != null && textInputEditTextprofessionUpdate.getText().toString() != null) {

                    if (!inputValidation.isInputEditTextFilled(textInputEditTextFullNameUpdate, textInputLayoutFullNameupate, "Enter a fullname")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextFilled(textInputEditTextprofessionUpdate, textInputLayoutProfessionUpdate, "Enter a profession")) {
                        return;
                    }


                    if (!inputValidation.isInputEditTextFilled(textInputEditTextPlaceUpdate, textInputLayoutWorkPlaceUpdate, "Enter a work place")) {
                        return;
                    }

                    if (!inputValidation.isInputEditTextFilled(textInputEditTextPhoneUpdate, textInputLayoutPhoneUpdate,"Enter a phone number")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextNumber(textInputEditTextPhoneUpdate, textInputLayoutPhoneUpdate, "Enter a valid phone number")) {
                        return;
                    }

                }
                userInformation();
            }
        });

                appCompatButtonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mainIntent = new Intent(ProfileActivity.this, DriverActivity.class);
                        startActivity(mainIntent);
                    }
                });



        mAuth = FirebaseAuth.getInstance();
        databaseReference =  FirebaseDatabase.getInstance().getReference().child("Users_Detail");


        Intent sintent = getIntent();
        final String semail = sintent.getStringExtra("email");

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = rootRef.child("Users_Detail");

        // Read from the database
        userRef.addValueEventListener(new ValueEventListener() {
            String fname, cemail, profession, workplace, phone;

            FirebaseUser fireusers = mAuth.getCurrentUser();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot keyId: dataSnapshot.getChildren()) {
                    if (keyId.child("email").getValue().equals(fireusers.getEmail())) {
                        fname = keyId.child("fullName").getValue(String.class);
                        profession = keyId.child("profession").getValue(String.class);
                        workplace = keyId.child("workplace").getValue(String.class);
                        phone = keyId.child("phone").getValue(String.class);
                        cemail = keyId.child("email").getValue(String.class);
                        break;
                    }
                }
                textInputEditTextFullNameUpdate.setText(fname);
                textInputEditTextprofessionUpdate.setText(profession);
                textInputEditTextPlaceUpdate.setText(workplace);
                textInputEditTextPhoneUpdate.setText(phone);
                textViewemail.setText(" "+cemail);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("s", "Failed to read value.", error.toException());
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){

            case R.id.actionlogout:
                displayNotification();
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.remove(getString(R.string.isDriver));
                editor.commit();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();

                break;

        }

        return super.onOptionsItemSelected(item);
    }



    private void userInformation(){


                String fulname = textInputEditTextFullNameUpdate.getText().toString().trim();
                String prof = textInputEditTextprofessionUpdate.getText().toString().trim();
                String work = textInputEditTextPlaceUpdate.getText().toString().trim();
                String phoneno = textInputEditTextPhoneUpdate.getText().toString().trim();

                FirebaseUser fireuser = mAuth.getCurrentUser();

                User userinformation = new User(fulname,
                        textViewemail.getText().toString().trim(),prof,work,phoneno,fireuser.getUid()
                );

                databaseReference.child(fireuser.getUid()).setValue(userinformation);

//                Toast.makeText(getApplicationContext(),"User information updated",Toast.LENGTH_LONG).show();
                Snackbar.make(nestedScrollViewprofile,"User information updated",Snackbar.LENGTH_LONG).show();





    }

    public void displayNotification(){
        Notification notification= new NotificationCompat
                .Builder(this,BusTrackNotification.Logout_Channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Logout")
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentText("Successfully logged out")
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        notificationManagerCompat.notify(1,notification);
    }

}