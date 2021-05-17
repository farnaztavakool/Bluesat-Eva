package com.bluesat.eva;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


//Used for receiving notifications from the SensorManager when there is new sensor data.
public class SensorActivity extends AppCompatActivity implements LocationListener {

    private SensorManager sm;
    private Sensor accel;
    private float x;
    private float y;
    private float z;
    private TextView tv;

    private CharSequence logMessage = "";
    private CharSequence logStatusMessage = null;

    /*
    0 = Recording
    1 = Paused
     */
    private int state;
    LocationManager lm;

    private static final int STATE_RECORDING = 0;
    private static final int STATE_PAUSED = 1;
    private DBhelper db;

    //context provides information regarding different parts of the application
    Context mcontex;

    private Timer recordingBlinkTimer = null;

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

        db = new DBhelper( this );

        // Register long press listener
        this.findViewById( R.id.pauseResumeButton ).setOnTouchListener( this.stopRecordingOnTouchListener );
        this.findViewById( R.id.recordButton ).setOnTouchListener( this.stopRecordingOnTouchListener );

        this.startRecordingBlink();
    }

    public void register() {
        int coarseLocationPermission = ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION );
        if( coarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    99
            );
        }

        lm.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                100,
                0, this
        );
    }

    @Override
    public void onLocationChanged( @NonNull Location location ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( location.getTime() ) );
        int hr = cal.get( Calendar.HOUR_OF_DAY );
        int min = cal.get( Calendar.MINUTE );
        int sec = cal.get( Calendar.SECOND );
        String time = String.format("%02d:%02d:%02d", hr, min, sec);
        db.insertLocation( location.getAltitude(), location.getLongitude(), location.getLatitude(), time );

        this.displayLocationInformation(time, location );

        Log.d( "data", String.valueOf( location.getAltitude() ) );
        Log.e( "override", time );
    }

//    public void permission() {
//        // Storage Permissions
//        int REQUEST_EXTERNAL_STORAGE = 1;
//        String[] PERMISSIONS_STORAGE = {
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//        };
//
//        /**
//         * Checks if the app has permission to write to device storage
//         *
//         * If the app does not has permission then the user will be prompted to grant permissions
//         *
//         * @param activity
//         */
//
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE );
//
//        if( permission != PackageManager.PERMISSION_GRANTED ) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    this,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }

