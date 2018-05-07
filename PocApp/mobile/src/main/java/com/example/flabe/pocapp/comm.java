package com.example.flabe.pocapp;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by flabe on 7/5/2018.
 */

public class comm extends Activity {

    Singleton s;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    String type = "";

    Button send;
    TextView msg_box;
    TextView textView;

    int teste = 0;
    int msg_send = 0;

    ArrayList<BluetoothSocket> sockets;

    SendReceive sendReceive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication);

        findViewByIds();

        s = s.getInstance();

        Intent intent = getIntent();
        type = intent.getStringExtra("SERVER_TYPE");

        textView.setText(type);

        connectSockets();

        implementListeners();
    }

    public void findViewByIds() {
        send = (Button)findViewById(R.id.send);
        msg_box = (TextView)findViewById(R.id.msg_box);
        textView = (TextView)findViewById(R.id.textView_new);
    }

    public void implementListeners() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string= "Value: " + Integer.toString(msg_send);
                msg_send++;
                if (type.equals("Master")) {
                    //SEND MESSAGE TO ALL SLAVES
                } else {
                    //SEND MESSAGE TO MASTER
                    sendReceive.write(string.getBytes());
                }
            }
        });
    }

    public void connectSockets(){
        sockets = s.getSockets();
        for(int i = 0; i < sockets.size(); i++){
            sendReceive = new SendReceive(sockets.get(i));
            sendReceive.start();
        }
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
                    textView.setText("message received");
                    break;
                case 10:
                    textView.setText(Integer.toString(teste));
                    teste++;
                    break;
                case 11:
                    break;
            }
            return true;
        }
    });

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        ArrayList<BluetoothSocket> btSockets = new ArrayList<BluetoothSocket>();

        public SendReceive(BluetoothSocket socket){
            bluetoothSocket = socket;
            btSockets.add(bluetoothSocket);

            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream=tempIn;
            outputStream=tempOut;
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
