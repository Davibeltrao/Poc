package com.example.flabe.captureapp2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.capture.Communication;
import com.example.capture.Connections;
import com.example.capture.Sensor.FileWriter;
import com.example.capture.Sensor.SensorActivity;
import com.example.capture.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by flabe on 16/5/2018.
 */

public class Capture extends AppCompatActivity {

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int START = 13;
    static final int STOP = 14;

    EditText editText;
    TextView textView;
    Spinner location_spinner;
    Spinner side_spinner;
    Spinner wear_spinner;
    Button start;
    Button connect;

    Singleton s = Singleton.getInstance();
    MessagesClass messenger;
    SensorData sensorData;

    String state = "Start";

    //private MessagesClass m;
    private BluetoothAdapter myBluetoothAdapter;
    private Connections c;
    private Connections.ServerClass serverClass;
    private ArrayList<BluetoothSocket> sockets;
    private Map<String, Boolean> sensors_use;
    private Map<String, Boolean> mobile_sensors;

    FileWriter.ExternalFileWriter InternalWriterAcc;
    FileWriter.ExternalFileWriter InternalWriterLac;
    FileWriter.ExternalFileWriter InternalWriterGyr;
    FileWriter.ExternalFileWriter InternalWriterBar;
    FileWriter.ExternalFileWriter InternalWriterMag;

