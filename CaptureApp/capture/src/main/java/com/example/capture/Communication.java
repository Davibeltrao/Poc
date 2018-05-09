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
        public final BluetoothSocket bluetoothSocket;
        public final InputStream inputStream;
        public final OutputStream outputStream;
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
    }
}
