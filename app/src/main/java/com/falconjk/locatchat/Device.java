package com.falconjk.LocalChat;

public class Device {
    private String alias;
    private String fingerprint;
    private String deviceModel;
    private String deviceType;
    private int port;
    private String protocol;
    private String ipAddress;
    private long lastUpdateTime;

    public Device() {
    }

    public Device(String alias, String fingerprint, String deviceModel, String ipAddress) {
        this.alias = alias;
        this.fingerprint = fingerprint;
        this.deviceModel = deviceModel;
        this.ipAddress = ipAddress;
        this.port = 53317;
        this.protocol = "http";
        this.deviceType = "mobile";
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
