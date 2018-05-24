package com.example.capture;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by flabe on 9/5/2018.
 */

public class Singleton {

    private static final Singleton ourInstance = new Singleton();

    //CONNECTIONS
    private Connections c = null;
    private Connections.ServerClass serverClass = null;
    public int clientConnected = 0;

    //COMMUNICATION
    private Communication.SendReceive messenger = null;
    private ArrayList<BluetoothSocket> sockets_list = new ArrayList<>();

    //SENSORS
    private Map<String, Boolean> wearSensors = new HashMap<>();
    private Map<String,Boolean> sensors_used = new HashMap<>();

    ArrayList<UUID> myUUIDs = new ArrayList<>();
    BluetoothAdapter btAdapter;

    public static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
        myUUIDs.add(UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b"));

        initSensorMap();

        //myUUIDs.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        //myUUIDs.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        //myUUIDs.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        //myUUIDs.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
    }

    private void initSensorMap() {
        sensors_used.put("ACC", false);
        sensors_used.put("LAC", false);
        sensors_used.put("GYR", false);
        sensors_used.put("MAG", false);
        sensors_used.put("BAR", false);
        sensors_used.put("GPS", false);

        wearSensors.put("ACC", false);
        wearSensors.put("LAC", false);
        wearSensors.put("GYR", false);
        wearSensors.put("MAG", false);
        wearSensors.put("BAR", false);
        wearSensors.put("GPS", false);
    }

    //MESSENGER GETTER AND SETTER
    public void setCommunicator(Communication.SendReceive obj){ this.messenger = obj; }
    public Communication.SendReceive getCommunicator(){ return this.messenger; }

    //GET UUID Available
    public ArrayList<UUID> getUUIDs(){
        return this.myUUIDs;
    }

    //BLUETOOTH ADAPTER FUNCTIONS
    public void setBtAdapter(BluetoothAdapter adapter){
        this.btAdapter = adapter;
    }
    public BluetoothAdapter getBtAdapter(){
        return this.btAdapter;
    }

    //SOCKET SETTER AND GETTER
    public void setSockets(ArrayList<BluetoothSocket> sockets){
        this.sockets_list = sockets;
    }
    public ArrayList<BluetoothSocket> getSockets(){
        return this.sockets_list;
    }
    public Connections getConnection(){ return c; }
    public void setConnection(Connections con){ this.c = con; }
    public Connections.ServerClass getServerClass(){return serverClass;}
    public void setServerClass(Connections.ServerClass server){this.serverClass = server;}

    //SENSORS AVAILABLE GETTER AND SETTER
    //public ArrayList<Boolean> getAvailableSensors() { return this.wearSensors;   }
    //public void setAvailableSensors(ArrayList<Boolean> wearSensors){ this.wearSensors = wearSensors;  }


    //SENSOR USED GETTER AND SETTER
    public void setMap(Map<String,Boolean> map){ this.sensors_used = map; }
    public Map<String, Boolean> getMap(){ return this.sensors_used; }

    public void setwearMap(Map<String, Boolean>map){ this.wearSensors = map; }
    public Map<String, Boolean> getWearMap(){ return this.wearSensors; }
}
