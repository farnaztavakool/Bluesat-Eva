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
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);

        mcontex = this;
        lm = (LocationManager) mcontex.getSystemService(Context.LOCATION_SERVICE);

        //checks if we have the permission and if not request permission

        register();

        state = 0;
        System.out.print("updating the location");



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
                    100,
                    0, this);

        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        writeToFile(location.toString(),(new Date(location.getTime())).toString(), this);
        Log.d("data", String.valueOf(new Date(location.getTime())));
        Log.e("override",location.toString());


    }
    public void permission() {
        // Storage Permissions
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

/**
 * Checks if the app has permission to write to device storage
 *
 * If the app does not has permission then the user will be prompted to grant permissions
 *
 * @param activity
 */

            // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



    private void writeToFile(String data, String time, Context context) {


        try {
            permission();
            String path = context.getFilesDir().getAbsolutePath();
            File root = new File(path);


            if (!root.exists()) {
                root.mkdir();
            }
            File output = new File(path+"/config.txt");
            if (!output.exists()) output.createNewFile();
            Log.e("in write to file",output.getAbsolutePath()+" "+output.exists());

            if (!output.exists()) output.createNewFile();

            output.setExecutable(true);



            BufferedWriter bf = new BufferedWriter(new FileWriter(output,true));
            bf.append (data);
            bf.append(" "+time);
            bf.newLine();
            bf.close();
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
           // Log.e("Exception", "File write failed: " + e.printStackTrace(););
        }

    }

    public void onLocationChanged(View view) {



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        /**
         * define criteria
         * creating a data object and writting the final object to the .txt file
         */

        LocationProvider provider =
                lm.getProvider(LocationManager.GPS_PROVIDER);

        lm.requestLocationUpdates(provider.getName(), 1000, 0, this);

        Location location = lm.getLastKnownLocation(provider.getName());


//        Double loc = (location.getAltitude());
        writeToFile(location.toString(),new Date(location.getTime()).toString(),this);
        System.out.print("updating the location");
        Log.d("onlocationchanged",location.toString());



    }
    @Override
    public void onProviderEnabled (String provider) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)


    //responsible for initilisation of the activity
    // onStart and onResume can be used too
    //Bundle stores the state of the activity which onCreate can use to create the new version of the activity



    /**
     * ending data collection and returning to the main page
     * @param v
     */
    public void end(View v) {

        lm.removeUpdates(this);

        startActivity(new Intent(SensorActivity.this, MainActivity.class));

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

