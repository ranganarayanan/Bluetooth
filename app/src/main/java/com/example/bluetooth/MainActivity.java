package com.example.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
   // Button listen, showlist;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String NAME = "BluetoothChat";
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    List<BluetoothDevice> deviceList=new ArrayList<>();
    List<String> nameList=new ArrayList<>();
    TextView statusTxt;
    ListView lv;
    ArrayAdapter adapter;
    CardView listen, showlist;

    @SuppressLint({"MissingInflatedId", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window=getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        listen = (CardView) findViewById(R.id.listen_cv);
        showlist = (CardView) findViewById(R.id.showlist_cv);
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        statusTxt=(TextView)findViewById(R.id.status_txt);
        lv=(ListView) findViewById(R.id.lv);
        if(!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startClientThread(position);
            }
        });
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusTxt.setText("Listening...");
                startServerThread();
            }
        });

        showlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> devices= mBluetoothAdapter.getBondedDevices();
                for(BluetoothDevice device: devices) {
                    if(device.getName().startsWith("Tech"))
                        continue;
                    deviceList.add(device);
                    nameList.add(device.getName());
                }
                adapter=new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_spinner_item,nameList);
                lv.setAdapter(adapter);
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        nameList.add("Received:" +readMessage);
                        adapter.notifyDataSetChanged();
                        // Process the read message
                        break;
                }
                return true;
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }
    private void startServerThread() {
        ServerThread serverThread = new ServerThread();
        serverThread.start();
    }

    private void startClientThread(int position) {
        ClientThread clientThread=new ClientThread(deviceList.get(position));
        clientThread.start();
        // Start your client thread here
    }

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        @SuppressLint("MissingPermission")
        public ServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                // Handle exception
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    // Handle exception
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTxt.setText("Connection Failed");
                        }
                    });
                    break;
                }
                if (socket != null) {
                    // Start the chat thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            statusTxt.setText("Connected");
                        }
                    });
                    MainActivity2.socket=socket;
                    goToChatActivity();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        // Handle exception
                        break;
                    }
                    break;
                }
            }
        }
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                // Handle exception
            }
        }
    }

    private void goToChatActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });
    }


    private static final int MESSAGE_READ = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private class ClientThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        @SuppressLint("MissingPermission")
        public ClientThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                // Handle exception
            }
            mmSocket = tmp;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                // Handle exception
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        @SuppressLint("MissingPermission")
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTxt.setText("Connected");
                    }
                });
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    // Handle exception
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTxt.setText("Connection failed");
                        }
                    });
                }
                return;
            }

            // Connection established, start the chat
            MainActivity2.socket=mmSocket;
            goToChatActivity();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                // Handle exception
            }
        }
    }
}

