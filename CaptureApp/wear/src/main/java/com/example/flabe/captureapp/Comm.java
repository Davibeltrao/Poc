package com.example.flabe.captureapp;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.capture.Communication;
import com.example.capture.Singleton;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by flabe on 9/5/2018.
 */

public class Comm extends WearableActivity {

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private Button start;

    Singleton s;
    MessagesClass m;

    ArrayList<BluetoothSocket> sockets;

    Button send;
    TextView textView;

    int msg_send = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_communication);
        s = s.getInstance();

        findViewByIds();
        connectSockets();
        implementListeners();
    }

    public void findViewByIds(){
        textView = (TextView)findViewById(R.id.textView);
        send = (Button)findViewById(R.id.send);
    }

    public void implementListeners(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string= "Value: " + Integer.toString(msg_send);
                msg_send++;
                m.write(string.getBytes());
            }
        });
    }


    private void connectSockets() {
        sockets = s.getSockets();

        for(int i = 0; i < sockets.size(); i++){
             m = new MessagesClass(sockets.get(i));
             m.start();
         //   messageThreads.add(m);
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
                    textView.setText(tempMsg);
                    //textView.setText("message received");
                    break;
                case 10:
                    //textView.setText(Integer.toString(teste));
                    //teste++;
                    break;
                case 11:
                    break;
                case 12:
                    textView.setText("SUCESSO");
                    break;
                case 13:
                    textView.setText("FRACASSO");
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

}
