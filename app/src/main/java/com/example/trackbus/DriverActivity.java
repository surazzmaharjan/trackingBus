package com.example.trackbus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackbus.service.BusTrackNotification;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.material.snackbar.Snackbar;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {



    @BindView(R.id.logout_btn_driver)
    Button mLogout;

//    @BindView(R.id.profile)
//    Button mProfile;

    @BindView(R.id.link_bus)
    Button mLinkBus;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;


    public static final String LOG_TAG = DriverActivity.class.getSimpleName();
    private static final int RC_PER = 2;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;
    private int bus_num;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mMapView;
    private String channelId = "my_channel";
    private String passengerId = "";
    double passengerLocationLat = 0;
    double passengerLocationLon = 0;
    private Marker mPassengerMarker;
    private TextView useremail ,userfullname;
    private ImageView profileimage;

    CoordinatorLayout coordinatorLayout;
    private FirebaseAuth mAuth;

     NotificationManager mNotificationManager;

    NotificationManagerCompat notificationManagerCompat;

    String currentBusStatus;

    @BindView(R.id.bus_status_fab)
    FloatingActionButton statusBus;

    private Geocoder geocoder;
    private List<Address> addresses;
    private List<Address> addressess;
    private String address,city,state,substate,country,postalCode,feature,subcity,fare,subfare,premise;
    private String addresss,citys,states,substates,countrys,postalCodes,features,subcitys,fares,subfares,premises;

    String title;
    Marker busStatusMaker;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //First check if GPS is enabled
        turnGPSOn();

        notificationManagerCompat= NotificationManagerCompat.from(this);
        BusTrackNotification channel = new BusTrackNotification(this);
        channel.createNotificationChannel();


        setContentView(R.layout.activity_driver);

        ButterKnife.bind(this);
        // Toolbar :: Transparent
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bus Tracking System");

        View header=mNavigationView.getHeaderView(0);



        coordinatorLayout = findViewById(R.id.driverCoordinatorlayout);
        useremail = header.findViewById(R.id.navemail);
        profileimage = header.findViewById(R.id.profile_img);
        userfullname = header.findViewById(R.id.navfullname);

        userfullname.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        useremail.setText(currentUser.getEmail());
        userfullname.setText(currentUser.getDisplayName());
////        Log.d("cuser",currentUser.getDisplayName());
        if(currentUser.getPhotoUrl()!=null){
            String photoUrl = currentUser.getPhotoUrl().toString();
            photoUrl = photoUrl+ "?type=large";

            Picasso.get().load(photoUrl).into(profileimage);
        }


        statusBus = (FloatingActionButton) findViewById(R.id.bus_status_fab);

        statusBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bus_num==0){
                    Toast.makeText(DriverActivity.this, "Sorry, Please choose bus number first!", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder metaDialog = new AlertDialog.Builder(DriverActivity.this);
                metaDialog.setTitle("Current bus status?")
                        .setItems(R.array.bus_status, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        currentBusStatus="bus_full";
                                        break;
                                    case 1:
                                        currentBusStatus="traffic_jam";

                                        break;
                                    case 2:
                                        currentBusStatus="normal";

                                        break;


                                }
                                String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(bus_num)).child("bus_status");
                                ref.setValue(currentBusStatus);
                                Snackbar.make(coordinatorLayout,"Current bus status updated",Snackbar.LENGTH_LONG).show();
                            }
                        });
                metaDialog.show();

            }
        });

        mNotificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        setupDrawerContent(mNavigationView);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMapView = mapFragment.getView();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Boolean startFromNotification = getIntent().getBooleanExtra(getString(R.string.launched_via_notification), false);
        if (startFromNotification) {

            Log.e(LOG_TAG, "Launched from notification");
            String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(uId);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        passengerId = map.get("passengerRequest").toString();


                        Log.e(LOG_TAG, "Passenger id is " + passengerId);

                        DatabaseReference passengerLocation = FirebaseDatabase.getInstance().getReference().child("Users").child("Passengers").child(passengerId).child(passengerId).child("l");
                        passengerLocation.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<Object> map = (List<Object>) dataSnapshot.getValue();
                                passengerLocationLat = Double.parseDouble(map.get(0).toString());
                                passengerLocationLon = Double.parseDouble(map.get(1).toString());
                                LatLng passengerLatLng = new LatLng(passengerLocationLat, passengerLocationLon);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());


                                    try {
                                        addresses = geocoder.getFromLocation(passengerLocationLat, passengerLocationLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                        city = addresses.get(0).getLocality();
                                        state = addresses.get(0).getAdminArea();
                                        country = addresses.get(0).getCountryName();
                                        substate = addresses.get(0).getSubAdminArea();
                                        fare = addresses.get(0).getThoroughfare();
                                        subfare = addresses.get(0).getThoroughfare();
                                        premise = addresses.get(0).getPremises();

                                        postalCode = addresses.get(0).getPostalCode();
                                        subcity = addresses.get(0).getSubLocality();

                                        feature = addresses.get(0).getFeatureName();
                                        title = address + "-" + city + "-" + state;

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (mPassengerMarker!=null) mPassengerMarker.remove();
                                mPassengerMarker = mMap.addMarker
                                        (new MarkerOptions()
                                                .position(passengerLatLng)
                                                .title(subcity+","+city)
                                                .snippet("Your passenger")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                Log.e(LOG_TAG, "Location of the passenger is " + map.get(0));
                                mPassengerMarker.showInfoWindow();
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(uid);
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(DriverActivity.this, "Request Available!", Toast.LENGTH_LONG).show();
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    Intent notificationIntent = new Intent(DriverActivity.this, DriverActivity.class);
                    notificationIntent.putExtra(getString(R.string.launched_via_notification), true);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(DriverActivity.this);
                    stackBuilder.addParentStack(DriverActivity.this);
                    stackBuilder.addNextIntent(notificationIntent);
                    PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(1,
                            PendingIntent.FLAG_UPDATE_CURRENT);

//                    NotificationCompat.Builder builder = new NotificationCompat.Builder(DriverActivity.this, channelId);
//
//                    builder.setSmallIcon(R.drawable.ic_launcher_foreground)
//                            .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher_foreground))
//                            .setContentTitle(getString(R.string.request_wait_notification))
//                            .setContentText("Tap me to view him/her on the map")
//                            .setContentIntent(notificationPendingIntent)
//                            .setAutoCancel(true)
//                            .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
//                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));



                    Notification notification= new NotificationCompat
                            .Builder(DriverActivity.this,BusTrackNotification.Driver_Channel)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setLargeIcon(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher_foreground))
                            .setContentTitle(getString(R.string.request_wait_notification))
                            .setContentText("Tap me to view him/her on the map")
                            .setContentIntent(notificationPendingIntent)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .build();

                    notificationManagerCompat.notify(1,notification);


//                    mNotificationManager.notify(1, builder.build());
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void turnGPSOn() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled in your device. Enable it?")
                    .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            /** Here it's leading to GPS setting options*/
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawrItem(item);
                return true;
            }
        });

    }

    private void selectDrawrItem(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.driver_contactus:
                Intent cintent = new Intent(DriverActivity.this, DriverContactActivity.class);
                startActivity(cintent);
                finish();

                break;


//            case R.id.profile:
//                Intent pintent = new Intent(DriverActivity.this, ProfileActivity.class);
//                startActivity(pintent);
//                finish();
//
//                break;

            case R.id.passengerlistdrawer:
                Intent pintent = new Intent(DriverActivity.this, AllPassengerList.class);
                startActivity(pintent);
                finish();

                break;


            case R.id.link_bus:
                AlertDialog.Builder metaDialog = new AlertDialog.Builder(DriverActivity.this);
                metaDialog.setTitle(getString(R.string.selectBusTitle))
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
                                ref.setValue("driver");
                            }
                        });
                metaDialog.show();
                break;
            case R.id.logout:
                displayNotification();
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.remove(getString(R.string.isDriver));
                editor.commit();

                Intent intent = new Intent(DriverActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;

        }
        item.setChecked(true);
        mDrawerLayout.closeDrawers();
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
    protected void onDestroy() {
        super.onDestroy();


        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            String uid = mFirebaseUser.getUid(); //Do what you need to do with the id
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("driver_available");
            GeoFire geoFire = new GeoFire(reference);
            geoFire.removeLocation(uid);
        }



    }

    @Override
    public void onLocationChanged(Location location) {

        mLastKnownLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.e(LOG_TAG, "Latitude and longitude are : " + latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

//        FirebaseUser mFirebaseUsers = mAuth.getCurrentUser();

//        if(mFirebaseUsers != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("driver_available");
            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()));
