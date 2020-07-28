package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.trackbus.service.BusTrackNotification;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PassengerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.passenger_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view_passenger)
    NavigationView mNavigationView;

    public double buslat = 0;
    public  double buslon = 0;

    @BindView(R.id.locate_bus_fab)
    FloatingActionButton locateBus;

//    @BindView(R.id.locate_nearest_bus_fab)
//    FloatingActionButton mLocateNearestBus;


//    @BindView(R.id.passengerprofile)
//    Button mProfile;
    private FirebaseAuth mAuth;

    NotificationManagerCompat notificationManagerCompat;


    public static final String LOG_TAG = PassengerActivity.class.getSimpleName();
    private static final int RC_PER = 2;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private LatLng etaLocation;
    private LatLng passengerLocation;
    private LocationRequest mLocationRequest;
    private int radiusLocateBusRequest = 1;
    private boolean busFound = false;
    private String busDriverKey = "";
    private ActionBarDrawerToggle mDrawerToggle;
    private View mMapView;
    private int bus_num;
    private boolean driverFound = false;
    private SharedPreferences prefs;
    private Marker mBusMarker;
    private ProgressBar spinner;
    private TextView useremail,userfullname;
    private ImageView profileimage;

    float estimatedDriveTimeInMinutes;
    int durationtime,distanceMeter;

    DatabaseReference busRefStatus;
    String busnumberstatus;

    Geocoder geocoder;
    Geocoder geocoders;
    private List<Address> addresses;
    private List<Address> addressess;
    private String address,city,state,substate,country,postalCode,feature,subcity,fare,subfare,premise;
    private String addresss,citys,states,substates,countrys,postalCodes,features,subcitys,fares,subfares,premises;
    String title;
    String addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        ButterKnife.bind(this);

        // Toolbar :: Transparent
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Locate your bus");

        Window window = this.getWindow();
        // Status bar :: Transparent
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

        View header=mNavigationView.getHeaderView(0);
        useremail = header.findViewById(R.id.navemail);
        profileimage = header.findViewById(R.id.profile_img);
        userfullname = header.findViewById(R.id.navfullname);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();




        DatabaseReference rootReff = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userReff = rootReff.child("Users_Detail");

        // Read from the database
        userReff.addValueEventListener(new ValueEventListener() {
            String fnames, cemails, professions, workplaces, phones,purl;

            FirebaseUser fireuserss = mAuth.getCurrentUser();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot keyId: dataSnapshot.getChildren()) {
                    if (keyId.child("email").getValue().equals(fireuserss.getEmail())) {
                        fnames = keyId.child("fullName").getValue(String.class);
                        cemails = keyId.child("email").getValue(String.class);
                        purl = keyId.child("photoUrl").getValue(String.class);
                        break;
                    }
                }
                useremail.setText(cemails);
                userfullname.setText(fnames);


                if(purl!=null){

                    Picasso.get().load(purl).into(profileimage);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("s", "Failed to read value.", error.toException());
            }
        });



        notificationManagerCompat= NotificationManagerCompat.from(this);
        BusTrackNotification channel = new BusTrackNotification(this);
        channel.createNotificationChannel();



        setupDrawerContent(mNavigationView);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mMapView = mapFragment.getView();

        // When the PassengerActivity launches first time, we ask for bus number and then it
        // would be configurable in the settings activity.
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean firstTime = prefs.getBoolean(getString(R.string.passenger_maps_first_time_launch), true);
        if (firstTime) {

            AlertDialog.Builder metadialogBuilder = new AlertDialog.Builder(PassengerActivity.this);
            metadialogBuilder.setTitle(getString(R.string.selectBusTitle))
                    .setItems(R.array.bus_numbers, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    bus_num = 1111;
                                    break;
                                case 1:
                                    bus_num = 2222;
                                    break;
                                case 2:
                                    bus_num = 3333;
                                    break;
                                case 3:
                                    bus_num = 4444;
                                    break;

                            }

                            String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(bus_num)).child(uId);
                            ref.setValue("passenger");
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(getString(R.string.passenger_maps_first_time_launch), false);
                            editor.putInt(getString(R.string.bus_no), bus_num);
                            editor.apply();
                        }
                    });
            AlertDialog dialog = metadialogBuilder.create();
            dialog.show();
            Log.e(LOG_TAG, "Bus number selected by user is : " + bus_num);


            //
        }

        locateBus = (FloatingActionButton) findViewById(R.id.locate_bus_fab);
