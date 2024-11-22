package com.falconjk.LocalChat;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

// DeviceDiscoveryService.java
public class DeviceDiscoveryService {
    private static final String TAG = "DeviceDiscovery";
    private static final int PORT = 53317;
    private static final String MULTICAST_ADDRESS = "224.0.0.167";
    private MulticastSocket socket;  // 改用 MulticastSocket
    private final String myFingerprint;
    private final String myAlias;
    private final DeviceDiscoveryListener listener;
    private final Handler mainHandler;
    private final Gson gson;
    private boolean isRunning;
    private WifiManager.MulticastLock multicastLock;

    public interface DeviceDiscoveryListener {
        void onDeviceFound(Device device);
    }

    public DeviceDiscoveryService(String alias, Context context, DeviceDiscoveryListener listener) {
        this.myAlias = alias;
        this.myFingerprint = UUID.randomUUID().toString();
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
        // 獲取多播鎖
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("deviceDiscoveryLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }

    public void startDiscovery() {
        isRunning = true;
        new Thread(() -> {
            try {
                // 創建 MulticastSocket 並加入多播組
                socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

                Log.d(TAG, "MulticastSocket created and joined group: " + MULTICAST_ADDRESS);

                // Start broadcasting presence
                broadcastPresence();

                // Listen for other devices
                while (isRunning) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try {
                        socket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        Log.d(TAG, "Received message: " + message);
                        Log.d(TAG, "From: " + receivePacket.getAddress().getHostAddress());

                        Device device = gson.fromJson(message, Device.class);

                        if (!device.getFingerprint().equals(myFingerprint)) {
                            device.setIpAddress(receivePacket.getAddress().getHostAddress());
                            mainHandler.post(() -> listener.onDeviceFound(device));
                        }
                    } catch (IOException e) {
                        if (!isRunning) break;
                        Log.e(TAG, "Error receiving packet: ", e);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error setting up MulticastSocket: ", e);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.leaveGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                    } catch (IOException e) {
                        Log.e(TAG, "Error leaving multicast group: ", e);
                    }
                    socket.close();
                }
            }
        }).start();
    }

    private void broadcastPresence() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Device myDevice = new Device(myAlias, myFingerprint, android.os.Build.MODEL, getLocalIpAddress());
                    String json = gson.toJson(myDevice);
                    byte[] sendData = json.getBytes();

                    InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendData,
                            sendData.length,
                            group,
                            PORT
                    );

                    Log.d(TAG, "Sending broadcast: " + json);
                    socket.send(sendPacket);

                    Thread.sleep(5000);
                } catch (Exception e) {
                    Log.e(TAG, "Error broadcasting presence: ", e);
                }
            }
        }).start();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, "Error getting IP address", ex);
        }
        return "Unknown";
    }

    public void stopDiscovery() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            } catch (IOException e) {
                Log.e(TAG, "Error leaving multicast group: ", e);
            }
            socket.close();
        }
        if (multicastLock != null && multicastLock.isHeld()) {
            multicastLock.release();
        }
    }
}
