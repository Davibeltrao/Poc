package com.example.flabe.captureapp2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capture.Communication;
import com.example.capture.Connections;
import com.example.capture.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends WearableActivity {

    static final int STATE_MESSAGE_RECEIVED = 5;

    private ListView listView;
    private Button start;
    private BluetoothAdapter myBluetoothAdapter;
    boolean acc;
    boolean lac;
    boolean gyr;
    boolean mag;
    boolean bar;
    boolean gps;

    Singleton s = Singleton.getInstance();
    Connections c = new Connections();
    MessagesClass m;

    ArrayList<Boolean> sensors = new ArrayList<Boolean>();
    Map<String,Boolean> sensorUse = new HashMap<>();

    BluetoothDevice[] btArray;
    ArrayList<UUID> mUuids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        //SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mUuids = s.getUUIDs();

        getSensors();

        findViewsById();

        implementListeners();
    }

    private void implementListeners() {
        Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
        btArray = new BluetoothDevice[bt.size()];
        String[] strings = new String[bt.size()];
        int index = 0;

        if (bt.size() > 0) {
            for (BluetoothDevice device : bt) {
                btArray[index] = device;
                strings[index] = device.getName();
                index++;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
            listView.setAdapter(arrayAdapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    //textView.setText("Connecting");
                    //for(int i = 0; i < 4; i++) {
                    Connections.ClientClass clientClass = c.new ClientClass(btArray[position], mUuids.get(0));
                    clientClass.start();
                    Handler threadHandler = new Handler();
                    threadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(s.clientConnected == 1){
                                Log.wtf("Socket_Size", Integer.toString(s.getSockets().size()));
                                Intent i = new Intent(MainActivity.this, Capture.class);
                                startActivity(i);
                                //m = new MessagesClass(s.getSockets().get(0));
                                //m.start();
                                //broadcastSensors();
                                //s.setCommunicator(m);
                            }
                        }
                    }, 2000);
                }
                catch (Exception e){

                }
            }
        });
    }

    private void broadcastSensors() {
        if(sensors.get(0) == false){
            m.write("ACC_FALSE".getBytes());
        }
        if(sensors.get(1) == false){
            m.write("GYR_FALSE".getBytes());
        }
        if(sensors.get(2) == false){
            m.write("MAG_FALSE".getBytes());
        }
        if(sensors.get(3) == false){
            m.write("BAR_FALSE".getBytes());
        }
        if(sensors.get(4) == false){
            m.write("GPS_FALSE".getBytes());
        }
    }

    private void findViewsById() {
        listView = findViewById(R.id.listView);
    }

    private void getSensors() {
        PackageManager PM = this.getPackageManager();

        acc = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        gyr = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        mag = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
        bar = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
        gps = PM.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        sensors.add(acc);
        Log.wtf("ACC", String.valueOf(acc));
        sensors.add(gyr);
        Log.wtf("GYR", String.valueOf(gyr));
        sensors.add(mag);
        Log.wtf("MAG", String.valueOf(mag));
        sensors.add(bar);
        Log.wtf("BAR", String.valueOf(bar));
        sensors.add(gps);
        Log.wtf("GPS", String.valueOf(gps));

        //s.setAvailableSensors(sensors);
    }

    private void checkSensorsUse(String message) {
        if(message.contains("ACC_CHECKED")){
            Log.wtf("ACC CHECKED", "TRUE");
            sensorUse.put("ACC", true);
        }else{
            Log.wtf("ACC UNCHECKED", "FALSE");
            sensorUse.put("ACC", false);
        }
        if(message.contains("GYR_CHECKED")){
            Log.wtf("GYR CHECKED", "TRUE");
            sensorUse.put("GYR", true);
        }else{
            Log.wtf("GYR UNCHECKED", "FALSE");
            sensorUse.put("GYR", false);
        }
        if(message.contains("MAG_CHECKED")){
            Log.wtf("MAG CHECKED", "TRUE");
        }else{
            Log.wtf("MAG UNCHECKED", "FALSE");
            sensorUse.put("MAG", false);
        }
        if(message.contains("BAR_CHECKED")){
            Log.wtf("BAR CHECKED", "TRUE");
            sensorUse.put("BAR", true);
        }else{
            Log.wtf("BAR_UNCHECKED", "FALSE");
            sensorUse.put("BAR", false);
        }
        if(message.contains("GPS_CHECKED")){
            Log.wtf("GPS CHECKED", "TRUE");
            sensorUse.put("GPS", true);
        }else{
            Log.wtf("GPS UNCHECKED", "FALSE");
            sensorUse.put("GPS", false);
        }
        s.setMap(sensorUse);
    }

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg){
            switch (msg.what){
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer= (byte[])msg.obj;
                    String tempMsg=new String(readBuffer, 0,msg.arg1);
                    Log.wtf("WEAR RECEIVED", tempMsg);

                    if(tempMsg.contains("START")){
                        //Intent i = new Intent(MainActivity.this, Capture.class);
                        //startActivity(i);
                    }
                    else{
                        if(tempMsg.contains("Send sensors")) {
                            broadcastSensors();
                        }
                        Long tsLong = System.currentTimeMillis();
                        String ts = tsLong.toString();
                        checkSensorsUse(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });


    private class MessagesClass extends Communication.SendReceive {

        public MessagesClass(BluetoothSocket socket) {
            super(socket);
        }

        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;
            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    Log.wtf("entrei", "MAIN_ACTIVITY");
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
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
}
