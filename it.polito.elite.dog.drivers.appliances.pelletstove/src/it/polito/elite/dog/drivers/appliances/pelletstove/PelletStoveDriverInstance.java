/*
 * Dog 3.1 - Dumb Pellet Stove Driver
 * 
 * 
 * Copyright 2014 Dario Bonino 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package it.polito.elite.dog.drivers.appliances.pelletstove;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.model.devicecategory.PelletHeater;
import it.polito.elite.dog.core.library.model.state.HeaterState;
import it.polito.elite.dog.core.library.model.state.StandByOnOffState;
import it.polito.elite.dog.core.library.model.statevalue.CoolingStateValue;
import it.polito.elite.dog.core.library.model.statevalue.FireUpStateValue;
import it.polito.elite.dog.core.library.model.statevalue.HeatingStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OffStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OnStateValue;
import it.polito.elite.dog.core.library.model.statevalue.StandByStateValue;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * @author bonino
 * 
 */
public class PelletStoveDriverInstance extends ApplianceDriverInstance
		implements PelletHeater
{

	/**
	 * @param device
	 * @param logger
	 * @param stateMachineLocator
	 */
	public PelletStoveDriverInstance(ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context)
	{
		// call the superclass constructor
		super(device, stateMachineLocator, logger);

		// initialize states
		this.initializeStates();
	}

	@Override
	public DeviceStatus getState()
	{
		// return the current device state
		return this.currentState;
	}

	@Override
	public void on()
	{
		// do nothing....

	}

	@Override
	public void standBy()
	{
		// do nothing....

	}

	@Override
	public void off()
	{
		// do nothing....

	}

	@Override
	public void notifyFiringUp()
	{
		((PelletHeater) this.device).notifyFiringUp();
	}

	@Override
	public void notifyCool()
	{
		((PelletHeater) this.device).notifyCool();
	}

	@Override
	public void notifyHeat()
	{
		((PelletHeater) this.device).notifyHeat();

	}

	@Override
	public void notifyOn()
	{
		((PelletHeater) this.device).notifyOn();
	}

	@Override
	public void notifyOff()
	{
		((PelletHeater) this.device).notifyOff();
	}

	@Override
	public void notifyStandby()
	{
		((PelletHeater) this.device).notifyStandby();
	}

	@Override
	public void updateStatus()
	{
		((PelletHeater) this.device).updateStatus();
	}

	@Override
	protected void newMessageFromHouse(String newStateName)
	{
		this.logger.log(LogService.LOG_INFO, "Entered:" + newStateName);
		// handle the new state
		switch (newStateName)
		{
			case "on":
			{
				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new OnStateValue()));

				// notify the state change
				this.notifyOn();
				break;
			}
			case "off":
			{
				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new OffStateValue()));

				// notify the state change
				this.notifyOff();
				break;
			}
			case "heating":
			{
				// change the current state
				this.currentState.setState(HeaterState.class.getSimpleName(),
						new HeaterState(new HeatingStateValue()));

				// notify the state change
				this.notifyHeat();
				break;
			}
			case "cooling":
			{
				// change the current state
				this.currentState.setState(HeaterState.class.getSimpleName(),
						new HeaterState(new CoolingStateValue()));

				// notify the state change
				this.notifyCool();
				break;
			}
			case "firingUp":
			{
				// change the current state
				this.currentState.setState(HeaterState.class.getSimpleName(),
						new HeaterState(new FireUpStateValue()));

				// notify the state change
				this.notifyFiringUp();
				break;
			}
			case "standby":
			{

				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new StandByStateValue()));

				// notify the state change
				this.notifyStandby();
				break;
			}
		}
		this.updateStatus();
	}

	@Override
	protected void specificConfiguration()
	{
		// intentionally left empty
	}

	/**
	 * Initializes the state of this pellet stove instance, initially set to
	 * off.
	 */
	private void initializeStates()
	{
		// initialize the state at off
		this.currentState.setState(StandByOnOffState.class.getSimpleName(),
				new StandByOnOffState(new OffStateValue()));

	}

}