//    private void writeToFile( String data, String time, Context context ) {
//        try {
//
//            String path = context.getFilesDir().getAbsolutePath();
//
//
//            File root = new File( path );
//
//
//            boolean NEW_FILE = false;
//            File output = new File( path + "/config.txt" );
//            if( !output.exists() ) {
//                NEW_FILE = true;
//                output.createNewFile();
//            }
//
//            Log.e( "in write to file", output.getAbsolutePath() + " " + output.exists() );
//
//
//
//            BufferedWriter bf = new BufferedWriter( new FileWriter( output, true ) );
//            //if (NEW_FILE) bf.append()
//            bf.append( data );
//            bf.append( " " + time );
//            bf.newLine();
//            bf.close();
//
//            // OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
//            // outputStreamWriter.write(data);
//            // outputStreamWriter.close();
//        } catch( IOException e ) {
//            e.printStackTrace();
//            // Log.e("Exception", "File write failed: " + e.printStackTrace(););
//        }
//    }

    public void onRecordButtonClick( View view ) {
        if( this.state == SensorActivity.STATE_RECORDING ) {
            this.onLocationChanged( view );
        } else {
            this.onPauseResumeButtonClick( view );
        }
    }

    /**
     * TODO: find the code for requesting access to location
     *
     * @param view
     */
    public void onLocationChanged( View view ) {
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {


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

            Calendar cal = Calendar.getInstance();
            cal.setTime( new Date( location.getTime() ) );
            int hr = cal.get( Calendar.HOUR_OF_DAY );
            int min = cal.get( Calendar.MINUTE );
            int sec = cal.get( Calendar.SECOND );
            String time = String.format("%02d:%02d:%02d", hr, min, sec);
            db.insertLocation( location.getAltitude(), location.getLongitude(), location.getLatitude(), time );

            this.displayLocationInformation( time + " (snap)", location );

//            Double loc = (location.getAltitude());
//                writeToFile(location.toString(), String.valueOf(new Date(location.getTime()).getTime()), this);

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


            recordButton.setText( "Resume\n(hold)" );
            recordButton.setBackgroundColor( Color.GREEN );
            pauseResumeButton.setText( "Stop\n(hold)" );
            pauseResumeButton.setBackgroundColor( Color.RED );
            titleText.setText( "PAUSED" );

            String message;

            if( this.db.getNumberOfRecords() == 0 ) {
                message = "no record saved.";
            } else if(  this.db.getNumberOfRecords() == 1 ) {
                message = "1 record saved.";
            } else {
                message = String.format("%d records saved.", this.db.getNumberOfRecords());
            }

            this.setLogMessage( message + "\nTap for resume.\nHold for finish." );

            state = 1;

            this.stopRecordingBlink();
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

        pauseResumeButton.setText( "Pause\n(hold)" );
        pauseResumeButton.setBackgroundColor( 0xFFFFE900 );
        recordButton.setText( "Record current point" );
        recordButton.setBackgroundColor( 0xFFFFFFFF );
        titleText.setText( "Recording 🔴" );
        this.setLogMessage( "" );

        this.startRecordingBlink();

        //super.onResume();
        register();

        state = 0;
    }

    public void startRecordingBlink(){
        long startTime = System.currentTimeMillis();

        TimerTask timerTask = new TimerTask() {
            public void run() {
                String title;
                if( SensorActivity.this.state == SensorActivity.STATE_RECORDING ) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int dotCount = (int) (elapsedTime / 2000);

                    if( dotCount % 2 == 0 ) {
                        title = "Recording 🔴";
                    } else {
                        title= "Recording ⚫️";
                    }
                } else {
                    title = "PAUSED";
                }

                SensorActivity.this.runOnUiThread( ()->{
                    TextView titleText = SensorActivity.this.findViewById( R.id.titleTextView );
                    titleText.setText( title );
                } );

                if( SensorActivity.this.state == SensorActivity.STATE_PAUSED ) {
                    SensorActivity.this.recordingBlinkTimer.cancel();
                }
            }
        };

        if( this.recordingBlinkTimer != null ) {
            this.recordingBlinkTimer.cancel();
        }

        this.recordingBlinkTimer = new Timer( "RecordingBlink" );
        this.recordingBlinkTimer.schedule( timerTask, 500L, 100L );
    }

    private void stopRecordingBlink(){
        if( this.recordingBlinkTimer != null ) {
            this.recordingBlinkTimer.cancel();
            this.recordingBlinkTimer.purge();
        }

        SensorActivity.this.runOnUiThread( ()->{
            TextView titleText = SensorActivity.this.findViewById( R.id.titleTextView );
            titleText.setText( "PAUSED" );
        } );
    }

    private void displayLocationInformation( String time, @NonNull Location location ) {
        this.setLogMessage( String.format( Locale.UK, "%s\nLat %.02f\nLong %.02f\nAlt %.02f",
                time,
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude() )
        );
    }


    private int gps_signal_state = 0;

//    public void onGpsSignalClick( View v ) {
//        switch( this.gps_signal_state ) {
//            case 0:
//                this.setGpsSignalStrength( GpsSignalStrength.Good );
//                this.gps_signal_state = 1;
//                break;
//            case 1:
//                this.setGpsSignalStrength( GpsSignalStrength.Fair );
//                this.gps_signal_state = 2;
//                break;
//            case 2:
//                this.setGpsSignalStrength( GpsSignalStrength.Bad );
//                this.gps_signal_state = 0;
//                break;
//        }
//    }

    /**
     *
     */
