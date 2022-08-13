package org.abstractica.railroadapi;

public interface RailroadAPI
{
    Locomotive createLocomotive(String name, long deviceId);
    Switch createSwitch(String name, Switch.Side type, long deviceId);
    void start();
    void waitForAllDevicesToConnect() throws InterruptedException;
    void stop() throws InterruptedException;
}
