package com.example.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button on,off,list;
    TextView tv1,list_tv;
    Set<BluetoothDevice> ad;
    private static final int REQUEST_ENABLE_BLUTOOTH=2;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        on=(Button) findViewById(R.id.on_btn);
        off=(Button) findViewById(R.id.off_btn);
        list=(Button) findViewById(R.id.list_btn);
        tv1=(TextView) findViewById(R.id.textView2);
        list_tv=(TextView)findViewById(R.id.textView2);
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        if (adapter==null){
            Toast.makeText(this, "Blutooth is Not Supported!!!", Toast.LENGTH_SHORT).show();
        }
        on.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!adapter.isEnabled()){
                    Intent i=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(i,REQUEST_ENABLE_BLUTOOTH);
                }

            }
        });
        off.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
               Intent i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
               startActivity(i);

            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                StringBuilder sb=new StringBuilder();
                ad=adapter.getBondedDevices();
                for (BluetoothDevice temp:ad){
                    sb.append("\n"+temp.getName()+"\n");
                }
                list_tv.setText(sb.toString());
            }
        });
        /*ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN},
                REQUEST_BLUETOOTH_PERMISSION);*/
    }
}
