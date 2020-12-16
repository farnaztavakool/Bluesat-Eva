package com.bluesat.eva;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


//Used for receiving notifications from the SensorManager when there is new sensor data.
public class SensorActivity extends AppCompatActivity  implements SensorEventListener {

    private  SensorManager sm;
    private  Sensor accel;
    private float x;
    private float y;
    private float z;
    private TextView tv;
    private int state;


    @Override
    public void onCreate ( Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this,accel,SensorManager.SENSOR_DELAY_NORMAL);      //value of normal delay
        state = 0;



    }


    public void end(View v) {
        sm.unregisterListener(this);
        startActivity(new Intent(SensorActivity.this, MainActivity.class));

    }

    /**
     * will be called when there is new data: will also read the same data but with different timestamp
     * TODO: read the data into a csv file
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i("Data",event.toString());
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        File file = new File("/Users/farnaz/Desktop/append.txt");
        try {
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(event.toString());
            br.close();
        } catch(IOException e) {
            System.out.println();
        }
//        tv.setText(String.valueOf(x));  //converting an integer to a string
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * pausing interaction with the activity
     */
    public void onPause(View view) {
        if (state == 0) {
            super.onPause();
            sm.unregisterListener(this);
            ((Button) findViewById(R.id.pause)).setText("Resume");
            state = 1;
            return;
        }
        onResume();

    }

    /**
     * registered listener for the accelometer listener
     */
    public void onResume() {
        super.onResume();
        sm.registerListener(this,accel,SensorManager.SENSOR_DELAY_NORMAL);      //value of normal delay
        ((Button) findViewById(R.id.pause)).setText("Pause");
        state = 0;

    }

    public void export(View v) {
        //
    }
}

