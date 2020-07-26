package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.trackbus.model.User;
import com.example.trackbus.validation.InputValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText nameEditText, professionEditText, workEditText, passwordEditText;
    private TextInputEditText phoneEditText, emailEditText;
    private ImageView picImageView;
    private AppCompatButton registerButton;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private static final String USERS = "Users_Detail";
    private String TAG = "RegisterActivity";
    private String username, fname, email, profession, phone, workplace,user_type;
    private String password;
    private User user;
    private FirebaseAuth mAuth;
    private Spinner spinner;




    private NestedScrollView nestedScrollView;

    private TextInputLayout textInputLayoutSignupEmail;
    private TextInputLayout textInputLayoutSignupFullname;
    private TextInputLayout textInputLayoutProfession;
    private TextInputLayout textInputLayoutWorkPlace;
    private TextInputLayout textInputLayoutPhone;
    private TextInputLayout textInputLayoutSignupPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText textInputEditTextConfirmPassword;



    private AppCompatTextView appCompatTextViewLoginLink;
    private InputValidation inputValidation;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameEditText = findViewById(R.id.fullname_edittext);
        professionEditText = findViewById(R.id.profession_edittext);
        workEditText = findViewById(R.id.workplace_edittext);
        phoneEditText = findViewById(R.id.phone_edittext);
        passwordEditText = findViewById(R.id.enterpass_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        registerButton = findViewById(R.id.register_button);
        spinner = findViewById(R.id.chooser_spinner);



        nestedScrollView = findViewById(R.id.nestedSignupScrollView);

        textInputLayoutSignupEmail =findViewById(R.id.text_input_layout_signup_email);
        textInputLayoutSignupFullname =findViewById(R.id.text_input_layout_fullname);
        textInputLayoutProfession = findViewById(R.id.text_input_layout_profession);
        textInputLayoutWorkPlace = findViewById(R.id.text_input_layout_workplace);
        textInputLayoutPhone = findViewById(R.id.text_input_layout_phone);
        textInputLayoutSignupPassword =  findViewById(R.id.text_input_layout_signup_password);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutRegisterConfirmPassword);

        textInputEditTextConfirmPassword = findViewById(R.id.textInputEditTextRegisterConfirmPassword);

        appCompatTextViewLoginLink =  findViewById(R.id.appCompatTextViewLoginLink);
        appCompatTextViewLoginLink.setPaintFlags(appCompatTextViewLoginLink.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);



        registerButton.setOnClickListener(this);
        appCompatTextViewLoginLink.setOnClickListener(this);
        inputValidation = new InputValidation(this);



        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USERS);
        mAuth = FirebaseAuth.getInstance();





    }

    public void registerUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {



                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            String user_id  = mAuth.getCurrentUser().getUid();

                            user = new User(fname, email, profession, workplace, phone,user_id);

                            mDatabase.child(user_id).setValue(user); //adding user info to database

                            Boolean isDriver = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.isDriver), false);
                            if (isDriver) {
                                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                                user_db.setValue(true);
//                                user_db.setValue(user);
                                Snackbar.make(nestedScrollView, "Driver was register successfully", Snackbar.LENGTH_LONG).show();
                                emptyInputEditText();

                            } else {
                                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(user_id);
                                user_db.setValue(true);
//                                user_db.setValue(user);

                                Snackbar.make(nestedScrollView, "Passenger was register successfully", Snackbar.LENGTH_LONG).show();
                                emptyInputEditText();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();

                            Snackbar.make(nestedScrollView, "Authentication failed.", Snackbar.LENGTH_LONG).show();

                        }
                    }
                });
    }

    /**
     * adding user information to database and redirect to login screen
     *
     */
    public void updateUI(FirebaseUser currentuser) {


    }

    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        nameEditText.setText(null);
        emailEditText.setText(null);
        phoneEditText.setText(null);
        professionEditText.setText(null);
        workEditText.setText(null);
        passwordEditText.setText(null);
        textInputEditTextConfirmPassword.setText(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.register_button:

                if(emailEditText.getText().toString() != null && passwordEditText.getText().toString() != null) {
                    fname = nameEditText.getText().toString();
                    email = emailEditText.getText().toString();
                    phone = phoneEditText.getText().toString();
                    profession = professionEditText.getText().toString();
                    workplace = workEditText.getText().toString();
                    password = passwordEditText.getText().toString();
                    String usertype = spinner.getSelectedItem().toString();
                    user_type = spinner.getSelectedItem().toString();

                    if (!inputValidation.isInputEditTextFilled(emailEditText, textInputLayoutSignupEmail, "Enter a email")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextEmail(emailEditText, textInputLayoutSignupEmail,"Enter a valid email")) {
                        return;
                    }

                    if (!inputValidation.isInputEditTextFilled(passwordEditText, textInputLayoutSignupPassword, "Enter a password")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextMatches(passwordEditText, textInputEditTextConfirmPassword,
                            textInputLayoutConfirmPassword,"Password Does Not Matches")) {
                        return;
                    }

                    if (!inputValidation.isInputEditTextFilled(nameEditText, textInputLayoutSignupFullname, "Enter a fullname")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextFilled(professionEditText, textInputLayoutProfession, "Enter a profession")) {
                        return;
                    }


                    if (!inputValidation.isInputEditTextFilled(workEditText, textInputLayoutWorkPlace, "Enter a work place")) {
                        return;
                    }

                    if (!inputValidation.isInputEditTextFilled(phoneEditText, textInputLayoutPhone,"Enter a phone number")) {
                        return;
                    }
                    if (!inputValidation.isInputEditTextNumber(phoneEditText, textInputLayoutPhone, "Enter a valid phone number")) {
                        return;
                    }





                    if (usertype.equals("Driver")) {
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putBoolean(getString(R.string.isDriver), true);
                        editor.commit();
                    } else {
                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.remove(getString(R.string.isDriver));
                        editor.commit();
                    }


                    registerUser();
                }
                break;

            case R.id.appCompatTextViewLoginLink:
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
}