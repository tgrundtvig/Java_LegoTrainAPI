package org.abstractica.railroadapi.examples;

import org.abstractica.railroadapi.RailroadAPI;
import org.abstractica.railroadapi.impl.RailroadAPIImpl;

import java.net.SocketException;
import java.net.UnknownHostException;

public class FirstTest
{
	public static void main(String[] args) throws SocketException, UnknownHostException
	{
		RailroadAPI api = new RailroadAPIImpl();
		api.start();
	}
}
