package com.bluesat.eva;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


//Used for receiving notifications from the SensorManager when there is new sensor data.
public class MainActivity extends AppCompatActivity {


    //Button pause;


    @Override
    public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        Thread.setDefaultUncaughtExceptionHandler( new ErrorHandler( this ) );
    }

    public void start( View v ) {
        startActivity( new Intent( MainActivity.this, SensorActivity.class ) );
    }


    /**
     * will be called when there is new data: will also read the same data but with different timestamp
     * TODO: read the data into a csv file
     * @param event
     */
}
