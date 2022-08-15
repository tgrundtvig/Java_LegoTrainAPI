package org.abstractica.railroadapi.examples;

import org.abstractica.railroadapi.Locomotive;
import org.abstractica.railroadapi.RailroadAPI;
import org.abstractica.railroadapi.Switch;
import org.abstractica.railroadapi.impl.RailroadAPIImpl;

import java.net.SocketException;
import java.net.UnknownHostException;

public class FirstLocomotiveTest
{
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException
	{
		RailroadAPI api = new RailroadAPIImpl();
		Locomotive loc1 = api.createLocomotive("Locomotive 1",10849186);
		api.start();
		api.waitForAllDevicesToConnect();
		while(true)
		{
			loc1.moveAndWait(Locomotive.Direction.FORWARD,100, 5);
			Thread.sleep(5000);
			loc1.moveAndWait(Locomotive.Direction.BACKWARD,100, 5);
			Thread.sleep(5000);
			loc1.identify(2);
			Thread.sleep(5000);
		}
	}
}
