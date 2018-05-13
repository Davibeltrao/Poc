package com.example.flabe.captureapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capture.Connections;
import com.example.capture.Singleton;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by flabe on 9/5/2018.
 */

public class Connection extends Activity{
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

    ArrayList<BluetoothSocket> sockets_list = new ArrayList<>();

    ArrayList<UUID> mUuids;

    Connections c;
    Connections.ServerClass serverClass;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connections);

        findViewByIds();

        s = s.getInstance();
        c = new Connections();
        Intent intent = getIntent();

        mUuids = s.getUUIDs();

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        s.setBtAdapter(myBluetoothAdapter);

        type = intent.getStringExtra(MainActivity.SERVER_TYPE);
        textView.setText(type);

        implementListeners();
    }

    private void findViewByIds() {
        listView = (ListView) findViewById(R.id.listView);
        textView = findViewById(R.id.textView_new);
        listen = (Button)findViewById(R.id.listen);
        //send = (Button)findViewById(R.id.send);
        start = (Button)findViewById(R.id.start);

        msg_box = (TextView)findViewById(R.id.msg_box);
        msg_box.setGravity(Gravity.FILL_HORIZONTAL);
    }

    public void ChangeTextView(TextView view, String msg){
        view.setText(msg);
    }

    public void implementListeners(){
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
            serverClass= c.new ServerClass();

            ChangeTextView(msg_box, "Waiting Connections...");
            ChangeTextView(textView, "Listening...");

            adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, serverClass.devices);
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    textView.setText("Connecting");
                    for(int i = 0; i < 4; i++) {
                        Connections.ClientClass clientClass = c.new ClientClass(btArray[position], mUuids.get(i));
                        clientClass.start();

                        Handler threadHandler = new Handler();
                        threadHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(s.clientConnected == 1) {
                                    Intent intent = new Intent(Connection.this, Comm.class);
                                    intent.putExtra("SERVER_TYPE", type);
                                    startActivity(intent);
                                    s.clientConnected = 0;
                                }
                            }
                        }, 5000);
                    }
                }
                catch (Exception e){
                    textView.setText("Failed");
                }
                //Intent intent = new Intent(connection.this, comm.class);
                //intent.putExtra("SERVER_TYPE", type);
                //startActivity(intent);
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleMessage(STATE_LISTENING);
                ChangeTextView(msg_box, "Listening...");

                listen.setVisibility(View.INVISIBLE);
                //RUN THIS IN THREAD TO LET LISTEN BUTTON BE SET TO INVISIBLES (WAIT 200 millis)
                Handler threadHandler = new Handler();
                threadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        serverClass.listenConn(actual_index);
                        actual_index++;
                        adapter.notifyDataSetChanged();

                        handleMessage(STATE_CONNECTED);
                        listen.setVisibility(View.VISIBLE);
                        ChangeTextView(msg_box, "Waiting Connections...");
                    }
                }, 200);
                //////////////////////////////////////////////////////////////////////////////////
                //listen.setVisibility(View.VISIBLE);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("Master")) {
                    s.setSockets(serverClass.sockets_list);
                }
                Intent i = new Intent(Connection.this, Comm.class);
                i.putExtra("SERVER_TYPE", type);
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
                    textView.setText("Listening...");
                    break;
                case STATE_CONNECTING:
                    textView.setText("connecting");
                    break;
                case STATE_CONNECTED:
                    textView.setText("Connected!!");
                    break;
                case STATE_CONNECTION_FAILED:
                    textView.setText("failed");
                    break;
                case 10:
                    break;
                case 11:
                    break;
            }
            return true;
        }
    });
}
