package com.example.flabe.pocapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


/**
 * Created by flabe on 6/5/2018.
 */

public class connection extends Activity {

    Singleton s;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;

    String type = "";

    ArrayList<String> devices = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    int actual_index = 0;
    int msg_send = 0;

    BluetoothAdapter myBluetoothAdapter;
    BluetoothDevice[] btArray;

    ListView listView;
    TextView textView;
    TextView msg_box;
    Button listen;
    Button send;
    Button start;

    ServerClass serverClass;

    ArrayList<BluetoothSocket> sockets_list = new ArrayList<>();

    private static final String APP_NAME = "BluetoothComm";
    ArrayList<UUID> mUuids;

    int teste  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connections);

        s = s.getInstance();

        Intent intent = getIntent();

        type = intent.getStringExtra(MainActivity.SERVER_TYPE);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        findViewByIds();

        textView.setText(type);

        mUuids = new ArrayList<UUID>();
        mUuids.add(UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b"));
        mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));

        implementListeners();
    }

    public void findViewByIds() {
        listView = (ListView) findViewById(R.id.listView);
        textView = findViewById(R.id.textView_new);
        listen = (Button)findViewById(R.id.listen);
        send = (Button)findViewById(R.id.send);
        start = (Button)findViewById(R.id.start);

        msg_box = (TextView)findViewById(R.id.msg_box);
        msg_box.setGravity(Gravity.FILL_HORIZONTAL);
    }

    public void ChangeTextView(TextView view, String msg){
        view.setText(msg);
    }

    public void implementListeners() {
        if (type.equals("Slave")) {
            ChangeTextView(msg_box, "Connect to a Server...");
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
            listen.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
        }

        if (type.equals("Master")){
            serverClass=new ServerClass();
            ChangeTextView(msg_box, "Waiting Connections...");
            ChangeTextView(textView, "Listening Connections");

            adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devices);

            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    textView.setText("Connecting");
                    for(int i = 0; i < 4; i++) {
                        ClientClass clientClass = new ClientClass(btArray[position], mUuids.get(i));
                        clientClass.start();
                    }
                }
                catch (Exception e){
                    textView.setText("Failed");
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeTextView(msg_box, "Listening...");
                listen.setVisibility(View.INVISIBLE);

                //RUN THIS IN THREAD TO LET LISTEN BUTTON BE SET TO INVISIBLES (WAIT 200 millis)
                Handler threadHandler = new Handler();
                threadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        serverClass.listenConn();
                        actual_index++;
                    }
                }, 200);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(connection.this, comm.class);
                i.putExtra("SERVER_TYPE", type);
                s.setSockets(sockets_list);
                startActivity(i);
            }
        });
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

    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket = null;

        public ServerClass(){

        }

        public void listenConn(){
            BluetoothSocket socket = null;
            int index = 0;
            try {
                serverSocket = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, mUuids.get(actual_index));
                socket = serverSocket.accept();

                if (socket != null) {
                    sockets_list.add(socket);

                    String address = socket.getRemoteDevice().getAddress();
                    devices.add(address);
                    adapter.notifyDataSetChanged();

                    handleMessage(STATE_CONNECTED);
                    listen.setVisibility(View.VISIBLE);
                }

            } catch (IOException e) {
                handleMessage(STATE_CONNECTION_FAILED);
                e.printStackTrace();
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1, UUID uuidToTry){
            device = device1;
            try {
                socket=device.createRfcommSocketToServiceRecord(uuidToTry);
            } catch (IOException e) {
                handleMessage(STATE_CONNECTION_FAILED);
                e.printStackTrace();
            }

        }
        public void run(){
            try {
                socket.connect();
                handleMessage(STATE_CONNECTED);

                sockets_list.add(socket);
                s.setSockets(sockets_list);

                Intent i = new Intent(connection.this, comm.class);
                i.putExtra("SERVER_TYPE", type);
                startActivity(i);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
