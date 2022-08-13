package org.abstractica.railroadapi.impl;

import org.abstractica.deviceserver.Device;
import org.abstractica.deviceserver.DevicePacketHandler;
import org.abstractica.deviceserver.Response;
import org.abstractica.railroadapi.Locomotive;

import java.util.ArrayList;
import java.util.Collection;

public class LocomotiveImpl implements Locomotive, DevicePacketHandler
{
	//Commands for locomotives
	private final static int COMMAND_IDENTIFY = 1000;
	private final static int COMMAND_MOVE_FORWARD = 1001;
	private final static int COMMAND_MOVE_BACKWARD = 1002;
	private final static int COMMAND_EMERGENCY_BRAKE = 1003;
	private final static int COMMAND_DISTANCE_TO_GOAL = 2000;

	private final Device device;

	private final String name;
	private volatile int distanceToGoal;
	private volatile boolean dtgOK;
	private final Object dtgLock;

	LocomotiveImpl(String name, Device device)
	{
		this.name = name;
		this.device = device;
		listeners = new ArrayList<>();
		distanceToGoal = 0;
		dtgOK = true;
		dtgLock = new Object();
	}

	private Collection<DistanceToGoalListener> listeners;

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean move(Direction dir, int maxSpeed, int magnets) throws InterruptedException
	{
		if(magnets < 0 || magnets > 65535 || maxSpeed < 0 || maxSpeed > 100)
		{
			throw new IllegalArgumentException("Illegal move command!");
		}
		dtgOK = false;
		int command = (dir == Locomotive.Direction.FORWARD) ? COMMAND_MOVE_FORWARD : COMMAND_MOVE_BACKWARD;
		Response response = device.sendPacket(command, maxSpeed, magnets,null, true, false);
		if(response == null) return false;
		return response.getResponse() == 0;
	}

	@Override
	public boolean moveAndWait(Direction dir, int maxSpeed, int magnets) throws InterruptedException
	{
		boolean res = move(dir, maxSpeed, magnets);
		if(res)
		{
			waitWhileDistanceToGoalGreaterThan(0);
		}
		return res;
	}

	@Override
	public boolean emergencyBrake() throws InterruptedException
	{
		Response response = device.sendPacket(COMMAND_EMERGENCY_BRAKE, 0, 0, null, true, false);
		if(response == null) return false;
		return response.getResponse() == 0;
	}

	@Override
	public int distanceToGoal() throws InterruptedException
	{
		synchronized(dtgLock)
		{
			while(!dtgOK)
			{
				dtgLock.wait();
			}
		}
		return distanceToGoal;
	}

	@Override
	public void waitWhileDistanceToGoalGreaterThan(int distance) throws InterruptedException
	{
		waitWhileDistanceToGoal(dtg -> dtg > distance);
	}

	@Override
	public void waitWhileDistanceToGoal(DistanceToGoalWaiter waiter) throws InterruptedException
	{
		if (waiter.keepWaiting(distanceToGoal))
		{
			DistanceToGoalListener listener = new DistanceToGoalListener(waiter);
			listeners.add(listener);
			listener.doWait();
		}
	}

	@Override
	public int onPacket(int command, int arg1, int arg2, byte[] load)
	{
		if(command == COMMAND_DISTANCE_TO_GOAL)
		{
			updateDistanceToGoal(arg1);
			return 0;
		}
		throw new IllegalStateException("Unknown command: " + command);
	}

	@Override
	public String toString()
	{
		return name;
	}

	private void updateDistanceToGoal(int distanceToGoal)
	{
		if (this.distanceToGoal != distanceToGoal)
		{
			this.distanceToGoal = distanceToGoal;
			onDistanceToGoalChange();
		}
		synchronized (dtgLock)
		{
			//System.out.println("dtgOK is true!");
			dtgOK = true;
			dtgLock.notifyAll();
		}
	}

	private void onDistanceToGoalChange()
	{
		ArrayList<DistanceToGoalListener> doomed = new ArrayList<>();
		for(DistanceToGoalListener listener : listeners)
		{
			if(!listener.onDistanceToGoalChange())
			{
				doomed.add(listener);
			}
		}
		listeners.removeAll(doomed);
	}

	private class DistanceToGoalListener
	{
		private final DistanceToGoalWaiter waiter;
		private boolean keepWaiting;

		private DistanceToGoalListener(DistanceToGoalWaiter waiter)
		{
			this.waiter = waiter;
			this.keepWaiting = true;
		}

		public synchronized void doWait() throws InterruptedException
		{
			keepWaiting = waiter.keepWaiting(distanceToGoal);
			while(keepWaiting)
			{
				wait();
			}
		}

		public synchronized boolean onDistanceToGoalChange()
		{
			keepWaiting = waiter.keepWaiting(distanceToGoal);
			if(!keepWaiting)
			{
				notifyAll();
			}
			return keepWaiting;
		}
	}
}