    Long startTime;
    Long systemTime;
    Long change;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture);

        findViewsById();

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        s = Singleton.getInstance();
        s.setBtAdapter(myBluetoothAdapter);

        if(c == null) {
            c = new Connections();
            serverClass = c. new ServerClass();
            s.setConnection(c);
            s.setServerClass(serverClass);
        }
        else {
            c = s.getConnection();
            serverClass = s.getServerClass();
        }
        messenger = (MessagesClass)s.getCommunicator();
        if(messenger != null){
            connect.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);
        }

        sensorData = new SensorData(this);

        systemTime = System.currentTimeMillis();
        startTime = SystemClock.elapsedRealtimeNanos()/1000000;
        change = systemTime - startTime;

        implementListeners();
    }

    private void findViewsById() {
        editText = findViewById(R.id.editText);

        start = findViewById(R.id.start);
        start.setVisibility(View.INVISIBLE);

        connect = findViewById(R.id.connect);

        textView = findViewById(R.id.textView2);
        textView.setGravity(Gravity.CENTER);

        location_spinner = findViewById(R.id.location_spinner);
        implementSpinners(0);
        side_spinner = findViewById(R.id.side_spinner);
        implementSpinners(1);
        wear_spinner = findViewById(R.id.wear_spinner);
        implementSpinners(2);
    }

    private void implementSpinners(int spinner) {
        List<String> list = new ArrayList<String>();
        switch(spinner){
            case 0:
                list.add("Front Pocket");
                list.add("Back Pocket");
                break;
            case 1:
                list.add("Right");
                list.add("Left");
                break;
            case 2:
                list.add("Right");
                list.add("Left");
                break;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(spinner == 0){
            location_spinner.setAdapter(dataAdapter);
        }else if(spinner == 1){
            side_spinner.setAdapter(dataAdapter);
        }else if(spinner == 2){
            wear_spinner.setAdapter(dataAdapter);
        }
        //list.clear();
    }

    private void implementListeners(){
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals("Start")) {
                    messenger.write("INPUT\n".getBytes());
                    startCapture();
                }else if(state.equals("Stop")){
                    messenger.write("STOP CAPTURE".getBytes());
                    stopCapture();
                }

            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect.setVisibility(View.INVISIBLE);
                Handler threadHandler = new Handler();
                threadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        serverClass.listenConn(0);
                        //connect.setVisibility(View.VISIBLE);
                        connectSocket();
                        //getAvaiableSensors();
                        //handleMessage(STATE_CONNECTED);
                        //listen.setVisibility(View.VISIBLE);
                        //ChangeTextView(msg_box, "Waiting Connections...");
                    }
                }, 200);
            }
        });
    }

    private void stopCapture(){
        handleMessage(STOP);
        sensorData.unregisterListeners();
        if(mobile_sensors.get("ACC") == true){
            InternalWriterAcc.closeOutput();
        }if(mobile_sensors.get("LAC") == true){
            InternalWriterAcc.closeOutput();
        }if(mobile_sensors.get("GYR") == true){
            InternalWriterAcc.closeOutput();
        }if(mobile_sensors.get("MAG") == true){
            InternalWriterAcc.closeOutput();
        }if(mobile_sensors.get("BAR") == true){
            InternalWriterAcc.closeOutput();
        }if(mobile_sensors.get("GPS") == true){
        //    InternalWriterAcc.closeOutput();
        }
    }

    private void startCapture(){
        handleMessage(START);
        String name = "NAME:" + editText.getText().toString() + "\n";
        String wear_pos = "POS:" + wear_spinner.getSelectedItem().toString() + "\n";
        messenger.write(name.getBytes());
        messenger.write(wear_pos.getBytes());
        sensorsToUse();
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                textView.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                textView.setText("Started!");
                messenger.write("START CAPTURE".getBytes());
                //messenger.write("")
                init();
            }
        }.start();
    }

    private void init() {
        mobile_sensors = s.getMap();
        String name = editText.getText().toString();
        String mobile_pos = location_spinner.getSelectedItem().toString();
        String side_spin = side_spinner.getSelectedItem().toString();
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        if(mobile_sensors.get("ACC") == true){
            InternalWriterAcc = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_acc.txt"), this);
        }if(mobile_sensors.get("LAC") == true){
            InternalWriterLac = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_lac.txt"), this);
        }if(mobile_sensors.get("GYR") == true){
            InternalWriterGyr = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_gyr.txt"), this);
        }if(mobile_sensors.get("MAG") == true){
            InternalWriterMag = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_mag.txt"), this);
        }if(mobile_sensors.get("BAR") == true){
            InternalWriterBar = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_bar.txt"), this);
        }if(mobile_sensors.get("GPS") == true){
            //InternalWriterGPS = new FileWriter.ExternalFileWriter((name + "_mobile_" + mobile_pos + "_" + side_spin + "_" + ts + "_gps.txt"), this);
        }
        sensorData.registerListenersThread(mobile_sensors.get("ACC"), mobile_sensors.get("LAC"), mobile_sensors.get("GYR"), mobile_sensors.get("MAG"), mobile_sensors.get("BAR"), false);
    }

    private void sensorsToUse() {
        sensors_use = s.getWearMap();
        messenger.write(("ACC" + ":" + sensors_use.get("ACC").toString() + "\n").getBytes());
        messenger.write(("LAC" + ":" + sensors_use.get("LAC").toString() + "\n").getBytes());
        messenger.write(("GYR" + ":" + sensors_use.get("GYR").toString() + "\n").getBytes());
        messenger.write(("MAG" + ":" + sensors_use.get("MAG").toString() + "\n").getBytes());
        messenger.write(("BAR" + ":" + sensors_use.get("BAR").toString() + "\n").getBytes());
        messenger.write(("GPS" + ":" + sensors_use.get("GPS").toString() + "\n").getBytes());
    }

    private void connectSocket() {
        sockets = s.getSockets();
        Log.wtf("Size", Integer.toString(sockets.size()));
        messenger = new MessagesClass(sockets.get(0));
        messenger.start();
        handleMessage(20);
        s.setCommunicator(messenger);
    }

    public void handleMessage(int STATE){
        Message message = Message.obtain();
        message.what = STATE;
        handler.sendMessage(message);
    }

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg){
            switch (msg.what){
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer= (byte[])msg.obj;
                    String tempMsg=new String(readBuffer, 0,msg.arg1);
                    Log.wtf("Msg CRIADA NOVINHA EM FOLHA PORRA", tempMsg);
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    if(tempMsg.contains("Stopped")){
                        start.setText("Start");
                        state = "Start";
                    }
                    break;
                case 20:
                    connect.setVisibility(View.GONE);
                    start.setVisibility(View.VISIBLE);
                    textView.setText("CONNECTED!");
                    break;
                case START:
                    start.setText("Stop");
                    state = "Stop";
                    break;
                case STOP:
                    start.setText("Start");
                    state = "Start";
                    break;
            }
            return true;
        }
    });

    public class MessagesClass extends Communication.SendReceive {

        public MessagesClass(BluetoothSocket socket) {
            super(socket);
        }

        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;
            while(true){
                try {
                    bytes = inputStream.read(buffer);
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
            //Log.wtf("BAR X", Float.toString(x));
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
