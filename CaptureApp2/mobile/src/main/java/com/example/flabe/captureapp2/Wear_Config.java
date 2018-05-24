package com.example.flabe.captureapp2;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import com.example.capture.Singleton;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by flabe on 14/5/2018.
 */

public class Wear_Config extends AppCompatActivity {
    CheckBox acc;
    CheckBox lac;
    CheckBox bar;
    CheckBox gyr;
    CheckBox mag;
    CheckBox gps;

    Button save_config;

    Singleton s = Singleton.getInstance();

    private ArrayList<BluetoothSocket> sockets;
    private Map<String, Boolean> sensors_used;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_config);

        findViewsById();

        getBoxState();

        implementListeners();
    }

    private void getBoxState() {
        sensors_used = s.getWearMap();
        acc.setChecked(sensors_used.get("ACC"));
        lac.setChecked(sensors_used.get("LAC"));
        gyr.setChecked(sensors_used.get("GYR"));
        mag.setChecked(sensors_used.get("MAG"));
        bar.setChecked(sensors_used.get("BAR"));
        gps.setChecked(sensors_used.get("GPS"));
    }

    private void findViewsById() {
        acc = findViewById(R.id.Acc);
        lac = findViewById(R.id.Lacc);
        bar = findViewById(R.id.Bar);
        gyr = findViewById(R.id.gyr);
        mag = findViewById(R.id.Mag);
        gps = findViewById(R.id.gps);

        save_config = findViewById(R.id.save_config);
    }

    private void implementListeners() {
        save_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensors_used.put("ACC", acc.isChecked());
                sensors_used.put("LAC", lac.isChecked());
                sensors_used.put("GYR", gyr.isChecked());
                sensors_used.put("MAG", mag.isChecked());
                sensors_used.put("BAR", bar.isChecked());
                sensors_used.put("GPS", gps.isChecked());
                s.setwearMap(sensors_used);;
            }
        });
    }
}
