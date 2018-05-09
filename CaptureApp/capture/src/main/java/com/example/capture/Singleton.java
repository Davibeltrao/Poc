package com.example.capture;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.telephony.SmsMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by flabe on 9/5/2018.
 */

public class Singleton {

    public int clientConnected = 0;
    ArrayList<UUID> myUUIDs = new ArrayList<>();
    BluetoothAdapter btAdapter;

    private ArrayList<BluetoothSocket> sockets_list;

    private static final Singleton ourInstance = new Singleton();

    public static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
        myUUIDs.add(UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b"));
        myUUIDs.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        myUUIDs.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        myUUIDs.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        myUUIDs.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
    }

    public ArrayList<UUID> getUUIDs(){
        return this.myUUIDs;
    }

    public void setBtAdapter(BluetoothAdapter adapter){
        this.btAdapter = adapter;
    }

    public BluetoothAdapter getBtAdapter(){
        return this.btAdapter;
    }

    public void setSockets(ArrayList<BluetoothSocket> sockets){
        this.sockets_list = sockets;
    }

    public ArrayList<BluetoothSocket> getSockets(){
        return this.sockets_list;
    }
}