//    private void setGpsSignalStrength( GpsSignalStrength strength ) {
//        ImageView gpsSignal = this.findViewById( R.id.gpsSignalImageView );
//
//        switch( strength ) {
//            case Bad:
//                gpsSignal.setImageResource( R.drawable.signal_red );
//                break;
//            case Fair:
//                gpsSignal.setImageResource( R.drawable.signal_orange );
//                break;
//            case Good:
//                gpsSignal.setImageResource( R.drawable.signal_green );
//                break;
//        }
//    }

    /**
     * Set the status message to display in the logTextView.
     *
     * @param message Status message.
     */
    private void setLogStatusMessage( CharSequence message ) {
        this.logStatusMessage = message;

        this.displayLogMessage();
    }

    /**
     * Set the log message.
     *
     * @param message Message
     */
    private void setLogMessage( CharSequence message ) {
        this.logMessage = message;

        this.displayLogMessage();
    }

    /**
     * Display string in the logTextView.
     */
    private void displayLogMessage() {
        this.runOnUiThread( () -> {
            TextView logTextView = this.findViewById( R.id.logTextView );

            if( this.logStatusMessage != null ) {
                logTextView.setText( this.logStatusMessage );
            } else {
                logTextView.setText( this.logMessage );
            }
        } );
    }

    private final void vibrateOnAction() {
        Vibrator v = (Vibrator) getSystemService( Context.VIBRATOR_SERVICE );
        // Vibrate for 500 milliseconds
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            v.vibrate( VibrationEffect.createOneShot( 500, VibrationEffect.DEFAULT_AMPLITUDE ) );
        } else {
            //deprecated in API 26
            v.vibrate( 500 );
        }
    }


    /**
     * Listener for long press of button to end the recording.
     */
    private final View.OnTouchListener stopRecordingOnTouchListener = new View.OnTouchListener() {
        private Timer timer = null;
        private long startTime = 0;
        private boolean isUp = false;

        @Override
        public boolean onTouch( View v, MotionEvent ev ) {
            switch( ev.getAction() ) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.isUp = true;
                    if( this.timer != null ) {
                        this.timer.cancel();
                    }

                    SensorActivity.this.setLogStatusMessage( null );

                    long downTime = System.currentTimeMillis() - startTime;
                    if( SensorActivity.this.state == SensorActivity.STATE_RECORDING && v.getId() == R.id.recordButton ) {
                        // Manually record the location.
                        SensorActivity.this.vibrateOnAction();

                        v.performClick();
                        return true;
                    } else if( downTime <= 300 ) {
                        if( SensorActivity.this.state == SensorActivity.STATE_PAUSED ) {
                            // Resume the auto record

                            //SensorActivity.this.vibrateOnAction();

                            //v.performClick();
                            //return true;
                        } else {
                            if( v.getId() == R.id.pauseResumeButton ) {
                                return true;
                            } else {
                                // Manual record
                                SensorActivity.this.vibrateOnAction();

                                v.performClick();
                                return true;
                            }
                        }
                    } else if( downTime > 3000 ) {
                        if( SensorActivity.this.state == SensorActivity.STATE_PAUSED ) {
                            if( v.getId() == R.id.recordButton ) {
                                // Resume the activity
                                v.performClick();
                                return true;
                            } else {
                                // Close the activity
                                SensorActivity.this.finish();
                                return true;
                            }
                        } else {
                            if( v.getId() == R.id.pauseResumeButton ) {
                                // Pause the auto recording

                                v.performClick();
                                return true;
                            } else {
                                // Manually record the location

                                SensorActivity.this.vibrateOnAction();

                                v.performClick();
                                return true;
                            }
                        }
                    }

                    return true;
                case MotionEvent.ACTION_DOWN:
                    TimerTask timerTask = null;

                    if( SensorActivity.this.state == SensorActivity.STATE_PAUSED ) {
                        timerTask = new TimerTask() {
                            public void run() {
                                if( isUp ) {
                                    if( timer != null ) {
                                        timer.cancel();
                                    }
                                    return;
                                }

                                long downTime = System.currentTimeMillis() - startTime;
                                int dotCount = (int) (downTime / 1000);

                                if( 200 < downTime && downTime <= 3000 ) {
                                    char[] dots = new char[ dotCount ];
                                    Arrays.fill( dots, '.' );

                                    if( v.getId() == R.id.recordButton ) {
                                        SensorActivity.this.setLogStatusMessage( "Resuming" + new String( dots ) );
                                    } else {
                                        SensorActivity.this.setLogStatusMessage( "Stopping" + new String( dots ) );
                                    }
                                } else if( downTime > 3000 ) {
                                    if( v.getId() == R.id.recordButton ) {
                                        SensorActivity.this.setLogStatusMessage( "Resumed" );
                                    } else {
                                        SensorActivity.this.setLogStatusMessage( "Stopped" );
                                    }


                                    SensorActivity.this.vibrateOnAction();

                                    if( timer != null ) {
                                        timer.cancel();
                                    }
                                }
                            }
                        };
                    } else {
                        if( v.getId() == R.id.pauseResumeButton ) {
                            timerTask = new TimerTask() {
                                public void run() {
                                    if( isUp ) {
                                        if( timer != null ) {
                                            timer.cancel();
                                        }
                                        return;
                                    }

                                    long downTime = System.currentTimeMillis() - startTime;
                                    int dotCount = (int) (downTime / 1000);

                                    if( 200 < downTime && downTime <= 3000 ) {
                                        char[] dots = new char[ dotCount ];
                                        Arrays.fill( dots, '.' );

                                        SensorActivity.this.setLogStatusMessage( "Pausing" + new String( dots ) );
                                    } else if( downTime > 3000 ) {
                                        SensorActivity.this.setLogStatusMessage( "Paused" );

                                        SensorActivity.this.vibrateOnAction();

                                        if( timer != null ) {
                                            timer.cancel();
                                        }
                                    }
                                }
                            };
                        }
                    }

                    if( this.timer != null ) {
                        this.timer.cancel();
                    }

                    if( timerTask == null ) {
                        return false;
                    }

                    this.isUp = false;
                    this.startTime = System.currentTimeMillis();

                    this.timer = new Timer( "LongPressTimer" );
                    this.timer.schedule( timerTask, 200L, 100L );
                    break;
            }

            return false;
        }
    };
}

