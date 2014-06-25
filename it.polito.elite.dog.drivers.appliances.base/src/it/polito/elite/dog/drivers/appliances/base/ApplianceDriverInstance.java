/*
 * Dog 3.1 - Dumb Appliance Driver
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
package it.polito.elite.dog.drivers.appliances.base;

import it.polito.elite.dog.core.library.model.CNParameters;
import it.polito.elite.dog.core.library.model.ConfigurationConstants;
import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.util.ElementDescription;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.digester.Digester;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.SimpleErrorReporter;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * @author bonino
 * 
 */
public abstract class ApplianceDriverInstance implements SCXMLListener
{
	// the DogOnt-defined device managed by instances of classes descending from
	// this abstract class.
	protected ControllableDevice device;

	// the state machine locator to use for accessing the device specific state
	// machine
	protected ApplianceStateMachineLocator stateMachineLocator;

	// the serial number assoiated to this device
	protected String deviceSerial;
	
	// the state machine associated to this driver instance
	protected SCXML stateMachine;
	
	// the state machine executor associated to this driver instance
	protected SCXMLExecutor executor;

	// the state of the device associated to this driver
	protected DeviceStatus currentState;

	// the set of notifications associated to the driver
	protected HashMap<String, CNParameters> notifications;

	// the set of commands associated to the driver
	protected HashMap<String, CNParameters> commands;

	/**
	 * @param device
	 */
	public ApplianceDriverInstance(ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator)
	{
		// store a reference to the associate device
		this.device = device;

		// initialize data structures
		this.notifications = new HashMap<String, CNParameters>();
		this.commands = new HashMap<String, CNParameters>();

		// fill the data structures depending on the specific device
		// configuration parameters
		this.fillConfiguration();

		// call the specific configuration method, if needed
		this.specificConfiguration();

		// handle the state machine associated to the given controllable device
		this.initializeDeviceStateMachine();
	}

	/**
	 * Extending classes should implement this method for handling state
	 * changes, as triggered / detected by the device-specific state machine.
	 * State machines provide the current state name that, by design, should
	 * match the current state value to be set as device current state.
	 * 
	 * @param newStateName
	 */
	protected abstract void newMessageFromHouse(String newStateName);

	/**
	 * Extending classes might implement this method to provide driver-specific
	 * configurations to be done during the driver creation process, before
	 * associating the device-specific driver to the network driver
	 */
	protected abstract void specificConfiguration();

	private void initializeDeviceStateMachine()
	{
		// check if all needed elements are initialized
		if ((this.deviceSerial != null) && (!this.deviceSerial.isEmpty())
				&& (this.stateMachineLocator != null))
		{
			// get the device-specific state machine URL, if any available
			URL stateMachineURL = this.stateMachineLocator
					.getStateMachine(this.deviceSerial);

			// check not null
			if (stateMachineURL != null)
			{
				try
				{
					// get the SCXML state machine (SCXML root element)
					this.stateMachine = SCXMLReader.read(stateMachineURL);

					// build a new executor
					this.executor = new SCXMLExecutor(
							new JexlEvaluator(), new SimpleDispatcher(),
							new SimpleErrorReporter());
					
					//set the state machine
					this.executor.setStateMachine(this.stateMachine);
					
					//set the root context
					this.executor.setRootContext(new JexlContext());
					
					//add this class as listener of the state machine changes
					this.executor.addListener(this.stateMachine,this);
					
					//start the engine
					this.executor.go();
				}
				catch (IOException | ModelException | XMLStreamException e)
				{
					// TODO handle using logging
					e.printStackTrace();
				}

			}
		}

	}

	/***
	 * Fills the inner data structures depending on the specific device
	 * configuration parameters, extracted from the device instance associated
	 * to this driver instance
	 */
	private void fillConfiguration()
	{
		// specified for the whole device
		Map<String, Set<String>> deviceConfigurationParams = this.device
				.getDeviceDescriptor().getSimpleConfigurationParams();

		// check not null
		if (deviceConfigurationParams != null)
		{
			// get the local id 1-sized set
			Set<String> serials = deviceConfigurationParams
					.get(ApplianceInfo.SERIAL);

			if ((serials != null) && (serials.size() == 1))
			{
				// store the local id anf the corresponding bridge ip
				this.deviceSerial = serials.iterator().next();
			}
		}

		// gets the properties associated to each device commmand/notification,
		// if any. E.g.,
		// the unit of measure associated to meter functionalities.

		// get parameters associated to each device command (if any)
		Set<ElementDescription> commandsSpecificParameters = this.device
				.getDeviceDescriptor().getCommandSpecificParams();

		// get parameters associated to each device notification (if any)
		Set<ElementDescription> notificationsSpecificParameters = this.device
				.getDeviceDescriptor().getNotificationSpecificParams();

		// --------------- Handle command specific parameters ----------------
		for (ElementDescription parameter : commandsSpecificParameters)
		{

			// the parameter map
			Map<String, String> params = parameter.getElementParams();
			if ((params != null) && (!params.isEmpty()))
			{
				// the name of the command associated to this device...
				String commandName = params
						.get(ConfigurationConstants.COMMAND_NAME);

				if (commandName != null)
					// store the parameters associated to the command
					this.commands.put(commandName, new CNParameters(
							commandName, params));
			}

		}

		// --------Handle notification specific parameters ----------------
		for (ElementDescription parameter : notificationsSpecificParameters)
		{
			// the parameter map
			Map<String, String> params = parameter.getElementParams();
			if ((params != null) && (!params.isEmpty()))
			{
				// the name of the command associated to this device...
				String notificationName = params
						.get(ConfigurationConstants.NOTIFICATION_NAME);

				if (notificationName != null)
					// store the parameters associated to the command
					this.notifications.put(notificationName, new CNParameters(
							notificationName, params));
			}

		}

	}

	@Override
	public void onEntry(final EnterableState enteredIn)
	{
		//dispatch the message
		this.newMessageFromHouse(enteredIn.getId());
	}

	
}
