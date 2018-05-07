package com.example.flabe.pocapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static String SERVER_TYPE = "";
    Button master;
    Button slave;
    Singleton s;

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

    private void implementListeners(){
        master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Master";
                Intent i = new Intent(MainActivity.this, connection.class);
                i.putExtra(SERVER_TYPE, message);
                startActivity(i);
            }
        });

        slave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Slave";
                Intent i = new Intent(MainActivity.this, connection.class);
                i.putExtra(SERVER_TYPE, message);
                startActivity(i);
            }
        });
    }

}
