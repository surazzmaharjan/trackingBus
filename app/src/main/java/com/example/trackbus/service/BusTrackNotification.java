package com.example.trackbus.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class BusTrackNotification {
    Context context;
    public final static String Login_Channel ="Login";
    public final static String Signup_Channel ="Signup";
    public final static String Logout_Channel ="Logout";
    public final static String Refresh_Channel ="Refresh";
    public final static String Disconnected_Channel ="Disconnected";
    public final static String Connected_Channel ="Connected";
    public final static String Distance_Channel ="Distance";

    public final static String Driver_Channel ="Available";

    public BusTrackNotification(Context context) {
        this.context = context;
    }

    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel login = new NotificationChannel(Login_Channel,"Login", NotificationManager.IMPORTANCE_HIGH);
            login.setDescription("Successfully login to dashboard");
            login.enableVibration(true);
            login.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            login.setShowBadge(false);


            NotificationChannel distance = new NotificationChannel(Distance_Channel,"Distance", NotificationManager.IMPORTANCE_HIGH);
            login.enableVibration(true);
            login.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            login.setShowBadge(false);


            NotificationChannel signup = new NotificationChannel(Signup_Channel,"Signup", NotificationManager.IMPORTANCE_HIGH);
            signup.setDescription("Successfully signup");
            signup.enableVibration(true);
            signup.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            signup.setShowBadge(false);


            NotificationChannel disconnected = new NotificationChannel(Disconnected_Channel,"Disconnected", NotificationManager.IMPORTANCE_HIGH);
            disconnected.setDescription("Disconnected Wifi");
            disconnected.enableVibration(true);
            disconnected.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            disconnected.setShowBadge(false);


            NotificationChannel connected = new NotificationChannel(Connected_Channel,"Connected", NotificationManager.IMPORTANCE_HIGH);
            connected.setDescription("Connected Wifi");
            connected.enableVibration(true);
            connected.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            connected.setShowBadge(false);



            NotificationChannel logout = new NotificationChannel(Logout_Channel,"Logout", NotificationManager.IMPORTANCE_HIGH);
            connected.setDescription("Logout Successfully");
            connected.enableVibration(true);
            connected.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            connected.setShowBadge(false);



            NotificationChannel refresh = new NotificationChannel(Refresh_Channel,"Refresh", NotificationManager.IMPORTANCE_HIGH);
            connected.setDescription("Refresh Successfully");
            connected.enableVibration(true);
            connected.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            connected.setShowBadge(false);


            NotificationChannel available = new NotificationChannel(Driver_Channel,"Available", NotificationManager.IMPORTANCE_HIGH);
//            connected.setDescription("Refresh Successfully");
            connected.enableVibration(true);
            connected.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            connected.setShowBadge(false);



            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(login);
            manager.createNotificationChannel(logout);
            manager.createNotificationChannel(refresh);
            manager.createNotificationChannel(signup);
            manager.createNotificationChannel(disconnected);
            manager.createNotificationChannel(connected);
            manager.createNotificationChannel(available);
            manager.createNotificationChannel(distance);


        }
    }
}
