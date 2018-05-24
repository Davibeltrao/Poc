package com.example.capture.Sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by flabe on 10/5/2018.
 */

public class SensorActivity extends Service implements SensorEventListener {

    public SensorManager manager;

    private HandlerThread mSensorThreadAcc = null;
    private HandlerThread mSensorThreadLac = null;
    private HandlerThread mSensorThreadGyr = null;
    private HandlerThread mSensorThreadMag = null;
    private HandlerThread mSensorThreadBar = null;
    //private HandlerThread mSensorThreadGps = null;
    private Handler mSensorHandler;

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
        }else if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            getLinearAcceleration(event);
        }else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            getGyroscope(event);
        }else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            getMagnetometer(event);
        }else if(sensor.getType() == Sensor.TYPE_PRESSURE) {
            getBarometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerListeners(){
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void registerListenersThread(boolean acc, boolean lac,boolean gyr, boolean mag, boolean bar, boolean gps){
        if(acc){startAcc();}
        if(lac){startLac();}
        if(gyr){startGyr();}
        if(mag){startMag();}
        if(bar){startBar();}
        if(gps){startGps();}
    }

    public void startAcc(){
        mSensorThreadAcc = new HandlerThread("Acc");
        mSensorThreadAcc.start();
        mSensorHandler = new Handler(mSensorThreadAcc.getLooper());
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10000, mSensorHandler);
    }

    public void startLac() {
        mSensorThreadLac = new HandlerThread("Lac");
        mSensorThreadLac.start();
        mSensorHandler = new Handler(mSensorThreadLac.getLooper());
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 10000, mSensorHandler);
    }

    public void startGyr(){
        mSensorThreadGyr = new HandlerThread("Gyr");
        mSensorThreadGyr.start();
        mSensorHandler = new Handler(mSensorThreadGyr.getLooper());
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 10000, mSensorHandler);
    }

    public void startBar() {
        mSensorThreadBar = new HandlerThread("Bar");
        mSensorThreadBar.start();
        mSensorHandler = new Handler(mSensorThreadBar.getLooper());
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_PRESSURE), 10000, mSensorHandler);
    }

    public void startMag(){
        mSensorThreadMag = new HandlerThread("Mag");
        mSensorThreadMag.start();
        mSensorHandler = new Handler(mSensorThreadMag.getLooper());
        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 10000, mSensorHandler);
    }

    public void startGps(){
        //mSensorThread = new HandlerThread("Gyr", Thread.MAX_PRIORITY);
        //mSensorThread.start();
        //mSensorHandler = new Handler(mSensorThread.getLooper());
        //this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_), SensorManager.SENSOR_DELAY_GAME, mSensorHandler);
    }


    public void getAccelerometer(SensorEvent event){
    }
    public void getGyroscope(SensorEvent event){
    }
    public void getMagnetometer(SensorEvent event){
    }
    public void getBarometer(SensorEvent event){
    }
    public void getLinearAcceleration(SensorEvent event) {
    }

    public void unregisterListeners(){
        if(mSensorThreadAcc != null) {
            mSensorThreadAcc.quitSafely();
        }if(mSensorThreadGyr != null){
            mSensorThreadGyr.quitSafely();
        }if(mSensorThreadBar != null){
            mSensorThreadBar.quitSafely();
        }if(mSensorThreadLac != null){
            mSensorThreadLac.quitSafely();
        }if(mSensorThreadMag != null){
            mSensorThreadMag.quitSafely();
        }
        this.manager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
