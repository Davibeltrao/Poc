package com.example.flabe.captureapp;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.capture.Communication;
import com.example.capture.Communication.SendReceive;
import com.example.capture.Sensor.FileWriter.InternalFileWriter;
import com.example.capture.Sensor.SensorActivity;
import com.example.capture.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by flabe on 9/5/2018.
 */

public class Comm extends Activity {
    Singleton s;
    Communication c;
    SensorData sensorData;
    InternalFileWriter InternalWriter;

    int teste = 0;
    int msg_send = 0;
    int n = 0;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int START = 13;
    static final int STOP = 14;

    String type = "";
    String button_type = "Start";

    Button send;
    TextView msg_box;
    TextView textView;

    ArrayList<BluetoothSocket> sockets;

    ArrayList<MessagesClass> messageThreads = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication);

        findViewByIds();

        createObjects();

        //sensorData.registerListeners();

        Intent intent = getIntent();
        type = intent.getStringExtra("SERVER_TYPE");

        textView.setText(type);

        connectSockets();

        implementListeners();
    }

    private void createObjects(){
        s = s.getInstance();
        c = new Communication();
        sensorData = new SensorData(this);
        InternalWriter = new InternalFileWriter("SensorFile.txt", this);
    }

    private void findViewByIds() {
        send = (Button)findViewById(R.id.send);
        msg_box = (TextView)findViewById(R.id.msg_box);
        textView = (TextView)findViewById(R.id.textView_new);
    }

    private void connectSockets() {
        sockets = s.getSockets();

        for(int i = 0; i < sockets.size(); i++){
            MessagesClass m = new MessagesClass(sockets.get(i));
            m.start();
            messageThreads.add(m);
        }
    }

    public void implementListeners() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button_type.equals("Start")){
                    handleMessage(STOP);
                    new CountDownTimer(6000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            msg_box.setText("" + millisUntilFinished / 1000);
                            long t = millisUntilFinished/1000;
                            String timer = String.valueOf(t);
                            //here you can have your logic to set text to edittext
                            reSendMessage(timer);
                        }
                        public void onFinish() {
                            reSendMessage("START");
                            if(type.equals("Master")){
                                startCollectingData();
                            }
                        }
                    }.start();
                }
                if (button_type.equals("Stop")){
                    if(type.equals("Master")){
                        stopCollectingData();
                    }
                    reSendMessage("STOP");
                }
            }
        });
    }

    public void startCollectingData(){
        handleMessage(STOP);
        sensorData.registerListeners();
        InternalWriter.openOutput();
    }

    public void stopCollectingData(){
        handleMessage(START);
        sensorData.unregisterListeners();
        InternalWriter.readFile();
        InternalWriter.closeOutput();
    }

    public void reSendMessage(String message){
        if (type.equals("Master")) {
            //SEND MESSAGE TO ALL SLAVES
            for(int i = 0; i < messageThreads.size(); i++) {
                messageThreads.get(i).write(message.getBytes());
            }
        } else {
            //SEND MESSAGE TO MASTER
            messageThreads.get(0).write(message.getBytes());
        }
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
                case STATE_LISTENING:
                    textView.setText("listening");
                    break;
                case STATE_CONNECTING:
                    textView.setText("connecting");
                    break;
                case STATE_CONNECTED:
                    textView.setText("connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    textView.setText("failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer= (byte[])msg.obj;
                    String tempMsg=new String(readBuffer, 0,msg.arg1);
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    msg_box.setText(tempMsg);
                    if(type.equals("Master")){
                        if(tempMsg.equals("START")){
                            startCollectingData();
                        }
                        if(tempMsg.equals("STOP")){
                            stopCollectingData();
                        }
                        reSendMessage(tempMsg);
                    }
                    else{
                        if(tempMsg.equals("START")){
                            startCollectingData();
                        }
                        if(tempMsg.equals("STOP")){
                            stopCollectingData();
                        }
                    }
                    textView.setText("message received");
                    break;
                case 10:
                    textView.setText(Integer.toString(teste));
                    teste++;
                    break;
                case 11:
                    break;
                case 12:
                    msg_box.setText(Integer.toString(n));
                    n++;
                    textView.setText("SUCESSO");
                    break;
                case START:
                    button_type = "Start";
                    send.setText("START");
                    textView.setText(button_type);
                    break;
                case STOP:
                    button_type = "Stop";
                    send.setText("Stop");
                    textView.setText(button_type);
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


    private class SensorData extends SensorActivity{

        public SensorData(Context c) {
            super(c);
        }

        public void getAccelerometer(SensorEvent event){
            float[] values = event.values;
            x = values[0];
            y = values[1];
            z = values[2];
            time = event.timestamp;
            new Thread(new Runnable() {
                public void run(){
                    InternalWriter.writeToFile(Float.toString(x), Float.toString(y), Float.toString(z), String.valueOf(time));
                }
            }).start();
        }
    }
}
