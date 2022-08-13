package org.abstractica.railroadapi.impl;

import org.abstractica.deviceserver.Device;
import org.abstractica.deviceserver.DeviceServer;
import org.abstractica.deviceserver.DeviceServerListener;
import org.abstractica.deviceserver.impl.DeviceServerImpl;
import org.abstractica.railroadapi.Locomotive;
import org.abstractica.railroadapi.RailroadAPI;
import org.abstractica.railroadapi.Switch;

import java.net.SocketException;
import java.net.UnknownHostException;

public class RailroadAPIImpl implements RailroadAPI, DeviceServerListener
{
	private final static int SERVER_PORT = 3377;
	private final DeviceServer deviceServer;

	public RailroadAPIImpl() throws SocketException, UnknownHostException
	{
		deviceServer = new DeviceServerImpl(SERVER_PORT,
								1024,
									10,
								1000,
										this);
	}

	@Override
	public Locomotive createLocomotive(String name, long deviceId)
	{
		Device device = deviceServer.createDevice(deviceId, "LEGO_Locomotive", 1);
		LocomotiveImpl locomotive = new LocomotiveImpl(name, device);
		device.setPacketHandler(locomotive);
		return locomotive;
	}

	@Override
	public Switch createSwitch(String name, Switch.Side type, long deviceId)
	{
		String deviceType = type == Switch.Side.LEFT ? "LEGO_Left_Switch" : "LEGO_Right_Switch";
		Device device = deviceServer.createDevice(deviceId, deviceType, 1);
		SwitchImpl railSwitch = new SwitchImpl(name, type, device);
		device.setPacketHandler(railSwitch);
		return railSwitch;
	}

	@Override
	public void start()
	{
		deviceServer.start();
	}

	@Override
	public void waitForAllDevicesToConnect() throws InterruptedException
	{
		deviceServer.waitForAllDevicesToConnect();
	}

	@Override
	public void stop() throws InterruptedException
	{
		deviceServer.stop();
	}

	@Override
	public boolean acceptAndInitializeNewDevice(Device device)
	{
		System.out.println("Unknown device trying to connect: " + device);
		return false;
	}

	@Override
	public void onDeviceAdded(Device device)
	{

	}

	@Override
	public void onDeviceRemoved(Device device)
	{

	}
}
