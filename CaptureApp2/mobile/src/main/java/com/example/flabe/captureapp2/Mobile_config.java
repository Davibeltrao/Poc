package com.example.flabe.captureapp2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.capture.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flabe on 16/5/2018.
 */

public class Mobile_config extends AppCompatActivity {
    CheckBox acc;
    CheckBox lac;
    CheckBox bar;
    CheckBox gyr;
    CheckBox mag;
    CheckBox gps;

    Button save_config;

    Singleton s = Singleton.getInstance();
    private Map<String, Boolean> sensors_used = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_config);

        findViewsById();

        getBoxState();

        implementListeners();
    }

    private void getBoxState() {
        sensors_used = s.getMap();
        acc.setChecked(sensors_used.get("ACC"));
        lac.setChecked(sensors_used.get("LAC"));
        gyr.setChecked(sensors_used.get("GYR"));
        mag.setChecked(sensors_used.get("MAG"));
        bar.setChecked(sensors_used.get("BAR"));
        gps.setChecked(sensors_used.get("GPS"));
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
                s.setMap(sensors_used);
            }
        });
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
}
