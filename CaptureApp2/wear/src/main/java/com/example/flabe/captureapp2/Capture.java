package com.example.flabe.captureapp2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.capture.Communication;
import com.example.capture.Sensor.FileWriter;
import com.example.capture.Sensor.SensorActivity;
import com.example.capture.Singleton;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by flabe on 17/5/2018.
 */

public class Capture extends WearableActivity {
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int START = 11;
    static final int STOP = 12;
    static final int TIMER = 13;

    String subject_name = "";
    String wear_side = "";
    boolean acc, lac, gyr, mag, bar, gps = false;

    Button start;
    EditText clock;

    Singleton s = Singleton.getInstance();
    MessagesClass m;
    SensorData sensorData;

    FileWriter.ExternalFileWriter InternalWriterAcc;
    FileWriter.ExternalFileWriter InternalWriterLac;
    FileWriter.ExternalFileWriter InternalWriterGyr;
    FileWriter.ExternalFileWriter InternalWriterBar;
    FileWriter.ExternalFileWriter InternalWriterMag;

    //FileWriter.InternalFileWriter InternalWriterGPS;

    String state = "Start";
    Long startTime;
    Long systemTime;
    Long change;
    Long firstTimer;
    int timer = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);

        // Enables Always-on
        setAmbientEnabled();

        start = findViewById(R.id.start);
        clock = findViewById(R.id.clock);

        implementListeners();

        //Log.wtf("SIZE", String.valueOf(s.getSockets().size()));
        m = new MessagesClass(s.getSockets().get(0));
        m.start();

        sensorData = new SensorData(this);

        //Log.wtf("NANO3", String.valueOf(SystemClock.))
        //Log.wtf("REAL TIME",)
        systemTime = System.currentTimeMillis();
        startTime = SystemClock.elapsedRealtimeNanos()/1000000;
        change = systemTime - startTime;
    }

    private void implementListeners() {
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals("Stop")) {
                    stopCollectingData();
                }else if(state.equals("Start")){

                }
            }
        });
    }

    public void handleMessage(int STATE){
        Message message = Message.obtain();
        message.what = STATE;
        handler3.sendMessage(message);
    }

    Handler handler3= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg){
            switch (msg.what){
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer= (byte[])msg.obj;
                    String tempMsg=new String(readBuffer, 0,msg.arg1);
                    Log.wtf("Mobile messageeeeeeeeee", tempMsg);
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    checkCondition(tempMsg);
                    break;
                case START:
                    start.setText("STOP");
                    state = "Stop";
                    break;
                case STOP:
                    start.setText("START");
                    state = "Start";
                    break;
                case TIMER:
                    clock.setText(String.valueOf(timer));
                    break;
            }
            return true;
        }
    });

    private void checkCondition(String tempMsg){
        Log.wtf("O QUE VEIO", tempMsg);
        //Check if message is configuration
        if(tempMsg.contains("NAME") || tempMsg.contains("WEAR") || tempMsg.contains("INPUT")) {
            getInputConfiguration(tempMsg);
        }

        //Always check for Sensor Changes
        checkSensorToUse(tempMsg);

        //Check if message is to STOP CAPTURE
        if(tempMsg.contains("STOP CAPTURE")){
            stopCollectingData();
        }

        //Check if message is to START CAPTURE
        if(tempMsg.contains("START CAPTURE")){
            Log.wtf("BORA CAPTURAR", "WOOOOWWW");
            startCapture();
        }
    }

    private void checkSensorToUse(String msg) {
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();

        String[] lines = msg.split("\n");
        for (String line : lines) {
            Log.wtf("SPLIT", line);
            if(line.contains("ACC:")){
                acc = Boolean.valueOf(line.substring(4));
                InternalWriterAcc = new FileWriter.ExternalFileWriter((subject_name + "_wear_" + wear_side + "_" + ts + "_acc.txt"), this);
            }if(line.contains("LAC:")) {
                lac = Boolean.valueOf(line.substring(4));
                InternalWriterLac = new FileWriter.ExternalFileWriter((subject_name + "_wear_" + wear_side + "_" + ts + "_lac.txt"), this);
            }if(line.contains("GYR:")){
                gyr = Boolean.valueOf(line.substring(4));
                InternalWriterGyr = new FileWriter.ExternalFileWriter((subject_name + "_wear_" + wear_side + "_" + ts + "_gyr.txt"), this);
            }if(line.contains("MAG:")){
                mag = Boolean.valueOf(line.substring(4));
                InternalWriterMag = new FileWriter.ExternalFileWriter((subject_name + "_wear_" + wear_side + "_" + ts + "_mag.txt"), this);
            }if(line.contains("BAR:")){
                bar = Boolean.valueOf(line.substring(4));
                InternalWriterBar = new FileWriter.ExternalFileWriter((subject_name + "_wear_" + wear_side + "_" + ts + "_bar.txt"), this);
            }if(line.contains("GPS:")){
                gps = Boolean.valueOf(line.substring(4));
            }
        }
    }

    private void getInputConfiguration(String msg){
        Log.wtf("CONFIGURATION", msg);
        String[] lines = msg.split("\n");
        for (String line : lines) {
            //Log.wtf("SPLIT", line);
            if(line.contains("NAME:")){
                subject_name = line.substring(5);
              //  Log.wtf("SUBJECT", subject_name);
            }
            if(line.contains("POS:")){
                wear_side = line.substring(4);
             //   Log.wtf("POS", wear_side);
            }
        }
    }

    private void startCapture() {
        handleMessage(START);
        Log.wtf("SENSOR-ACC", String.valueOf(acc));
        Log.wtf("SENSOR-LAC", String.valueOf(lac));
        Log.wtf("SENSOR-GYR", String.valueOf(gyr));
        Log.wtf("SENSOR-MAG", String.valueOf(mag));
        Log.wtf("SENSOR-BAR", String.valueOf(bar));
        Log.wtf("SENSOR-GPS", String.valueOf(gps));
        firstTimer = System.currentTimeMillis();
        timer = 0;
        sensorData.registerListenersThread(acc, lac, gyr, mag, bar, gps);
        if(acc) {
            //InternalWriterAcc.openOutput();
        }if(lac){
            //InternalWriterLac.openOutput();
        }if(gyr){
            //InternalWriterGyr.openOutput();
        }if(mag){
            //InternalWriterMag.openOutput();
        }if(bar){
            //InternalWriterBar.openOutput();
        }
    }

    private void stopCollectingData() {
        handleMessage(STOP);
        m.write("Stopped".getBytes());
        sensorData.unregisterListeners();
        if(acc) {
            //InternalWriterAcc.readFile();
            InternalWriterAcc.closeOutput();
        }if(lac){
            //InternalWriterLac.readFile();
            InternalWriterLac.closeOutput();
        }if(gyr){
            //InternalWriterGyr.readFile();
            InternalWriterGyr.closeOutput();
        }if(mag){
            //InternalWriterMag.readFile();
            InternalWriterMag.closeOutput();
        }if(bar){
            //InternalWriterBar.readFile();
            InternalWriterBar.closeOutput();
        }
    }

    private class MessagesClass extends Communication.SendReceive {

        public MessagesClass(BluetoothSocket socket) {
            super(socket);
        }

        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;
            while(true){
                try {
                    //Log.wtf("entrei", "NO LUGAR DE DEUS POR FAVOR!");
                    bytes = inputStream.read(buffer);
                    handler3.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                System.out.println("bytes" + bytes);
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void changeTime() {
        handleMessage(TIMER);
    }

    private class SensorData extends SensorActivity {

        public SensorData(Context c) {
            super(c);
        }

        public void getAccelerometer(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            y = values[1];
            z = values[2];
            time = event.timestamp;
            Long eventTime = change + time/1000000;

             if((eventTime - firstTimer)/1000 > timer){
                Log.wtf("NEW TIME", String.valueOf(timer));
                timer++;
                changeTime();
            }
            InternalWriterAcc.writeToFile(Float.toString(x), Float.toString(y), Float.toString(z), String.valueOf(eventTime));
        }

        public void getGyroscope(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            y = values[1];
            z = values[2];
            time = event.timestamp;
            Long eventTime = change + time/1000000;
//            Log.wtf("GYRO X", Float.toString(x));
//            Log.wtf("GYRO Y", Float.toString(y));
//            Log.wtf("GYRO Z", Float.toString(z));
            InternalWriterGyr.writeToFile(Float.toString(x), Float.toString(y), Float.toString(z), String.valueOf(eventTime));
        }

        public void getBarometer(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            time = event.timestamp;
            Long eventTime = change + time/1000000;
           // Log.wtf("BAR X", Float.toString(x));
            InternalWriterBar.writeBarToFile(Float.toString(x), String.valueOf(eventTime));
        }

        public void getMagnetometer(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            y = values[1];
            z = values[2];
            time = event.timestamp;
            Long eventTime = change + time/1000000;
//            Log.wtf("MAG X", Float.toString(x));
//            Log.wtf("MAG Y", Float.toString(y));
//            Log.wtf("MAG Z", Float.toString(z));
            InternalWriterMag.writeToFile(Float.toString(x), Float.toString(y), Float.toString(z), String.valueOf(eventTime));
        }

        public void getLinearAcceleration(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            y = values[1];
            z = values[2];
            time = event.timestamp;
            Long eventTime = change + time/1000000;
//            Log.wtf("LAC X", Float.toString(x));
//            Log.wtf("LAC Y", Float.toString(y));
//            Log.wtf("LAC Z", Float.toString(z));
            InternalWriterLac.writeToFile(Float.toString(x), Float.toString(y), Float.toString(z), String.valueOf(eventTime));
        }
    }
}
