package com.example.flabe.captureapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
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

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private ListView listView;
    private Button start;

    Singleton s;
    Connections c;

    BluetoothAdapter myBluetoothAdapter;
    BluetoothDevice[] btArray;

    ArrayList<UUID> mUuids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        s = s.getInstance();
        c = new Connections();

        mTextView = (TextView) findViewById(R.id.text);

        mUuids = s.getUUIDs();
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Enables Always-on
        setAmbientEnabled();

        findViewByIds();

        implelentListeners();
    }

    public void findViewByIds(){
        listView = (ListView)findViewById(R.id.listView);
        start = (Button)findViewById(R.id.start);
    }

    public void implelentListeners(){
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
                    for(int i = 0; i < 4; i++) {
                        Connections.ClientClass clientClass = c.new ClientClass(btArray[position], mUuids.get(i));
                        clientClass.start();
                        Handler threadHandler = new Handler();
                        threadHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(s.clientConnected == 1) {
                                    Intent intent = new Intent(MainActivity.this, Comm.class);
                                    intent.putExtra("SERVER_TYPE", "Slave");
                                    startActivity(intent);
                                    s.clientConnected = 0;
                                }
                            }
                        }, 5000);
                    }
                }
                catch (Exception e){

                }
            }
        });
    }
}
