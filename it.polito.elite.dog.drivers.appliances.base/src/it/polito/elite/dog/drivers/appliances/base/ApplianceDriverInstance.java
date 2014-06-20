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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author bonino
 *
 */
public abstract class ApplianceDriverInstance
{
	// the DogOnt-defined device managed by instances of classes descending from
		// this abstract class.
		protected ControllableDevice device;
		
		// the state of the device associated to this driver
		protected DeviceStatus currentState;
		
		// the set of notifications associated to the driver
		protected HashMap<String, CNParameters> notifications;

		// the set of commands associated to the driver
		protected HashMap<String, CNParameters> commands;

		/**
		 * @param device
		 */
		public ApplianceDriverInstance(ControllableDevice device)
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
		}
		
		/**
		 * Extending classes might implement this method to provide driver-specific
		 * configurations to be done during the driver creation process, before
		 * associating the device-specific driver to the network driver
		 */
		protected abstract void specificConfiguration();
		
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
				//nothing to do....? check which kind of parameter should be matched...
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
					String commandName = params.get(ConfigurationConstants.COMMAND_NAME);

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
					String notificationName = params.get(ConfigurationConstants.NOTIFICATION_NAME);

					if (notificationName != null)
						// store the parameters associated to the command
						this.notifications.put(notificationName, new CNParameters(
								notificationName, params));
				}

			}

		}
		
}
