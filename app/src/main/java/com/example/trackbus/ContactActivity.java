package com.example.trackbus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ContactActivity extends AppCompatActivity  implements OnMapReadyCallback {


    private GoogleMap gmap;
    private LatLng ownlocation = new LatLng(27.628379,85.302189);
    private Marker LocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Toolbar toolbar =(Toolbar)findViewById(R.id.contacttoolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("Contact Us");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ContactActivity.this, PassengerActivity.class);
                startActivity(mainIntent);
            }
        });


        ImageButton fbButton = findViewById(R.id.facebookButtton);
        ImageButton instaButton =  findViewById(R.id.instagramButton);
        ImageButton twitButton = findViewById(R.id.twitterButton);

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getOpenFacebookIntent(ContactActivity.this, "100003881591172","tsurajmaharjan"));

            }
        });
        instaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getOpenInstagramIntent(ContactActivity.this, "tsurajmaharjan"));

            }
        });

        twitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(getOpenTwitterIntent(ContactActivity.this, "tsuraj123"));

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googlemap);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        drawMarker();
    }

    private void drawMarker() {
        if (gmap == null) return;

        if (LocationMarker == null) {
            LocationMarker = gmap.addMarker(new MarkerOptions().position(ownlocation).title("Bus Track"));
        }else{
            LocationMarker.setPosition(ownlocation);
        }
        CameraUpdate center = CameraUpdateFactory.newLatLng(ownlocation);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        gmap.moveCamera(center);
        gmap.animateCamera(zoom);
    }

    public Intent getOpenInstagramIntent(Context context, String userName) {
        try {
            context.getPackageManager().getPackageInfo("com.instagram.android", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/" + userName));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/" + userName));
        }
    }


    public static Intent getOpenFacebookIntent(Context context, String profileId, String userName) {
        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb.com/" + profileId));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + userName));
        }
    }


    public static Intent getOpenTwitterIntent(Context context, String twitter_user_name) {
        try {
            context.getPackageManager().getPackageInfo("com.twitter.android", 0);


            return  new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitter_user_name));
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitter_user_name));
        }
    }
}