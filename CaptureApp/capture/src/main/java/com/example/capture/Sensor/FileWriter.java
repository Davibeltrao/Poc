package com.example.capture.Sensor;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by flabe on 12/5/2018.
 */

public class FileWriter {

    public static class ExternalFileWriter{

        public ExternalFileWriter(String filename){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Teste";
            File file = new File(path, "/myData.txt");
        }

    }

    public static class InternalFileWriter{

        FileOutputStream fos = null;
        FileInputStream fis = null;
        Context context;
        String filename = "";
        public InternalFileWriter(String filename, Context c){
            try {
                fos = c.openFileOutput(filename, 0);
                fis = c.openFileInput(filename);
                context = c;
                this.filename = filename;
            }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }

        public void closeOutput(){
            try {
                fos.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void openOutput(){
            try {
                fos = context.openFileOutput(filename, 0);
                fis = context.openFileInput(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeToFile(String x, String y, String z, String time){
            try {
                Log.wtf("X", x);
                Log.wtf("Y", y);
                Log.wtf("Z", z);
                Log.wtf("Time", time);
                fos.write(x.getBytes());
                fos.write(" ".getBytes());
                fos.write(y.getBytes());
                fos.write(" ".getBytes());
                fos.write(z.getBytes());
                fos.write("\n".getBytes());
                fos.write(time.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void readFile(){
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            try {
                while((text = br.readLine()) != null){
                    Log.wtf("Text", text);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
