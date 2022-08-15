package org.abstractica.railroadapi.examples;

import org.abstractica.railroadapi.RailroadAPI;
import org.abstractica.railroadapi.Switch;
import org.abstractica.railroadapi.impl.RailroadAPIImpl;

import java.net.SocketException;
import java.net.UnknownHostException;

public class FirstSwitchTest
{
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException
	{
		RailroadAPI api = new RailroadAPIImpl();
		Switch sw1 = api.createSwitch("Switch 1", Switch.Side.RIGHT,664219);
		api.start();
		api.waitForAllDevicesToConnect();
		while(true)
		{
			System.out.println("sw1.switchAndWait(Switch.Side.RIGHT)...");
			sw1.switchAndWait(Switch.Side.RIGHT);
			System.out.println("done!");
			Thread.sleep(5000);
			System.out.println("sw1.switchAndWait(Switch.Side.LEFT)...");
			sw1.switchAndWait(Switch.Side.LEFT);
			System.out.println("done!");
			Thread.sleep(5000);
			System.out.println("sw1.identify(2);");
			sw1.identify(2);
			System.out.println("done!");
			Thread.sleep(5000);
		}
	}
}
