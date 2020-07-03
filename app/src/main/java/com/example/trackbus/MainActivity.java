package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.trackbus.model.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar =(Toolbar)findViewById(R.id.my_toolbar);
//        toolbar.setTitle("Main System");
//        setSupportActionBar(toolbar);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater  inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.actionjoin:

                break;

            case R.id.actionlogout:

                FirebaseAuth.getInstance().signOut();
                Intent setupIntent = new Intent(getBaseContext(),LoginActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(setupIntent);
                finish();
//                Snackbar.make(this.findViewById(android.R.id.content),"Logged Out",Snackbar.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();

                break;

            case R.id.actionprofile:

                Intent profile = new Intent(this,ProfileActivity.class);
                Intent sintent = getIntent();
                String curremail = sintent.getStringExtra("email");

                profile.putExtra("email", curremail);

                startActivity(profile);
                finish();

                break;


        }

        return super.onOptionsItemSelected(item);
    }
}