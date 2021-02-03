package com.bluesat.eva;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;


//Used for receiving notifications from the SensorManager when there is new sensor data.
public class SensorActivity extends AppCompatActivity implements LocationListener {

    private SensorManager sm;
    private Sensor accel;
    private float x;
    private float y;
    private float z;
    private TextView tv;

    /*
    0 = Recording
    1 = Paused
     */
    private int state;
    LocationManager lm;

    private static final int STATE_RECORDING = 0;
    private static final int STATE_PAUSED = 1;

    //context provides information regarding different parts of the application
    Context mcontex;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.sensor );

        mcontex = this;
        lm = (LocationManager) mcontex.getSystemService( Context.LOCATION_SERVICE );

        //checks if we have the permission and if not request permission

        register();

        state = 0;
        System.out.print( "updating the location" );

        // Register long press listener
        this.findViewById( R.id.pauseResumeButton ).setOnTouchListener( this.stopRecordingOnTouchListener );
        this.findViewById( R.id.recordButton ).setOnTouchListener( this.stopRecordingOnTouchListener );
    }

    public void register() {
        int coarseLocationPermission = ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION );
        if( coarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    99
            );

            lm.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                    100,
                    0, this
            );
        }
    }

    @Override
    public void onLocationChanged( @NonNull Location location ) {
        writeToFile( location.toString(), (new Date( location.getTime() )).toString(), this );
        Log.d( "data", String.valueOf( new Date( location.getTime() ) ) );
        Log.e( "override", location.toString() );
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
        int permission = ActivityCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE );

        if( permission != PackageManager.PERMISSION_GRANTED ) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void writeToFile( String data, String time, Context context ) {
        try {
            permission();
            //String path = context.getFilesDir().getAbsolutePath();
            String path = "/storage/emulated/0/Download";


            File root = new File( path );

            if( !root.exists() ) {
                root.mkdir();
            }

            File output = new File( path + "/config.txt" );
            if( !output.exists() ) output.createNewFile();

            Log.e( "in write to file", output.getAbsolutePath() + " " + output.exists() );

            if( !output.exists() ) output.createNewFile();

            output.setExecutable( true );

            BufferedWriter bf = new BufferedWriter( new FileWriter( output, true ) );
            bf.append( data );
            bf.append( " " + time );
            bf.newLine();
            bf.close();

            // OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            // outputStreamWriter.write(data);
            // outputStreamWriter.close();
        } catch( IOException e ) {
            e.printStackTrace();
            // Log.e("Exception", "File write failed: " + e.printStackTrace(););
        }
    }

    public void onRecordButtonClick( View view ) {
        if( this.state == SensorActivity.STATE_RECORDING ) {
            this.onLocationChanged( view );
        } else {
            this.onPauseResumeButtonClick( view );
        }
    }

    public void onLocationChanged( View view ) {
        if( ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
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
         * creating a data object and writing the final object to the .txt file
         */

        LocationProvider locationProvider = lm.getProvider( LocationManager.GPS_PROVIDER );

        lm.requestLocationUpdates( locationProvider.getName(), 1000, 0, this );

        Location location = lm.getLastKnownLocation( locationProvider.getName() );
        if( location != null ) {
            TextView logTextView = this.findViewById( R.id.logTextView );

            logTextView.setText( String.format( Locale.UK, "00:00:00\nLat %.02f\nLong %.02f\nAlt %.02f",
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude() )
            );


//            Double loc = (location.getAltitude());
            writeToFile( location.toString(), new Date( location.getTime() ).toString(), this );
            System.out.print( "updating the location" );
            Log.d( "onlocationchanged", location.toString() );
        }
    }

    @Override
    public void onProviderEnabled( String provider ) {

    }

    @RequiresApi( api = Build.VERSION_CODES.M )


    //responsible for initilisation of the activity
    // onStart and onResume can be used too
    //Bundle stores the state of the activity which onCreate can use to create the new version of the activity


    /**
     * ending data collection and returning to the main page
     * @param v
     */
    public void end( View v ) {
        lm.removeUpdates( this );

        startActivity( new Intent( SensorActivity.this, MainActivity.class ) );
    }

    public void onPauseResumeButtonClick( View view ) {
        if( state == 0 ) {
            //super.onPause();
            lm.removeUpdates( this );

            Button pauseResumeButton = this.findViewById( R.id.pauseResumeButton );
            Button recordButton = this.findViewById( R.id.recordButton );
            TextView titleText = this.findViewById( R.id.titleTextView );
            TextView logTextView = this.findViewById( R.id.logTextView );

            pauseResumeButton.setText( "Resume" );
            recordButton.setText( "Resume" );
            titleText.setText( "PAUSED" );
            logTextView.setText( "Long Press Button to Stop" );

            state = 1;
        } else {
            this.onResumeLocationRecording();
        }
    }

    //
//    /**
//     * registered listener for the accelometer listener
//     */
    public void onResumeLocationRecording() {
        Button pauseResumeButton = this.findViewById( R.id.pauseResumeButton );
        Button recordButton = this.findViewById( R.id.recordButton );
        TextView titleText = this.findViewById( R.id.titleTextView );
        TextView logTextView = this.findViewById( R.id.logTextView );

        pauseResumeButton.setText( "Pause" );
        recordButton.setText( "Record" );
        titleText.setText( "Recording" );
        logTextView.setText( "" );

        //super.onResume();
        register();

        state = 0;
    }

    /**
     * Listener for long press of button to end the recording.
     */
    private final View.OnTouchListener stopRecordingOnTouchListener = new View.OnTouchListener() {
        private int delay = 0;

        @Override
        public boolean onTouch( View v, MotionEvent ev ) {
            if( ev.getAction() == MotionEvent.ACTION_DOWN ) {
                this.delay = 9;
            }

            if( state == SensorActivity.STATE_PAUSED ) {
                long downTime = (SystemClock.uptimeMillis() - ev.getDownTime());
                int dotCount = (int) downTime / 1000;

                if( downTime <= 200 ) {
                    // Do nothing
                    this.delay = 9;
                } else if( downTime < 3000 ) {
                    if( this.delay != dotCount ) {
                        TextView logTextView = SensorActivity.this.findViewById( R.id.logTextView );

                        char[] dots = new char[ dotCount ];
                        Arrays.fill( dots, '.' );

                        logTextView.setText( "Stopping" + new String( dots ) );
                        this.delay = dotCount;
                    }
                } else {
                    SensorActivity.this.finish();
                    return true;
                }
            }

            return false;
        }
    };

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

