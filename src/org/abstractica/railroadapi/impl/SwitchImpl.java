package org.abstractica.railroadapi.impl;

import org.abstractica.deviceserver.Device;
import org.abstractica.deviceserver.DevicePacketHandler;
import org.abstractica.deviceserver.Response;
import org.abstractica.railroadapi.Switch;

public class SwitchImpl implements Switch, DevicePacketHandler
{

	private static final int STATE_LEFT = 0;
	private static final int STATE_RIGHT = 1;
	private static final int STATE_SWITCHING_LEFT = 2;
	private static final int STATE_SWITCHING_RIGHT = 3;

	//Commands for switches
	private final int COMMAND_SWITCH_TO_LEFT = 1500;
	private final int COMMAND_SWITCH_TO_RIGHT = 1501;
	private final int COMMAND_SWITCH_GET_STATE = 1502;
	private final int COMMAND_SWITCH_ON_STATE_CHANGE = 2500;


	private final Device device;

	private final String name;
	private final Side type;
	private final Object stateLock;
	private volatile int state = -1;

	public SwitchImpl(String name, Side type, Device device)
	{
		this.name = name;
		this.type = type;
		this.device = device;
		stateLock = new Object();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Side getType()
	{
		return type;
	}

	@Override
	public boolean switchTo(Side side) throws InterruptedException
	{
		int iCmd = side == Switch.Side.LEFT ? COMMAND_SWITCH_TO_LEFT : COMMAND_SWITCH_TO_RIGHT;
		Response response = device.sendPacket(iCmd, 0, 0, null, true, false);
		if (response == null) return false;
		return response.getResponse() == 0;
	}

	@Override
	public boolean switchAndWait(Side side) throws InterruptedException
	{
		if (!switchTo(side))
		{
			return false;
		}
		waitFor(side);
		return true;
	}

	@Override
	public void waitFor(Side side) throws InterruptedException
	{
		synchronized(stateLock)
		{
			if (side == Side.LEFT)
			{
				while (state != STATE_LEFT)
				{
					stateLock.wait();
				}
			} else
			{
				while (state != STATE_RIGHT)
				{
					stateLock.wait();
				}
			}
		}
	}

	@Override
	public Side waitForSwitch() throws InterruptedException
	{
		synchronized(stateLock)
		{
			while (state != STATE_LEFT && state != STATE_RIGHT)
			{
				stateLock.wait();
			}
			return state == STATE_LEFT ? Side.LEFT : Side.RIGHT;
		}
	}

	@Override
	public int onPacket(int command, int arg1, int arg2, byte[] load)
	{
		if(command == COMMAND_SWITCH_ON_STATE_CHANGE)
		{
			updateState(arg1);
			return 0;
		}
		throw new IllegalStateException("Unknown command: " + command);
	}

	private void updateState(int state)
	{
		synchronized(stateLock)
		{
			this.state = state;
			if (state == STATE_LEFT || state == STATE_RIGHT)
			{
				stateLock.notifyAll();
			}
		}
	}
}
