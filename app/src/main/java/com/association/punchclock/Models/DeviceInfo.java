package com.association.punchclock.Models;

public class DeviceInfo {
    private int id;
    private String SerialNumber;
    private String IpAddress;
    private boolean isActive;

    public int getId() {
        return id;
    }

    public String getIpAddress() {
        return IpAddress;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setIpAddress(String ipAddress) {
        IpAddress = ipAddress;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }
}
