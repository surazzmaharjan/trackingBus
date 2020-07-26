package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.widget.NestedScrollView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackbus.model.User;
import com.example.trackbus.service.BusTrackNotification;
import com.example.trackbus.validation.InputValidation;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PassengerProfileActivity extends AppCompatActivity {



    private NestedScrollView nestedScrollViewprofilepassenger;

    private TextInputLayout textInputLayoutFullNameupatepassenger;
    private TextInputLayout textInputLayoutProfessionUpdatepassenger;
    private TextInputLayout textInputLayoutWorkPlaceUpdatepassenger;
    private TextInputLayout textInputLayoutPhoneUpdatepassenger;

    private TextInputEditText textInputEditTextFullNameUpdatepassenger;
    private TextInputEditText textInputEditTextprofessionUpdatepassenger;
    private TextInputEditText textInputEditTextPlaceUpdatepassenger;
    private TextInputEditText textInputEditTextPhoneUpdatepassenger;

    private TextView textViewemailpassenger;
    private ImageView profileimageView;



    private AppCompatButton appCompatButtonUpdatepassenger,appCompatButtonBackpassenger;
    private InputValidation inputValidation;

    String _fullname,_workplace,_profession,_phone;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    NotificationManagerCompat notificationManagerCompat;

    private final Integer PICK_IMAGE_REQUEST=1;
    Bitmap bitmap;
    Uri uri;
    FirebaseUser fireuser;
    String purl,phUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_profile);
        Toolbar toolbar =(Toolbar)findViewById(R.id.protoolbarpassenger);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(PassengerProfileActivity.this, PassengerActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });



        notificationManagerCompat= NotificationManagerCompat.from(this);
        BusTrackNotification channel = new BusTrackNotification(this);
        channel.createNotificationChannel();

        
        nestedScrollViewprofilepassenger = findViewById(R.id.passengernestedupdateScrollView);

        textInputLayoutFullNameupatepassenger =findViewById(R.id.text_input_layout_updatefullnamepassenger);
        textInputLayoutProfessionUpdatepassenger =findViewById(R.id.text_input_layout_professionupdatepassenger);
        textInputLayoutWorkPlaceUpdatepassenger = findViewById(R.id.text_input_layout_workplaceupdatepassenger);
        textInputLayoutPhoneUpdatepassenger = findViewById(R.id.text_input_layout_phoneupdatepassenger);

        textInputEditTextFullNameUpdatepassenger = findViewById(R.id.updatefullname_edittextpassenger);
        textInputEditTextprofessionUpdatepassenger = findViewById(R.id.updateprofession_edittextpassenger);
        textInputEditTextPlaceUpdatepassenger =findViewById(R.id.updateworkplace_edittextpassenger);
        textInputEditTextPhoneUpdatepassenger =findViewById(R.id.updatephone_edittextpassenger);


        textViewemailpassenger = findViewById(R.id.passengerupdatemail);
        profileimageView = findViewById(R.id.profilepicture);


        appCompatButtonUpdatepassenger = findViewById(R.id.updatebuttonpassenger);
        appCompatButtonBackpassenger = findViewById(R.id.appCompatbackbuttonpassenger);

        inputValidation = new InputValidation(this);


        profileimageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });
        appCompatButtonUpdatepassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textInputEditTextFullNameUpdatepassenger.getText().toString() != null && textInputEditTextprofessionUpdatepassenger.getText().toString() != null) {

                    if (!inputValidation.isInputEditTextFilled(textInputEditTextFullNameUpdatepassenger, textInputLayoutFullNameupatepassenger, "Enter a fullname")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextFilled(textInputEditTextprofessionUpdatepassenger, textInputLayoutProfessionUpdatepassenger, "Enter a profession")) {
                        return;
                    }


                    if (!inputValidation.isInputEditTextFilled(textInputEditTextPlaceUpdatepassenger, textInputLayoutWorkPlaceUpdatepassenger, "Enter a work place")) {
                        return;
                    }

                    if (!inputValidation.isInputEditTextFilled(textInputEditTextPhoneUpdatepassenger, textInputLayoutPhoneUpdatepassenger,"Enter a phone number")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextNumber(textInputEditTextPhoneUpdatepassenger, textInputLayoutPhoneUpdatepassenger, "Enter a valid phone number")) {
                        return;
                    }

                }
                userInformation();
            }
        });

        appCompatButtonBackpassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(PassengerProfileActivity.this, PassengerActivity.class);
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
                        phUrl = keyId.child("photoUrl").getValue(String.class);
                        break;
                    }
                }
                textInputEditTextFullNameUpdatepassenger.setText(fname);
                textInputEditTextprofessionUpdatepassenger.setText(profession);
                textInputEditTextPlaceUpdatepassenger.setText(workplace);
                textInputEditTextPhoneUpdatepassenger.setText(phone);
                textViewemailpassenger.setText(" "+cemail);
                if(phUrl!=null){

                    Picasso.get().load(phUrl).into(profileimageView);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("s", "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
//                Toast.makeText(this, "hey you selected image" + bitmap, Toast.LENGTH_SHORT).show();
                profileimageView.setImageBitmap(bitmap);
                //ImageView imageView = (ImageView) findViewById(R.id.imageView);
                //imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
//                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.remove(getString(R.string.isDriver));
                editor.commit();

                Intent intent = new Intent(PassengerProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();

                break;

        }

        return super.onOptionsItemSelected(item);
    }



    private void userInformation()  {



        fireuser = mAuth.getCurrentUser();
        byte[] data;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if(bitmap!=null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            data = bytes.toByteArray();
            FirebaseStorage.getInstance().getReference().child("trackBus").child(fireuser.getUid())
                    .child("profilePic")
                    .putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            purl = taskSnapshot.getDownloadUrl().toString();

                            String fulname = textInputEditTextFullNameUpdatepassenger.getText().toString().trim();
                            String prof = textInputEditTextprofessionUpdatepassenger.getText().toString().trim();
                            String work = textInputEditTextPlaceUpdatepassenger.getText().toString().trim();
                            String phoneno = textInputEditTextPhoneUpdatepassenger.getText().toString().trim();
                            User userinformation = new User(fulname,
                                    textViewemailpassenger.getText().toString().trim(), prof, work, phoneno, fireuser.getUid(), purl);
                            databaseReference.child(fireuser.getUid()).setValue(userinformation);
                            Snackbar.make(nestedScrollViewprofilepassenger, "User information updated", Snackbar.LENGTH_LONG).show();

                        }
                    });
        }else{
            String fulnamee = textInputEditTextFullNameUpdatepassenger.getText().toString().trim();
            String profe = textInputEditTextprofessionUpdatepassenger.getText().toString().trim();
            String worke = textInputEditTextPlaceUpdatepassenger.getText().toString().trim();
            String phonenoe = textInputEditTextPhoneUpdatepassenger.getText().toString().trim();

            User userinformation = new User(fulnamee,
                    textViewemailpassenger.getText().toString().trim(), profe, worke, phonenoe, fireuser.getUid(), phUrl);
            databaseReference.child(fireuser.getUid()).setValue(userinformation);
            Snackbar.make(nestedScrollViewprofilepassenger, "User information updated", Snackbar.LENGTH_LONG).show();

        }

//                Toast.makeText(getApplicationContext(),"User information updated",Toast.LENGTH_LONG).show();





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