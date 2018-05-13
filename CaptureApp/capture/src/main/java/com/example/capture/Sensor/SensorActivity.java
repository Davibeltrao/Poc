package com.example.capture.Sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by flabe on 10/5/2018.
 */

public class SensorActivity implements SensorEventListener{

    private SensorManager manager;
    public float x;
    public float y;
    public float z;
    public long time;

    public SensorActivity(Context c){
        this.manager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.wtf("DEBUG","GYR\n");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerListeners(){
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void getAccelerometer(SensorEvent event){
    }

    public void unregisterListeners() {
        this.manager.unregisterListener(this);
    }
}
