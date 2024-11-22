package com.falconjk.locatchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        DeviceDiscoveryService.DeviceDiscoveryListener,
        ChatService.ChatListener {
    private final String TAG = "MainActivity";
    private static final long DEVICE_TIMEOUT = 15000;
    private Handler cleanupHandler;
    private final Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            deviceAdapter.removeOfflineDevices(DEVICE_TIMEOUT);
            cleanupHandler.postDelayed(this, 5000); // 每5秒檢查一次
        }
    };

    private DeviceDiscoveryService discoveryService;
    private ChatService chatService;
    private DeviceAdapter deviceAdapter;
    private MessageAdapter messageAdapter;
    private Device currentChatDevice;
    private String myAlias;
    private String myId;

    private RecyclerView deviceList;
    private RecyclerView chatMessages;
    private LinearLayout chatContainer;
    private TextView tvChatTitle;
    private EditText etMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化視圖
        initializeViews();

        // 設置我的設備信息
        myId = UUID.randomUUID().toString();
        myAlias = "User-" + myId.substring(0, 4);

        // 初始化服務
        chatService = new ChatService(this);
        discoveryService = new DeviceDiscoveryService(myAlias, getApplicationContext(), this);

        // 啟動服務
        chatService.start();
        discoveryService.startDiscovery();

        // 初始化清理處理器
        cleanupHandler = new Handler(Looper.getMainLooper());
        cleanupHandler.postDelayed(cleanupRunnable, 5000);
    }

    private void initializeViews() {
        deviceList = findViewById(R.id.deviceList);
        chatMessages = findViewById(R.id.chatMessages);
        chatContainer = findViewById(R.id.chatContainer);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // 設置RecyclerView
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        chatMessages.setLayoutManager(new LinearLayoutManager(this));

        deviceAdapter = new DeviceAdapter();
        messageAdapter = new MessageAdapter();

        deviceList.setAdapter(deviceAdapter);
        chatMessages.setAdapter(messageAdapter);

        // 設置點擊事件
        deviceAdapter.setOnDeviceClickListener(device -> {
            currentChatDevice = device;
            tvChatTitle.setText("正在與 " + device.getAlias() + " 聊天");
            chatContainer.setVisibility(View.VISIBLE);
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        Log.d(TAG, "sendMessage: ");
        if (!content.isEmpty() && currentChatDevice != null) {
            Message message = new Message(myId, myAlias, content, Message.MessageType.SENT);
            chatService.sendMessage(currentChatDevice.getIpAddress(), message);
            Log.d(TAG, "ChatDevice IP: " + currentChatDevice.getIpAddress());
            messageAdapter.addMessage(message);
            etMessage.setText("");
            chatMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDeviceFound(Device device) {
        device.setLastUpdateTime(System.currentTimeMillis());
        deviceAdapter.addDevice(device);
    }

    @Override
    public void onMessageReceived(Message message) {
        messageAdapter.addMessage(message);
        if (currentChatDevice == null)
            tvChatTitle.setText("正在與 " + message.getSenderAlias() + " 聊天");
        chatMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatService != null) {
            chatService.stop();
        }

        if (discoveryService != null) {
            discoveryService.stopDiscovery();
        }

        // 移除清理任務
        if (cleanupHandler != null) {
            cleanupHandler.removeCallbacks(cleanupRunnable);
        }
    }
}