//        locateBus.setBackgroundColor(getResources().getColor(R.color.white));
//        locateBus.setImageResource(R.drawable.activity);




        spinner=findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        spinner.getLayoutParams().height = 30;


//        mLocateNearestBus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("passengerRequestNearestBus");
//                GeoFire geoFire = new GeoFire(reference);
//                geoFire.setLocation(uid, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//                passengerLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
//
//                getNearestBus();
//            }
//        });


        locateBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PassengerActivity.this, "Lets go Lets go!", Toast.LENGTH_SHORT).show();
                if (busDriverKey.isEmpty()){
                    Toast.makeText(PassengerActivity.this, "Sorry, Your driver is not online!", Toast.LENGTH_LONG).show();
                    return;
                }
//
//                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("request_eta");
//                GeoFire geoFire = new GeoFire(reference);
//                geoFire.setLocation(uid, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//
                DatabaseReference busLocation = FirebaseDatabase.getInstance().getReference().child("driver_available").child(busDriverKey).child("l");
                busLocation.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            List<Object> map = (List<Object>)dataSnapshot.getValue();

                            if (map.get(0) != null){
                                buslat = Double.parseDouble(map.get(0).toString());
                            }

                            if (map.get(1) != null){
                                buslon = Double.parseDouble(map.get(1).toString());
                            }


                            final LatLng busLocationStatus = new LatLng(buslat, buslon);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());


                                try {
                                    addressess = geocoder.getFromLocation(buslat, buslon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                                addresss = addressess.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                    citys = addressess.get(0).getLocality();
                                    states = addressess.get(0).getAdminArea();
                                    countrys = addressess.get(0).getCountryName();
                                    substates = addressess.get(0).getSubAdminArea();
                                    fares = addressess.get(0).getThoroughfare();
                                    subfares = addressess.get(0).getThoroughfare();
                                    premises = addressess.get(0).getPremises();

                                    postalCodes = addressess.get(0).getPostalCode();
                                    subcitys = addressess.get(0).getSubLocality();

                                    features = addressess.get(0).getFeatureName();
                                    title = addresss + "-" + citys + "-" + states;

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Log.d("far",distance(buslat,buslon,'K')+" Kilometers far away");
                                Log.d("far", distance(buslat, buslon, 'M') + " Miles far away");

                            busRefStatus = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(bus_num));


                            busRefStatus.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                    String isStatus = dataSnapshot.getValue(String.class);

                                    Log.d("status",isStatus);
                                    if (isStatus.equals("traffic_jam")) {

                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions().position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Bus is stuck in traffic jam")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                        mBusMarker.showInfoWindow();

                                    }
                                    else if(isStatus.equals("bus_full")) {
                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions()
                                                .position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Bus is fully occupied")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                        mBusMarker.showInfoWindow();

                                    }
                                    else if(isStatus.equals("normal")) {

                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions()
                                                .position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Your bus is here")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        mBusMarker.showInfoWindow();

                                    } else{
                                        if (mBusMarker != null)
                                            mBusMarker.remove();
                                            mBusMarker = mMap.addMarker(new MarkerOptions()
                                                    .position(busLocationStatus)
                                                    .title(subcitys+","+citys)
                                                    .snippet("Your bus is here")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        mBusMarker.showInfoWindow();

                                    }




                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    String isStatus = dataSnapshot.getValue(String.class);

                                    Log.d("status",isStatus);
                                    if (isStatus.equals("traffic_jam")) {

                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions().position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Bus is stuck in traffic jam")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                                        mBusMarker.showInfoWindow();
                                    }
                                    else if(isStatus.equals("bus_full")) {
                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions()
                                                .position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Bus is fully occupied")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                        mBusMarker.showInfoWindow();
                                    }
                                    else if(isStatus.equals("normal")) {

                                        if (mBusMarker != null) mBusMarker.remove();
                                        mBusMarker = mMap.addMarker(new MarkerOptions()
                                                .position(busLocationStatus)
                                                .title(subcitys+","+citys)
                                                .snippet("Your bus is here")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        mBusMarker.showInfoWindow();

                                    } else{
                                        if (mBusMarker != null)
                                            mBusMarker.remove();
                                            mBusMarker = mMap.addMarker(new MarkerOptions()
                                                    .position(busLocationStatus)
                                                    .title(subcitys+","+citys)
                                                    .snippet("Your bus is here")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                        mBusMarker.showInfoWindow();

                                    }
                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });







                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.passengerprofile:
                        Intent pintent = new Intent(PassengerActivity.this, PassengerProfileActivity.class);
                        startActivity(pintent);
                        finish();

                        break;



                    case R.id.passenger_contactus:
                        Intent cintent = new Intent(PassengerActivity.this, ContactActivity.class);
                        startActivity(cintent);
                        finish();

                        break;


                    case R.id.eta:

                        if(buslat ==0 && buslon==0){
                            displayNotification2();

                        }else {
                            CalculationByDistance(buslat, buslon);
                            displayNotification1();
                        }
//                        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                       final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("request_eta");
//                        GeoFire geoFire = new GeoFire(reference);
//                        geoFire.setLocation(uid, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//                        etaLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

//                        mMap.addMarker(new MarkerOptions().position(etaLocation).title("Your are here"));




                        break;
                    case R.id.request_wait:
                        if (prefs.getInt(getString(R.string.bus_no), 0)==0){
                            Toast.makeText(PassengerActivity.this, "Please link your bus first!", Toast.LENGTH_LONG).show();
                            break;
                        }
                        spinner.setVisibility(View.VISIBLE);
                        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("request_wait");
                        GeoFire geofire = new GeoFire(ref);
                        geofire.setLocation(userId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));

                        etaLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                        geocoders = new Geocoder(getApplicationContext(), Locale.getDefault());



                            try {
                                addresses = geocoders.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                             address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                city = addresses.get(0).getLocality();
                                state = addresses.get(0).getAdminArea();
                                country = addresses.get(0).getCountryName();
                                substate = addresses.get(0).getSubAdminArea();
//                                fare = addresses.get(0).getThoroughfare();
//                                subfare = addresses.get(0).getThoroughfare();
//                                premise = addresses.get(0).getPremises();
//
//                                postalCode = addresses.get(0).getPostalCode();
                                subcity = addresses.get(0).getSubLocality();

                                feature = addresses.get(0).getFeatureName();
//                                title = address + "-" + city + "-" + state;

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                        //create your custom title
                      Marker pmarker=  mMap.addMarker(new MarkerOptions().position(etaLocation)
                                .title(subcity+","+city)
                                .snippet("I am here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                        pmarker.showInfoWindow();


                        Toast.makeText(PassengerActivity.this, "Requesting...", Toast.LENGTH_SHORT).show();

                        int busNo = prefs.getInt(getString(R.string.bus_no), 0);
                        if (busNo==0){
                            Toast.makeText(PassengerActivity.this, "Please add your bus number first in settings!", Toast.LENGTH_LONG).show();
                            break;
                        }
                        DatabaseReference busRef = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(busNo));
                        busRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                busDriverKey = dataSnapshot.getKey();
                                String isDriver = dataSnapshot.getValue(String.class);
                                if (isDriver.equals("driver")){
                                    busDriverKey = dataSnapshot.getKey();
                                    driverFound = true;
                                    HashMap map = new HashMap();
                                    map.put("busDriverID", busDriverKey);
                                    ref.child(userId).updateChildren(map);
                                    Log.e(LOG_TAG, "keyis : " + busDriverKey);

                                    String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child("Driver")
                                            .child(busDriverKey)
                                            .child("passengerRequest");
                                    driverRef.setValue(passengerId);

                                    DatabaseReference locationAdd = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(passengerId);
                                    GeoFire geofire = new GeoFire(locationAdd);
                                    geofire.setLocation(passengerId, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                                    return;
                                }
                                spinner.setVisibility(View.GONE);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        break;

                    case R.id.link_bus:

                        AlertDialog.Builder metadialogBuilder = new AlertDialog.Builder(PassengerActivity.this);
                        metadialogBuilder.setTitle(getString(R.string.selectBusTitle))
                                .setItems(R.array.bus_numbers, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        switch (i) {
                                            case 0:
                                                bus_num = 1111;
                                                break;
                                            case 1:
                                                bus_num = 2222;
                                                break;
                                            case 2:
                                                bus_num = 3333;
                                                break;
                                            case 3:
                                                bus_num = 4444;
                                                break;

                                        }

                                        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(bus_num)).child(uId);
                                        ref.setValue("passenger");
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putInt(getString(R.string.bus_no), bus_num);
                                        editor.commit();

                                    }
                                });
                        metadialogBuilder.show();
                        break;

                    case R.id.logout:
                        displayNotification();
//                        LoginManager.getInstance().logOut();
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove(getString(R.string.isDriver));
                        editor.remove(getString(R.string.passenger_maps_first_time_launch));
                        editor.remove(getString(R.string.bus_no));
                        editor.commit();

                        Intent intent = new Intent(PassengerActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void getNearestBus() {
        DatabaseReference nearestBus = FirebaseDatabase.getInstance().getReference().child("driver_available");
        GeoFire geoFire = new GeoFire(nearestBus);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(passengerLocation.latitude, passengerLocation.longitude), radiusLocateBusRequest);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!busFound) {
                    busFound = true;
                    busDriverKey = key;
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!busFound) {
                    radiusLocateBusRequest++;
                    getNearestBus();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 60);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public  void onLocationChanged(Location location) {

        mLastKnownLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.e(LOG_TAG, "Latitude and longitude are : " + latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, RC_PER);
    }


    public void displayNotification() {
        Notification notification = new NotificationCompat
                .Builder(this, BusTrackNotification.Logout_Channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Logout")
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentText("Successfully logged out")
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        notificationManagerCompat.notify(1, notification);
    }


    public void displayNotification1() {
        int plusone = durationtime+1;
        Notification notification = new NotificationCompat
                .Builder(this, BusTrackNotification.Logout_Channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Distance & Duration Information")
                .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher_foreground))
                .setContentText(distanceMeter+" Meters far away "+" || "+" Bus will come within "+durationtime+"-"+plusone+" Minutes")
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();


        notificationManagerCompat.notify(1, notification);
    }



    public void displayNotification2() {
        Notification notification = new NotificationCompat
                .Builder(this, BusTrackNotification.Logout_Channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Warning")
                .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher_foreground))
                .setContentText("Bus location not found")
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();


        notificationManagerCompat.notify(1, notification);
    }



    private Location getCurrentLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) return null;

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return location;
    }



    private double distance(double lat2, double lon2, char unit) {
        Location location = getCurrentLocation();


        double theta = location.getLongitude() - lon2;
        double dist = Math.sin(deg2rad(location.getLatitude())) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(location.getLatitude())) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public double CalculationByDistance(double lat2, double lon2) {
        Location location = getCurrentLocation();


        Location location1 = new Location("");
        location1.setLatitude(location.getLatitude());
        location1.setLongitude(location.getLongitude());

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        float distanceInMeters = location1.distanceTo(location2);

        //For example spead is 100 meters per minute.
        int speedIs10MetersPerMinute = 100;
        estimatedDriveTimeInMinutes = distanceInMeters / speedIs10MetersPerMinute;


        int Radius = 6371;// radius of earth in Km
        double lat1 = location.getLatitude();
        double lon1 = location.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("far", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        Log.d("far",estimatedDriveTimeInMinutes+" Minutes");
        Log.d("far",distanceInMeters+" in Meters");
        distanceMeter =(int) distanceInMeters;
        durationtime = (int) estimatedDriveTimeInMinutes;
        Log.d("far",durationtime+" minutes");

        return Radius * c;
    }


}
