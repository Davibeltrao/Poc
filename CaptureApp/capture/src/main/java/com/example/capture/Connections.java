package com.example.capture;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by flabe on 9/5/2018.
 */

public class Connections {

    Singleton s = Singleton.getInstance();

    public Connections(){}

    public class ServerClass extends Thread{

        public ArrayList<BluetoothSocket> sockets_list = new ArrayList<>();
        public ArrayList<String> devices = new ArrayList<String>();

        private BluetoothServerSocket serverSocket = null;

        public ServerClass(){

        }

        public void listenConn(int actual_index){
            BluetoothSocket socket = null;
            int index = 0;
            try {
                serverSocket = s.btAdapter.listenUsingRfcommWithServiceRecord("APP_NAME", s.myUUIDs.get(actual_index));
                socket = serverSocket.accept();

                if (socket != null) {
                    sockets_list.add(socket);

                    String address = socket.getRemoteDevice().getAddress();
                    devices.add(address);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        private ArrayList<BluetoothSocket> sockets_list = new ArrayList<>();

        public ClientClass(BluetoothDevice device1, UUID uuidToTry){
            device = device1;
            try {
                socket=device.createRfcommSocketToServiceRecord(uuidToTry);
            } catch (IOException e) {
                //handleMessage(STATE_CONNECTION_FAILED);
                e.printStackTrace();
            }

        }
        public void run(){
            try {
                socket.connect();
                //handleMessage(STATE_CONNECTED);
                sockets_list.add(socket);
                s.setSockets(sockets_list);
                s.clientConnected = 1;
//                Intent i = new Intent(connection.this, comm.class);
//                i.putExtra("SERVER_TYPE", type);
//                startActivity(i);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
