package com.example.capture;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by flabe on 9/5/2018.
 */

public class Communication {

    public Communication(){

    }

    public static class SendReceive extends Thread {
        public BluetoothSocket bluetoothSocket;
        public InputStream inputStream;
        public OutputStream outputStream;
        ArrayList<BluetoothSocket> btSockets = new ArrayList<BluetoothSocket>();

        public SendReceive(BluetoothSocket socket) {
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
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void disconnect(){
            try {
                inputStream.close();
                outputStream.close();
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
