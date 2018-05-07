package com.example.flabe.pocapp;

import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

/**
 * Created by flabe on 7/5/2018.
 */

public class Singleton {
    private static Singleton instance = null;
    private int teste = 0;
    private ArrayList<BluetoothSocket> sockets_list;

    protected Singleton() {}

    public static Singleton getInstance() {
        if(instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public void setSockets(ArrayList<BluetoothSocket> sockets){
        this.sockets_list = sockets;
    }

    public ArrayList<BluetoothSocket> getSockets(){
        return this.sockets_list;
    }
}
