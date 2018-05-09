package com.example.flabe.captureapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public static String SERVER_TYPE = "";
    Button master;
    Button slave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewByIds();
        implementListeners();
    }
    private void findViewByIds() {
        master = (Button)findViewById(R.id.master);
        slave = (Button)findViewById(R.id.slave);
    }

    private void implementListeners() {
        master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Master";
                Intent i = new Intent(MainActivity.this, Connection.class);
                i.putExtra(SERVER_TYPE, message);
                startActivity(i);
            }
        });

        slave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Slave";
                Intent i = new Intent(MainActivity.this, Connection.class);
                i.putExtra(SERVER_TYPE, message);
                startActivity(i);
            }
        });
    }

}
