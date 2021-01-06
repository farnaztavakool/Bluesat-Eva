package com.bluesat.eva;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


//Used for receiving notifications from the SensorManager when there is new sensor data.
public class SensorActivity extends AppCompatActivity implements LocationListener {

    private SensorManager sm;
    private Sensor accel;
    private float x;
    private float y;
    private float z;
    private TextView tv;
    private int state;
    LocationManager lm;

    //context provides information regarding different parts of the application
    Context mcontex;

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)


    //responsible for initilisation of the activity
    // onStart and onResume can be used too
    //Bundle stores the state of the activity which onCreate can use to create the new version of the activity

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);

        mcontex = this;
        lm = (LocationManager) mcontex.getSystemService(Context.LOCATION_SERVICE);

        //checks if we have the permission and if not request permission

        register();

        state = 0;

    }

    /**
     * ending data collection and returning to the main page
     * @param v
     */
    public void end(View v) {

        lm.removeUpdates(this);

        startActivity(new Intent(SensorActivity.this, MainActivity.class));

    }

    public void register() {
        if (ContextCompat.checkSelfPermission( this,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String [] { Manifest.permission.ACCESS_FINE_LOCATION },
                    99

            );
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000,
                    10, this);

        }
    }

    public void onPause(View view) {
        if (state == 0) {
            super.onPause();
            lm.removeUpdates(this);

            ((Button) findViewById(R.id.pause)).setText("Resume");
            state = 1;
            return;
        }
        onResume();

    }
//
//    /**
//     * registered listener for the accelometer listener
//     */
    public void onResume() {
        super.onResume();
        register();
        ((Button) findViewById(R.id.pause)).setText("Pause");
        state = 0;

    }
//
//    public void export(View v) {
//        //
//    }
//
//
//     public void customReading(Sensor event) {
//          //reading the value of the sensor
//         //defining mylocationlistener and then registering
//         LocationListener ls = new LocationListener();
////         LocationListener locationListener = new MyLocationListener();
//         lm.requestLocationUpdates(lm.GPS_PROVIDER);
////         lm.requestLocationUpdates(
////                 ls.GPS_PROVIDER, 5000, 10, locationListener);
////        }


}

