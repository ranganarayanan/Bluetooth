package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bluetooth.adapter.MessageAdapter;
import com.example.bluetooth.model.MessageModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    CardView send;
    RecyclerView recyclerView;
    EditText editText;
    LinearLayoutManager layoutManager;
    List<MessageModel> messageModels=new ArrayList<>();
    MessageAdapter messageAdapter;
    private Handler mHandler;
    private static final int MESSAGE_READ = 1;
    ChatThread chatThread;
    public static BluetoothSocket socket;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window=getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        send=(CardView) findViewById(R.id.send_cv);
        editText=(EditText) findViewById(R.id.et);
        recyclerView=(RecyclerView) findViewById(R.id.rv);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter=new MessageAdapter(messageModels,this);
        recyclerView.setAdapter(messageAdapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendMsg=editText.getText().toString();
                chatThread.write(sendMsg.getBytes());
                messageModels.add(new MessageModel(sendMsg,"w"));
                messageAdapter.notifyDataSetChanged();
            }
        });
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        messageModels.add(new MessageModel(readMessage,"r"));
                        messageAdapter.notifyDataSetChanged();
                        // Process the read message
                        break;
                }
                return true;
            }
        });
        chatThread=new ChatThread(socket);
        chatThread.start();
    }
    private class ChatThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ChatThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                // Handle exception
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    // Handle exception
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                // Handle exception
            }
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