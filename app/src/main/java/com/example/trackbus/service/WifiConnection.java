package com.example.trackbus.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import androidx.core.app.NotificationManagerCompat;

import com.example.trackbus.LoginActivity;
import com.google.android.material.snackbar.Snackbar;

public class WifiConnection extends BroadcastReceiver {


    NotificationManagerCompat notificationManagerCompat;
    Context con;


    @Override
    public void onReceive(Context context, Intent intent) {
        boolean noConnectivity;

        notificationManagerCompat= NotificationManagerCompat.from(context);
        BusTrackNotification channel = new BusTrackNotification(context);
        channel.createNotificationChannel();

        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);

            if(noConnectivity){

              Snackbar.make(LoginActivity.loginnestedScrollView, "Wi-fi Disconnected", Snackbar.LENGTH_LONG).show();

//                Toast.makeText(context, "Wi-fi Disconnected", Toast.LENGTH_SHORT).show();
            }else{
                Snackbar.make(LoginActivity.loginnestedScrollView, "Wi-fi Connected", Snackbar.LENGTH_LONG).show();
//                Toast.makeText(context, "Wi-fi Connected", Toast.LENGTH_SHORT).show();

            }
        }
    }



}