//        }

       final LatLng driverLoct= new LatLng(location.getLatitude(), location.getLongitude());

        DatabaseReference busRefStatus;
        busRefStatus = FirebaseDatabase.getInstance().getReference().child("Buses").child(String.valueOf(bus_num));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {

            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());


            try {
                addressess = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
////            addresss = addressess.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
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
        busRefStatus.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

//                String isStatus = dataSnapshot.getValue(String.class);
//
//                Log.d("status",isStatus);
//                if (isStatus.equals("traffic_jam")) {
//                    if (busStatusMaker != null) busStatusMaker.remove();
//                    busStatusMaker= mMap.addMarker(new MarkerOptions().position(driverLoct)
//                            .title(subcitys+","+citys)
//                            .snippet("My bus is stuck in traffic jam")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
////                    busStatusMaker.showInfoWindow();
//
//                }
//                else if(isStatus.equals("bus_full")) {
//                    if (busStatusMaker != null) busStatusMaker.remove();
//                    busStatusMaker= mMap.addMarker(new MarkerOptions()
//                            .position(driverLoct)
//                            .title(subcitys+","+citys)
//                            .snippet("My bus is fully occupied")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
////                    busStatusMaker.showInfoWindow();
//
//                }
//                else if(isStatus.equals("normal")) {
//                    if (busStatusMaker != null) busStatusMaker.remove();
//                    busStatusMaker = mMap.addMarker(new MarkerOptions()
//                            .position(driverLoct)
//                            .title(subcitys+","+citys)
//                            .snippet("My bus is here")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
////                    busStatusMaker.showInfoWindow();
//
//                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String isStatus = dataSnapshot.getValue(String.class);

                Log.d("status",isStatus);
                if (isStatus.equals("traffic_jam")) {
                    if (busStatusMaker != null) busStatusMaker.remove();
                    busStatusMaker= mMap.addMarker(new MarkerOptions().position(driverLoct)
                            .title(subcitys+","+citys)
                            .snippet("My bus is stuck in traffic jam")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    busStatusMaker.showInfoWindow();

                }
                else if(isStatus.equals("bus_full")) {
                    if (busStatusMaker != null) busStatusMaker.remove();
                    busStatusMaker= mMap.addMarker(new MarkerOptions()
                            .position(driverLoct)
                            .title(subcitys+","+citys)
                            .snippet("My bus is fully occupied")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    busStatusMaker.showInfoWindow();

                }
                else if(isStatus.equals("normal")) {
                    if (busStatusMaker != null) busStatusMaker.remove();
                    busStatusMaker = mMap.addMarker(new MarkerOptions()
                            .position(driverLoct)
                            .title(subcitys+","+citys)
                            .snippet("My bus is here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    busStatusMaker.showInfoWindow();

                }
                else{
                    if (busStatusMaker != null) busStatusMaker.remove();
                    busStatusMaker =mMap.addMarker(new MarkerOptions()
                                .position(driverLoct)
                                .title(subcitys+","+citys)
                                .snippet("My bus is here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    busStatusMaker.showInfoWindow();

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, RC_PER);
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


}
